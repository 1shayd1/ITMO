package org.Gh0st1yAnge1;

import org.Gh0st1yAnge1.audit.AuditProducer;
import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.auth.HasherFactory;
import org.Gh0st1yAnge1.auth.PasswordHasher;
import org.Gh0st1yAnge1.config.Config;
import org.Gh0st1yAnge1.db.DatabaseManager;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.db.UserRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.manager.InputManager;
import org.Gh0st1yAnge1.manager.ServerCommandExecutor;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.LoggerUtil;
import org.Gh0st1yAnge1.utils.TcpUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApp {

    private static final int PORT = 12345;
    private static final Logger logger = LoggerUtil.getLogger();

    private static UserRepository userRepository;
    private static AuthService authService;
    private static PasswordHasher passwordHasher;
    private static Config config;
    private static DatabaseManager databaseManager;
    private static CollectionManager collectionManager;
    private static ServerCommandExecutor serverCommandExecutor;
    private static InputManager inputManager;
    private static AuditProducer auditProducer;
    private static volatile boolean running = true;

    private static final ExecutorService readPool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final ForkJoinPool processPool = new ForkJoinPool();
    private static final ForkJoinPool sendPool = new ForkJoinPool();

    private static final Set<SelectionKey> closingKeys = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();
    private static volatile Selector mainSelector;

    public static void main(String[] args) throws SQLException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");
        logger.setLevel(Level.INFO);
        logger.info("=== LOADING SERVER ===");

        try {
            config = new Config();
            config.printConfig();
            databaseManager = new DatabaseManager(
                    config.getDbUrl(),
                    config.getDbUser(),
                    config.getDbPassword()
            );
            System.out.println("DB started!");
        } catch (Exception e) {
            logger.severe("DB isn't connected: " + e.getMessage() + ". Shutting down.");
            return;
        }

        passwordHasher = HasherFactory.create(config.getHashAlgorithm());
        logger.info("Using hash algorithm " + config.getHashAlgorithm());
        userRepository = new UserRepository(databaseManager, passwordHasher);
        authService = new AuthService(userRepository);

        RouteRepository routeRepository = new RouteRepository(databaseManager);

        LinkedHashMap<Integer, Route> loadedData = routeRepository.loadCollection();
        collectionManager = new CollectionManager();
        collectionManager.load(loadedData);
        logger.info("Collection loaded. Elements count: " + loadedData.size());

        String kafkaBootstrap = config.getKafkaBootstrapServers();
        if (kafkaBootstrap != null && !kafkaBootstrap.trim().isEmpty()) {
            try {
                auditProducer = new AuditProducer(kafkaBootstrap);
                logger.info("Kafka audit enabled. Bootstrap: " + kafkaBootstrap);
            } catch (Exception e) {
                logger.warning("Failed to init Kafka: " + e.getMessage() + ". Running without audit.");
                auditProducer = null;
            }
        } else {
            logger.info("KAFKA_BOOTSTRAP_SERVERS not set. Running without audit.");
        }

        serverCommandExecutor = new ServerCommandExecutor(collectionManager, auditProducer, authService, routeRepository);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (auditProducer != null) {
                auditProducer.close();
                logger.info("AuditProducer closed.");
            }
        }));

        logger.info("Starting server loop...");
        runServer();
    }

    private static void runServer() {
        Thread networkThread = new Thread(() -> {
            try (Selector selector = Selector.open();
                 ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

                serverChannel.configureBlocking(false);
                serverChannel.bind(new InetSocketAddress(PORT));
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                mainSelector = selector;
                logger.info("Server started on port: " + PORT);

                while (running) {

                    if (!selectorTasks.isEmpty()) {
                        selector.selectNow();
                    } else {
                        selector.select(10);
                    }

                    Runnable task;
                    while ((task = selectorTasks.poll()) != null) task.run();

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) continue;

                        try {
                            if (key.isAcceptable()) {
                                handleAccept(key, selector);
                            }
                            if (key.isValid() && key.isReadable()) {
                                key.interestOps(0);
                                readPool.submit(() -> dispatchRead(key));
                            }
                        } catch (IOException e) {
                            logger.severe("Accept error: " + e.getMessage());
                            closeClient(key);
                        }
                    }
                }
            } catch (IOException e) {
                logger.severe("Server startup error: " + e.getMessage());
            }
        });

        networkThread.setDaemon(true);
        networkThread.start();

        inputManager = new InputManager();
        while (running) {
            System.out.print("> ");
            String input = inputManager.readline();
            if (input == null || input.trim().isEmpty()) continue;
            String answer = serverCommandExecutor.execute(input);
            if (answer == null) continue;
            if (answer.equals("Session terminated.")) {
                if (auditProducer != null) {
                    auditProducer.close();
                    logger.info("AuditProducer closed.");
                }
                System.out.println(answer);
                running = false;
                System.exit(0);
            }
            System.out.println(answer);
        }
    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ, new ClientSession());
        if (clientChannel.getRemoteAddress() instanceof InetSocketAddress isa) {
            logger.info("New connection from: " + isa.getAddress().getHostAddress() + ":" + isa.getPort());
        }
    }

    private static void dispatchRead(SelectionKey key) {
        try {
            boolean dispatched = handleRead(key);
            if (!dispatched) {
                rearmRead(key);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (e.getMessage() != null) logger.warning("Read error: " + e.getMessage());
            closeClient(key);
        }
    }

    private static void rearmRead(SelectionKey key) {
        selectorTasks.add(() -> {
            if (key.isValid()) key.interestOps(SelectionKey.OP_READ);
        });
        if (mainSelector != null) mainSelector.wakeup();
    }

    private static boolean handleRead(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        while (true) {
            if (session.remainingChunkBytes == null) {
                int bytesRead = clientChannel.read(session.chunkLengthBuffer);
                if (bytesRead == -1) { closeClient(key); return true; }
                if (session.chunkLengthBuffer.hasRemaining()) return false;

                session.chunkLengthBuffer.flip();
                int chunkLength = session.chunkLengthBuffer.getInt();
                session.chunkLengthBuffer.clear();

                if (chunkLength < 0) { closeClient(key); return true; }

                if (chunkLength == 0) {
                    session.payloadOutputStream.flush();
                    Request request = (Request) TcpUtil.desirealizeFromFile(session.payloadFile);

                    if (clientChannel.getRemoteAddress() instanceof InetSocketAddress isa) {
                        logger.info("Request: " + request.commandType() + " from " + isa.getAddress().getHostAddress());
                    }

                    processPool.submit(() -> {
                        try {
                            Response response = serverCommandExecutor.execute(request);
                            sendPool.submit(() -> {
                                try {
                                    sendResponse(clientChannel, response);
                                    session.reset();
                                    rearmRead(key);
                                } catch (IOException e) {
                                    logger.warning("Send error: " + e.getMessage());
                                    closeClient(key);
                                }
                            });
                        } catch (Exception e) {
                            logger.warning("Process error: " + e.getMessage());
                            closeClient(key);
                        }
                    });
                    return true;
                }

                session.remainingChunkBytes = chunkLength;
            }

            session.payloadBuffer.clear();
            int portion = Math.min(session.remainingChunkBytes, session.payloadBuffer.capacity());
            session.payloadBuffer.limit(portion);
            int bytesRead = clientChannel.read(session.payloadBuffer);
            if (bytesRead == -1) { closeClient(key); return true; }
            if (bytesRead == 0) return false;
            session.payloadOutputStream.write(session.payloadBuffer.array(), 0, bytesRead);
            session.remainingChunkBytes -= bytesRead;
            if (session.remainingChunkBytes == 0) session.remainingChunkBytes = null;
        }
    }

    private static void sendResponse(SocketChannel channel, Response response) throws IOException {
        Path payloadFile = TcpUtil.serializeToTempFile(response);
        try {
            TcpUtil.writeChunkedFromFile(channel, payloadFile);
        } finally {
            Files.deleteIfExists(payloadFile);
        }
    }

    private static void closeClient(SelectionKey key) {
        if (!closingKeys.add(key)) return;
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            try { if (channel.getRemoteAddress() instanceof InetSocketAddress isa) {
                logger.info("Client disconnected: " + isa.getAddress().getHostAddress());
            } }
            catch (IOException ignored) {}
            ClientSession session = (ClientSession) key.attachment();
            if (session != null) { try { session.cleanUp(); } catch (IOException ignored) {} }
            key.cancel();
            try { channel.close(); } catch (IOException ignored) {}
        } finally {
            closingKeys.remove(key);
        }
    }

    private static class ClientSession {
        ByteBuffer chunkLengthBuffer = ByteBuffer.allocate(4);
        ByteBuffer payloadBuffer     = ByteBuffer.allocate(TcpUtil.CHUNK_SIZE);
        Integer remainingChunkBytes  = null;
        Path payloadFile;
        OutputStream payloadOutputStream;

        ClientSession() throws IOException { init(); }

        void init() throws IOException {
            chunkLengthBuffer.clear();
            remainingChunkBytes = null;
            payloadBuffer.clear();
            payloadFile = Files.createTempFile("tcp-request", ".bin");
            payloadOutputStream = Files.newOutputStream(payloadFile);
        }

        void cleanUp() throws IOException {
            if (payloadOutputStream != null) payloadOutputStream.close();
            if (payloadFile != null) Files.deleteIfExists(payloadFile);
        }

        void reset() throws IOException { cleanUp(); init(); }
    }
}
