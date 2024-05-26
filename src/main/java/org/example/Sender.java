package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.nio.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.json.*;

public class Sender {
    private OutputStream outputStream;
    private byte[] encryptionKey;

    public Sender(OutputStream outputStream, byte[] encryptionKey) {
        this.outputStream = outputStream;
        this.encryptionKey = encryptionKey;
    }

    public void sendMessage(Message message) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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

        // Send packet over output stream
        outputStream.write(buffer.array());
        outputStream.flush();
    }
}

