package org.example;

import org.example.MultithreadServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class MultithreadServerTest {

    private MultithreadServer server;
    private int port;

    @Test
    public void testServerStartStop() {
        MultithreadServer server = new MultithreadServer();
        int port = 44444;
        try {
            server.start(port);
            assertTrue(isServerRunning(port));
            server.stop();
            assertFalse(isServerRunning(port));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    private boolean isServerRunning(int port) {
        try {
            new Socket("localhost", port).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Before
    public void setUp() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        server = new MultithreadServer();
        port = 44444;
        server.start(port);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testClientServerCommunication() {
        try {
            sendMessageToServer("Hello, Server!");
            Thread.sleep(1000);
            assertTrue(serverReceivedMessage("Hello, Server!"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    private void sendMessageToServer(String message) throws Exception {
        Socket socket = new Socket("localhost", port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(message);
        out.flush();
        socket.close();
    }

    private boolean serverReceivedMessage(String expectedMessage) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String receivedMessage = in.readLine();
        clientSocket.close();
        serverSocket.close();
        return expectedMessage.equals(receivedMessage);
    }
}


