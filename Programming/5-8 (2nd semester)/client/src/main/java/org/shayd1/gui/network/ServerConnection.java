package org.shayd1.gui.network;

import org.shayd1.model.Route;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;
import org.shayd1.request_and_response.Response;
import org.shayd1.utils.TcpUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerConnection implements AutoCloseable {

    private static final String HOST = "localhost";
    private static final int    PORT = 12345;

    private SocketChannel channel;

    private String login;
    private String password;

    public synchronized void connect() throws IOException {
        if (isConnected()) return;

        close();

        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(HOST, PORT));

        long deadline = System.currentTimeMillis() + 5000;
        while (!channel.finishConnect()) {
            if (System.currentTimeMillis() > deadline) {
                throw new IOException("Connection timeout");
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Connection interrupted");
            }
        }
    }

    public synchronized boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isConnected();
    }

    @Override
    public synchronized void close() {
        try {
            if (channel != null) channel.close();
        } catch (IOException ignored) {}
        channel = null;
    }

    public synchronized void setCredentials(String login, String password) {
        this.login    = login;
        this.password = password;
    }

    public Response login(String login, String password) throws IOException, ClassNotFoundException {
        Request req = new Request(CommandType.LOGIN, login + " " + password, null, login, password);
        return send(req);
    }

    public Response register(String login, String password) throws IOException, ClassNotFoundException {
        Request req = new Request(CommandType.REGISTER, login + " " + password, null, login, password);
        return send(req);
    }

    public Response show() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.SHOW, null, null));
    }

    public Response info() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.INFO, null, null));
    }

    public Response checkInsertKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CHECK_INSERT_KEY, String.valueOf(key), null));
    }

    public Response checkUpdateKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CHECK_UPDATE_KEY, String.valueOf(key), null));
    }

    public Response insert(int key, Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.INSERT, String.valueOf(key), route));
    }

    public Response update(int key, Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.UPDATE, String.valueOf(key), route));
    }

    public Response removeKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.REMOVE_KEY, String.valueOf(key), null));
    }

    public Response clear() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CLEAR, null, null));
    }

    public Response averageOfDistance() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.AVERAGE_OF_DISTANCE, null, null));
    }

    public Response countByDistance(double distance) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.COUNT_BY_DISTANCE, String.valueOf(distance), null));
    }

    public Response filterLessThanDistance(double distance) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.FILTER_LESS_THAN_DISTANCE, String.valueOf(distance), null));
    }

    public Response removeGreater(Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.REMOVE_GREATER, null, route));
    }

    public Response removeGreaterKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.REMOVE_GREATER_KEY, String.valueOf(key), null));
    }

    public Response replaceIfLower(int key, Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.REPLACE_IF_LOWER, String.valueOf(key), route));
    }


    private synchronized Request authed(CommandType type, String arg, Route route) {
        return new Request(type, arg, route, login, password);
    }

    public synchronized Response send(Request request) throws IOException, ClassNotFoundException {
        try {
            if (!isConnected()) {
                connect();
            }

            channel.configureBlocking(true);

            sendRequest(request);
            Response response = receiveResponse();

            channel.configureBlocking(false);
            return response;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private void sendRequest(Request request) throws IOException {
        byte[] data = serialize(request);
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(TcpUtil.CHUNK_SIZE, data.length - offset);
            writeAll(intToBytes(len));
            writeAll(data, offset, len);
            offset += len;
        }
        writeAll(intToBytes(0));
    }

    private Response receiveResponse() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream acc = new ByteArrayOutputStream();
        while (true) {
            byte[] lenBytes = readExactly(4);
            int chunkLen = ByteBuffer.wrap(lenBytes).getInt();
            if (chunkLen == 0) break;
            if (chunkLen < 0) throw new IOException("Invalid chunk size from server: " + chunkLen);
            acc.write(readExactly(chunkLen));
        }
        return (Response) deserialize(acc.toByteArray());
    }

    private void writeAll(byte[] bytes) throws IOException {
        writeAll(bytes, 0, bytes.length);
    }

    private void writeAll(byte[] bytes, int offset, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes, offset, length);
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    private byte[] readExactly(int n) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(n);
        while (buf.hasRemaining()) {
            int r = channel.read(buf);
            if (r == -1) throw new EOFException("Server prematurely closed connection");
            if (r == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while reading from server", e);
                }
            }
        }
        return buf.array();
    }

    private static byte[] intToBytes(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }
}