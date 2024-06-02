package org.example;

import org.json.JSONArray;

public class CommandsProcessor {
    CommandsProcessor(){}
    public static void ServeCommand(int command_number, int sender_number, JSONArray data){
        System.out.println("Command was executed successfully");
    }
}
