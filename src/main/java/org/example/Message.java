package org.example;
import org.json.*;

public class Message {
    private byte client_number;
    private long message_number;
    private int command_number;
    private int sender_number;
    private JSONArray data;

    public Message(byte commandNumber, long messageNumber, int clientNumber, JSONArray data) {
    }

    public byte getClient_number() {
        return client_number;
    }
    public long getMessage_number() {
        return message_number;
    }
    public int getCommand_number() {
        return command_number;
    }
    public int getSender_number() {
        return sender_number;
    }
    public JSONArray getData() {
        return data;
    }

    public Message(byte client_number, long message_number, int command_number, int sender_number, JSONArray data){
        this.client_number = client_number;
        this.message_number = message_number;
        this.command_number = command_number;
        this.sender_number = sender_number;
        this.data = data;
    }
}
