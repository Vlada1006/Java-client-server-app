package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.ByteBuffer;
import org.json.*;

public class Receiver {
    private InputStream inputStream;
    private byte[] encryptionKey;

    public Receiver(InputStream inputStream, byte[] encryptionKey) {
        this.inputStream = inputStream;
        this.encryptionKey = encryptionKey;
    }

    public Message receiveMessage() throws IOException, InvalidObjectException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] messageBytes = new byte[1024]; // Adjust the size according to your message length
        int bytesRead = inputStream.read(messageBytes);
        if (bytesRead == -1) {
            throw new IOException("End of stream reached.");
        }

        if (messageBytes[0] != 0x13) {
            throw new InvalidObjectException("Invalid message: Incorrect bMagic value at the beginning.");
        }

        if (CRC16.CRC16Code(Arrays.copyOfRange(messageBytes, 0, 14)) != ByteBuffer.wrap(Arrays.copyOfRange(messageBytes, 14, 16)).getShort()) {
            throw new InvalidObjectException("Invalid message: CRC16 checksum mismatch for the header.");
        }

        int expectedLen = ByteBuffer.wrap(Arrays.copyOfRange(messageBytes, 10, 14)).getInt();
        int actualLen = bytesRead - 18; // Subtracting header (14 bytes) and CRC16 (2 bytes) lengths
        if (expectedLen != actualLen) {
            throw new InvalidObjectException("Invalid message: Length mismatch between expected and actual message length.");
        }

        if (CRC16.CRC16Code(Arrays.copyOfRange(messageBytes, 16, bytesRead - 2)) != ByteBuffer.wrap(Arrays.copyOfRange(messageBytes, bytesRead - 2, bytesRead)).getShort()) {
            throw new InvalidObjectException("Invalid message: CRC16 checksum mismatch for the message.");
        }

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Arrays.copyOfRange(messageBytes, 16, bytesRead - 2)); // Decrypt the message

        // Deserialize decrypted JSON data
        JSONArray jsonData = new JSONArray(new String(decrypted, StandardCharsets.UTF_8));

        // Extract message components
        byte clientNumber = messageBytes[1];
        long messageNumber = ByteBuffer.wrap(Arrays.copyOfRange(messageBytes, 2, 10)).getLong();
        int commandNumber = ByteBuffer.wrap(Arrays.copyOfRange(decrypted, 0, 4)).getInt();
        int senderNumber = ByteBuffer.wrap(Arrays.copyOfRange(decrypted, 4, 8)).getInt();

        // Create and return Message object
        return new Message(clientNumber, messageNumber, commandNumber, senderNumber, jsonData);
    }
}
