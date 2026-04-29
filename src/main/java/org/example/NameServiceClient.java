package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// simple client class for the distributed name service
// connects to the registry server so we can register and resolve stuff
public class NameServiceClient {

    // main method to run the client
    // args aren't used right now
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        System.out.println("---  Distributed Name Service - Client Node  ---");

        // set up our socket and streams to talk to the server (using try-with-resources so we don't leak)
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
            // keep grabbing input from console
            while ((userInput = console.readLine()) != null) {
                // quit if they type exit
                if (userInput.equalsIgnoreCase("EXIT")) break;
                // skip if they just pressed enter
                if (userInput.trim().isEmpty()) {
                    System.out.print("> ");
                    continue;
                }

                // send whatever they typed to the server
                out.writeUTF(userInput);

                // wait for the server to reply and print it out
                String response = in.readUTF();
                System.out.println("[Server] " + response + "\n");
                System.out.print("> ");
            }

        } catch (IOException e) {
            // catch any socket issues
            System.err.println("[Client] Error: " + e.getMessage());
        }
        System.out.println("[Client] Disconnected.");
    }
}
