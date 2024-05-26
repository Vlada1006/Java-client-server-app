package org.example;

import org.example.Message;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.json.CDL;
import org.json.JSONArray;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class ReceiverSenderTest {

    Message GetInitialMessage() {
        String string = "name, city, age \n"
                + "john, chicago, 22 \n"
                + "gary, florida, 35 \n"
                + "sal, vegas, 18";
        JSONArray json = CDL.toJSONArray(string);
        return new Message((byte) 10, 5, 7, 30, json);
    }

    Message GetEncodedThenDecodedMessage() throws InvalidObjectException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Message initialMessage = GetInitialMessage();
        byte[] encodedMessage = encodeMessage(initialMessage);
        Message decodedMessage = decodeMessage(encodedMessage);
        return decodedMessage;
    }

    byte[] encodeMessage(Message message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String encryptionKeyString = "thisisa128bitkey";
        byte[] encryptionKey = encryptionKeyString.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] messageData = CDL.toString(message.getData()).getBytes(StandardCharsets.UTF_8);
        byte[] messageDataEncoded = cipher.doFinal(messageData);
        return messageDataEncoded;
    }

    Message decodeMessage(byte[] encodedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidObjectException {
        String encryptionKeyString = "thisisa128bitkey";
        byte[] encryptionKey = encryptionKeyString.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedMessageData = cipher.doFinal(encodedMessage);
        System.out.println("Decoded Message Data: " + new String(decodedMessageData, StandardCharsets.UTF_8));
        JSONArray jsonData = new JSONArray(new String(decodedMessageData, StandardCharsets.UTF_8));
        byte clientNumber = 1;
        long messageNumber = 123456;
        int commandNumber = 100;
        int senderNumber = 200;

        return new Message(clientNumber, messageNumber, commandNumber, senderNumber, jsonData);
    }

    @Test
    void testGetEncodedThenDecodedMessage() {
        ReceiverSenderTest receiverSenderTest = new ReceiverSenderTest();
        try {
            Message decodedMessage = receiverSenderTest.GetEncodedThenDecodedMessage();
            // Add assertions to verify the correctness of the decoded message
            assertEquals(1, decodedMessage.getClient_number());
            assertEquals(123456, decodedMessage.getMessage_number());
            assertEquals(100, decodedMessage.getCommand_number());
            assertEquals(200, decodedMessage.getSender_number());
            // Add additional assertions for other message properties if needed
        } catch (InvalidObjectException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

}

