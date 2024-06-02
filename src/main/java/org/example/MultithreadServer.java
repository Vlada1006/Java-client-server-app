package org.example;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MultithreadServer {

    private ServerSocket serverSocket;

    public void start(int port) throws InterruptedException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Server server = new Server(serverSocket.accept());
                server.start();
                server.join();
                Message message = server.getMessage();
                CommandsProcessor.ServeCommand(message.getCommand_number(), message.getSender_number(), message.getData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
