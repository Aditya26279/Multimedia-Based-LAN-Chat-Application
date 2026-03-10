package com.chat.server;

import com.chat.common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private boolean isRunning;

    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Chat Server started on port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        System.out.println("Client connected: " + username + " (Total active clients: " + clients.size() + ")");
    }

    public synchronized void removeClient(String username) {
        clients.remove(username);
        System.out.println("Client disconnected: " + username + " (Total active clients: " + clients.size() + ")");
    }

    public synchronized void broadcastMessage(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
