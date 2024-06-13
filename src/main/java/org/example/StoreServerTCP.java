package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class StoreServerTCP extends Thread {

    private final byte[] encryption_key =  "thisisa128bitkey".getBytes();
    private final Socket clientSocket;
    private final byte[] input_beginning = new byte[14];

    public StoreServerTCP(Socket socket) {
        clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            while(in.read(input_beginning, 0, 14) != -1){
                byte[] input_end = new byte[ByteBuffer.wrap(Arrays.copyOfRange(input_beginning, 10, 14)).getInt() + 4];
                in.read(input_end);
                byte[] input = new byte[14 + input_end.length];
                System.arraycopy(input_beginning, 0, input, 0, 14);
                System.arraycopy(input_end, 0, input, 14, input_end.length);
                new CommandsProcessorTCP(input, encryption_key, out).start();
            }
        }
        catch (IOException er) {
            System.out.println("Client error");
        }
        finally {
            System.out.println("Connection closed");
            try {
                clientSocket.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

