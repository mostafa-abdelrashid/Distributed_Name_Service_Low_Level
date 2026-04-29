package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// central registry server for the project
// handles clients concurrently so we don't block
public class NameServiceServer {

    private static final int DEFAULT_PORT = 5000;
    // map to hold the names and IPs (needs to be thread safe!)
    static final ConcurrentHashMap<String, String> registry = new ConcurrentHashMap<>();

    // starts up the server on the port
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[Server] Invalid port. Using default " + DEFAULT_PORT);
            }
        }

        System.out.println("---   Distributed Name Service - Registry   ---");
        System.out.println("[Server] Starting on port " + port + " ...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Listening for connections on port " + port);

            // loop forever to accept new clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New connection from "
                        + clientSocket.getInetAddress().getHostAddress()
                        + ":" + clientSocket.getPort());

                // hand off to a new thread so we can keep listening
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }

        } catch (IOException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
        }
    }

    // thread task to handle one specific client
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // constructor just needs the socket
        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            // figure out who connected for logging
            String clientId = clientSocket.getInetAddress().getHostAddress()
                    + ":" + clientSocket.getPort();

            try (
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
            ) {
                // read till they disconnect
                while (true) {
                    String line = in.readUTF().trim();
                    if (line.isEmpty()) continue;

                    System.out.println("[" + clientId + "] >> " + line);
                    // figure out what to do with the command
                    String response = processCommand(line);
                    System.out.println("[" + clientId + "] << " + response);

                    // send it back
                    out.writeUTF(response);
                }

            } catch (IOException e) {
                System.out.println("[" + clientId + "] Connection closed: " + e.getMessage());
            } finally {
                // make sure to close the socket when we're done
                try { clientSocket.close(); } catch (IOException ignored) {}
                System.out.println("[" + clientId + "] Session ended.");
            }
        }

        // split up the input and call the right method
        // max 3 parts: command name ip
        private String processCommand(String message) {
            String[] tokens = message.split("\\s+", 3);
            String command = tokens[0].toUpperCase();

            switch (command) {
                case "REGISTER":   return handleRegister(tokens);
                case "RESOLVE":    return handleResolve(tokens);
                case "DEREGISTER": return handleDeregister(tokens);
                default:           return "ERROR: Unknown Command";
            }
        }

        // register a new service
        // returns error if the ip is already taken by someone else
        private String handleRegister(String[] tokens) {
            if (tokens.length < 3) return "ERROR: Bad Syntax - Usage: REGISTER <serviceName> <ipAddress>";

            String serviceName = tokens[1];
            String ipAddress   = tokens[2];

            // lock the map just in case two clients try to register at the exact same time
            synchronized (registry) {
                for (Map.Entry<String, String> entry : registry.entrySet()) {
                    if (entry.getValue().equals(ipAddress) && !entry.getKey().equals(serviceName)) {
                        return "ERROR: IP Already Registered - " + ipAddress + " is used by '" + entry.getKey() + "'";
                    }
                }
                registry.put(serviceName, ipAddress);
            }
            return "OK: Registered '" + serviceName + "' -> " + ipAddress;
        }

        // look up the ip for a name
        private String handleResolve(String[] tokens) {
            if (tokens.length < 2) return "ERROR: Bad Syntax - Usage: RESOLVE <serviceName>";

            String ipAddress = registry.get(tokens[1]);
            return ipAddress == null ? "ERROR: Not Found" : "OK: " + ipAddress;
        }

        // remove a service from the map
        private String handleDeregister(String[] tokens) {
            if (tokens.length < 2) return "ERROR: Bad Syntax - Usage: DEREGISTER <serviceName>";

            String removedIP = registry.remove(tokens[1]);
            return removedIP == null ? "ERROR: Not Found" : "OK: Deregistered '" + tokens[1] + "'";
        }
    }
}
