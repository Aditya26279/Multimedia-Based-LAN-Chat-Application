package com.chat.server;

import com.chat.common.Message;
import com.chat.common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // First message expected is CONNECT with username
            Message connectMessage = (Message) in.readObject();
            if (connectMessage != null && connectMessage.getType() == MessageType.CONNECT) {
                this.username = connectMessage.getSender();
                server.addClient(this.username, this);
                server.broadcastMessage(new Message(MessageType.TEXT, "Server", username + " has joined the chat."));
            }

            while (true) {
                Message message = (Message) in.readObject();
                if (message != null) {
                    if (message.getType() == MessageType.DISCONNECT) {
                        break;
                    }
                    // Broadcast message to all clients
                    server.broadcastMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client error or disconnected: " + (username != null ? username : "Unknown"));
        } finally {
            closeConnections();
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnections() {
        if (username != null) {
            server.removeClient(username);
            Message disconnectMsg = new Message(MessageType.TEXT, "Server", username + " has left the chat.");
            server.broadcastMessage(disconnectMsg);
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
