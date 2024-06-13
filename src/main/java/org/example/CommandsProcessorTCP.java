package org.example;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.json.JSONArray;

public class CommandsProcessorTCP extends Thread {
    byte[] input;
    DataOutputStream out;
    byte[] encryption_key;

    CommandsProcessorTCP(byte[] input, byte[] encryption_key, DataOutputStream out) {
        this.input = input;
        this.out = out;
        this.encryption_key = encryption_key;
    }

    @Override
    public void run() {
        try {
            try {
                Message message = decodeMessage(input, encryption_key);
                out.writeLong(message.getMessage_number());
                try {
                    serveCommand(message);
                    out.writeInt(message.getCommand_number());
                } catch (AssertionError e) {
                    out.writeInt(-1);
                    System.out.print("Invalid command: ");
                    System.out.print(message.getCommand_number());
                    System.out.print(" in message: ");
                    System.out.print(message.getMessage_number());
                    System.out.print(" in client: ");
                    System.out.println(message.getClient_number());
                }
            } catch (InvalidObjectException e) {
                out.writeLong(-1);
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException er) {
                er.printStackTrace();
            }
        } catch (IOException er) {
            System.out.println("Client error");
        }
    }

    private Message decodeMessage(byte[] input, byte[] encryptionKey) throws InvalidObjectException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (input[0] != 0x13) {
            throw new InvalidObjectException("Invalid message: Incorrect bMagic value at the beginning.");
        }

        if (CRC16.CRC16Code(Arrays.copyOfRange(input, 0, 14)) != ByteBuffer.wrap(Arrays.copyOfRange(input, 14, 16)).getShort()) {
            throw new InvalidObjectException("Invalid message: CRC16 checksum mismatch for the header.");
        }

        int expectedLen = ByteBuffer.wrap(Arrays.copyOfRange(input, 10, 14)).getInt();
        int actualLen = input.length - 18; // Subtracting header (14 bytes) and CRC16 (2 bytes) lengths
        if (expectedLen != actualLen) {
            throw new InvalidObjectException("Invalid message: Length mismatch between expected and actual message length.");
        }

        if (CRC16.CRC16Code(Arrays.copyOfRange(input, 16, input.length - 2)) != ByteBuffer.wrap(Arrays.copyOfRange(input, input.length - 2, input.length)).getShort()) {
            throw new InvalidObjectException("Invalid message: CRC16 checksum mismatch for the message.");
        }

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Arrays.copyOfRange(input, 16, input.length - 2)); // Decrypt the message

        // Deserialize decrypted JSON data
        JSONArray jsonData = new JSONArray(new String(decrypted, StandardCharsets.UTF_8));

        // Extract message components
        byte clientNumber = input[1];
        long messageNumber = ByteBuffer.wrap(Arrays.copyOfRange(input, 2, 10)).getLong();
        int commandNumber = ByteBuffer.wrap(Arrays.copyOfRange(decrypted, 0, 4)).getInt();
        int senderNumber = ByteBuffer.wrap(Arrays.copyOfRange(decrypted, 4, 8)).getInt();

        // Create and return Message object
        return new Message(clientNumber, messageNumber, commandNumber, senderNumber, jsonData);
    }

    private void serveCommand(Message message) {
        switch (message.getCommand_number()) {
            case 1:
                System.out.println("Command 1 was executed successfully");
                break;
            case 2:
                System.out.println("Command 2 was executed successfully");
                break;
            case 3:
                System.out.println("Command 3 was executed successfully");
                break;
            case 4:
                System.out.println("Command 4 was executed successfully");
                break;
            case 5:
                System.out.println("Command 5 was executed successfully");
                break;
            case 6:
                System.out.println("Command 6 was executed successfully");
                break;
            default:
                throw new AssertionError();
        }
    }
}
