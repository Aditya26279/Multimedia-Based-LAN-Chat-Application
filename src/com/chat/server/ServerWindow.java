package com.chat.server;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class ServerWindow extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private ChatServer server;
    private Thread serverThread;

    public ServerWindow() {
        setTitle("LAN Chat Server Admin");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        redirectSystemOut();
    }

    private void initComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void redirectSystemOut() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }
            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startServer() {
        int port = 8080; // Defaults to 8080 or could add an input field
        server = new ChatServer(port);
        serverThread = new Thread(() -> server.start());
        serverThread.start();
        
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        System.out.println("Chat Server stopped manually.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new ServerWindow().setVisible(true);
        });
    }
}
