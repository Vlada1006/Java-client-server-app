package org.example;

import org.json.CDL;
import org.json.JSONArray;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ClientTest {
    public static void main(String[] args) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String string = "name, city, age \n"
                + "john, chicago, 22 \n"
                + "gary, florida, 35 \n"
                + "sal, vegas, 18";
        JSONArray json = CDL.toJSONArray(string);
        Message message = new Message((byte)10, 5, 7, 30, json);
        byte[] encryption_key = "thisisa128bitkey".getBytes();
        Client client = new Client();
        client.startConnection("127.0.0.1", 44444);
        client.sendMessage(message, encryption_key);
        client.stopConnection();
    }
}
