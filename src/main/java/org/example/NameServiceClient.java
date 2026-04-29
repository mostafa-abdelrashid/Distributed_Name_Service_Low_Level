package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class NameServiceClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        System.out.println("---  Distributed Name Service - Client Node  ---");

        try (
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("[Client] Connected to " + host + ":" + port);
            System.out.println("Commands: REGISTER <name> <ip>, RESOLVE <name>, DEREGISTER <name>, EXIT\n");

            String userInput;
            System.out.print("> ");
            while ((userInput = console.readLine()) != null) {
                if (userInput.equalsIgnoreCase("EXIT")) break;
                if (userInput.trim().isEmpty()) {
                    System.out.print("> ");
                    continue;
                }

                out.writeUTF(userInput);

                String response = in.readUTF();
                System.out.println("[Server] " + response + "\n");
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.err.println("[Client] Error: " + e.getMessage());
        }
        System.out.println("[Client] Disconnected.");
    }
}
