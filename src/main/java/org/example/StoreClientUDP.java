package org.example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class StoreClientUDP {
    private Socket clientSocket;
    private DataOutputStream out;

    public StoreClientUDP(Socket clientSocket, OutputStream outputStream) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new DataOutputStream(outputStream);
    }

    public void sendMessage(Message message, byte[] encryptionKey) throws IOException {
        // Your existing sendMessage logic
    }

    public void stopConnection() throws IOException {
        out.close();
        clientSocket.close();
    }
}
