package org.example;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.*;

public class StoreClientUDPTest {
    private StoreClientUDP client;
    private Socket socket;
    private DataOutputStream out;

    @BeforeEach
    public void setUp() throws IOException {
        socket = mock(Socket.class);
        out = mock(DataOutputStream.class);
        client = new StoreClientUDP(socket, out);
    }

    @Test
    public void testSendMessage() throws IOException {
        // Prepare test data
        byte commandNumber = 1;
        long messageNumber = 1L;
        int clientNumber = 1;
        JSONArray data = new JSONArray();
        Message message = new Message(commandNumber, messageNumber, clientNumber, data);
        byte[] encryptionKey = "1234567890123456".getBytes();
        byte[] encodedMessage = "encoded_message".getBytes(); // Replace with actual encoded message

        // Mock behavior of DataOutputStream 'out'
        when(socket.getOutputStream()).thenReturn(out);
        doNothing().when(out).write(any(byte[].class), anyInt(), anyInt());

        // Call the method under test
        client.sendMessage(message, encryptionKey);

        // Verify that DataOutputStream.write was called with the expected arguments
        verify(out, times(1)).write(eq(encodedMessage), eq(0), eq(encodedMessage.length));
    }

    @Test
    public void testStopConnection() throws IOException {
        // Call the method under test
        client.stopConnection();

        // Verify that close() was called on DataOutputStream and Socket
        verify(out, times(1)).close();
        verify(socket, times(1)).close();
    }
}
