package org.example;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class StoreClientTCP {
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    String ip;
    int port;

    public void startConnection(String ip, int port) throws IOException, InterruptedException {
        this.ip = ip;
        this.port = port;
        boolean connected = false;
        while (!connected) {
            try {
                clientSocket = new Socket(ip, port);
                connected = true;
            } catch (IOException e) {
                Thread.sleep(1000);
                System.out.println("Server not responding, waiting 1 s");
                connected = false;
            }
        }
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    public void sendMessage(Message message, byte[] encryption_key_string) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        byte[] encoded_message = encodeMessage(message, encryption_key_string);
        out.write(encoded_message, 0, encoded_message.length);
        while (true) {
            try {
                while (in.readLong() != message.getMessage_number()) {
                    System.out.println("Incorrect message received, sending another one in 1 s");
                    out.write(encoded_message, 0, encoded_message.length);
                    Thread.sleep(1000);
                }
                if (in.readInt() == -1) {
                    System.out.print("Invalid command: ");
                    System.out.print(message.getCommand_number());
                    System.out.print(" in message: ");
                    System.out.println(message.getMessage_number());
                }
                break;
            } catch (IOException e) {
                startConnection(ip, port);
                out.write(encoded_message, 0, encoded_message.length);
            }
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private byte[] encodeMessage(Message message, byte[] encryptionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // Serialize message data to JSON
        String jsonData = message.getData().toString();

        // Encrypt JSON data
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));

        // Create packet structure
        ByteBuffer buffer = ByteBuffer.allocate(18 + encryptedData.length);
        buffer.put((byte) 0x13); // bMagic
        buffer.put(message.getClient_number()); // bSrc
        buffer.putLong(message.getMessage_number()); // bPktId
        buffer.putInt(encryptedData.length); // wLen
        buffer.putShort(CRC16.CRC16Code(Arrays.copyOfRange(buffer.array(), 0, 14))); // wCrc16
        buffer.put(encryptedData); // bMsq
        buffer.putShort(CRC16.CRC16Code(Arrays.copyOfRange(buffer.array(), 16, buffer.array().length))); // wCrc16

        // Return encoded message
        return buffer.array();
    }
}
