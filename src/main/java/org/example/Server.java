package org.example;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Server extends Thread {

    private byte[] encryptionKey = "thisisa128bitkey".getBytes(StandardCharsets.UTF_8);
    private final Socket clientSocket;
    private byte[] inputBeginning = new byte[14];
    private byte[] inputEnd;
    private byte[] input;
    private Message message;

    public Server(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            while (in.read(inputBeginning, 0, 14) != -1) {
                int payloadLength = ByteBuffer.wrap(Arrays.copyOfRange(inputBeginning, 10, 14)).getInt() + 4;
                inputEnd = new byte[payloadLength];
                in.readFully(inputEnd);
                input = new byte[14 + payloadLength];
                System.arraycopy(inputBeginning, 0, input, 0, 14);
                System.arraycopy(inputEnd, 0, input, 14, payloadLength);

                // Process the message once it is fully read
                processMessage();
            }
        } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processMessage() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Receiver receiver = new Receiver(new ByteArrayInputStream(input), encryptionKey);
        message = receiver.receiveMessage();
        // Handle the decoded message here
        System.out.println("Received message: " + message);
    }

    public Message getMessage() {
        return message;
    }
}
