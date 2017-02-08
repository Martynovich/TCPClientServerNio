package com.andersen;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class ServerTCP implements Runnable {

    private Logger logger = Logger.getLogger(ServerTCP.class);

    private Selector selector;

    private Map<SocketChannel, String> clientsRequests = new HashMap<SocketChannel, String>();

    static final String END_OF_MESSAGE_FLAG = "\n END OF MESSAGE \n";

    public static final String SERVER_ECHO = " from server.";

    private InetSocketAddress inetSocketAddress;

    private int allVisitors = 0;

    private int clientsOnServer = 0;

    public ServerTCP(String host, int port) {
        logger.info("Creating server");
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    public void run() {
        this.startServer();
    }

    private void startServer() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(inetSocketAddress);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Server started.");
            while (true) {
                int num = selector.select();
                if (num == 0) continue;
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        allVisitors++;
        clientsOnServer++;
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr + ".Client №" + allVisitors + ". All clients on server - " + clientsOnServer);
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int numRead = channel.read(buffer);
        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        String request = new String(data);
        String currentRequest = clientsRequests.get(channel);
        if (currentRequest == null) {
            currentRequest = "";
        }
        clientsRequests.put(channel, currentRequest + request);
        String updatedRequest = clientsRequests.get(channel);
        if (updatedRequest.contains(END_OF_MESSAGE_FLAG)) {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
    private void write(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        String request = clientsRequests.get(channel).
                substring(0, (clientsRequests.get(channel).length() - END_OF_MESSAGE_FLAG.length()));
        String response = request + SERVER_ECHO;
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        System.out.println("Got: " + request);
        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.clear();
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientsOnServer--;
    }
}