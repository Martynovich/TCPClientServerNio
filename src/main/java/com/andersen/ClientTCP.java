package com.andersen;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientTCP {

    private Logger logger = Logger.getLogger(ClientTCP.class);

    private SocketChannel socketChannel;

    private String request = null;

    private String serverAnswer = null;

    private InetSocketAddress inetSocketAddress ;

    public ClientTCP(String host, int port) {
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    public String sendAndReceive(String request) {
        this.request = request;
        try {
            socketChannel = SocketChannel.open(inetSocketAddress);
            String message = request + ServerTCP.END_OF_MESSAGE_FLAG;
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(writeBuffer);
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int numRead = socketChannel.read(readBuffer);
            byte[] data = new byte[numRead];
            System.arraycopy(readBuffer.array(), 0, data, 0, numRead);
            serverAnswer = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
        return serverAnswer;
    }

    public  String getRequest() {
        return request;
    }
}
