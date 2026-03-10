package com.chat.client;

import com.chat.common.Message;
import com.chat.common.MessageType;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ChatClient {
    private String serverAddress;
    private int port;
    private String username;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isRunning;
    
    // Callbacks to GUI
    private Consumer<Message> onMessageReceived;
    private Runnable onDisconnectCallback;

    public ChatClient(String serverAddress, int port, String username, Consumer<Message> onMessageReceived, Runnable onDisconnectCallback) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.onMessageReceived = onMessageReceived;
        this.onDisconnectCallback = onDisconnectCallback;
    }

    public void start() throws IOException {
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        isRunning = true;

        // Send CONNECT message
        Message connectMsg = new Message(MessageType.CONNECT, username, "Connected");
        out.writeObject(connectMsg);
        out.flush();

        // Start a thread to listen for incoming messages
        new Thread(new IncomingMessageHandler()).start();
    }

    public void sendMessage(String text) {
        if (!isRunning) return;
        try {
            Message textMsg = new Message(MessageType.TEXT, username, text);
            out.writeObject(textMsg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMultimedia(File file, MessageType type) {
        if (!isRunning || file == null || !file.exists()) return;
        new Thread(() -> {
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                Message msg = new Message(type, username, file.getName(), fileBytes);
                // Synchronize writing to the ObjectOutputStream to prevent interleaved serialization 
                // between different threads (e.g., text vs image sending simultaneously)
                synchronized(out) {
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendAudio(byte[] audioData) {
        if (!isRunning || audioData == null || audioData.length == 0) return;
        new Thread(() -> {
            try {
                Message msg = new Message(MessageType.AUDIO, username, "Voice Message", audioData);
                synchronized(out) {
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void disconnect() {
        if (!isRunning) return;
        try {
            Message disconnectMsg = new Message(MessageType.DISCONNECT, username, "Disconnected");
            if (out != null) {
                synchronized(out) {
                    out.writeObject(disconnectMsg);
                    out.flush();
                }
            }
        } catch (IOException e) {
            // Ignore error gracefully
        } finally {
            close();
        }
    }

    private void close() {
        isRunning = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (onDisconnectCallback != null) {
            onDisconnectCallback.run();
        }
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    Message message = (Message) in.readObject();
                    if (message != null && onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isRunning) {
                    close();
                }
            }
        }
    }
}
