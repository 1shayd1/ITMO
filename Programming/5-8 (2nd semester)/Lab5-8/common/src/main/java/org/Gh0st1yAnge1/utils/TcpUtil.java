package org.Gh0st1yAnge1.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class TcpUtil {
    public static final int CHUNK_SIZE = 8 * 1024;

    private TcpUtil(){};

    public static Path serializeToTempFile(Object object) throws IOException {
        Path tempFile = Files.createTempFile("tcp-payload", ".bin");
        try (OutputStream outputStream = Files.newOutputStream(tempFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        }
        return tempFile;
    }

    public static Object desirealizeFromFile(Path filePath) throws IOException, ClassNotFoundException{
        try (InputStream inputStream = Files.newInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
            return objectInputStream.readObject();
        }
    }

    public static void writeChunkedFromFile(SocketChannel socketChannel, Path filePath) throws IOException{
        try (Selector writeSelector = Selector.open()){
            socketChannel.register(writeSelector, SelectionKey.OP_WRITE);

            ByteBuffer headerBuf = ByteBuffer.allocate(4);
            byte[] chunk = new byte[CHUNK_SIZE];

            try (InputStream inputStream = Files.newInputStream(filePath)){
                int read;
                while ((read = inputStream.read(chunk)) != -1){
                    headerBuf.clear();
                    headerBuf.putInt(read);
                    headerBuf.flip();
                    writeFully(socketChannel, headerBuf, writeSelector);

                    ByteBuffer payloadBuf = ByteBuffer.wrap(chunk, 0, read);
                    writeFully(socketChannel, payloadBuf, writeSelector);
                }

                headerBuf.clear();
                headerBuf.putInt(0);
                headerBuf.flip();
                writeFully(socketChannel, headerBuf, writeSelector);
            }
        }
    }

    private static void writeFully(SocketChannel socketChannel, ByteBuffer byteBuffer, Selector selector) throws IOException {
        while (byteBuffer.hasRemaining()){
            int written = socketChannel.write(byteBuffer);
            if (written < 0){
                throw new EOFException("Channel closed while writing.");
            }
            if (written == 0){
                selector.select();
                selector.selectedKeys().clear();
            }
        }
    }
}

