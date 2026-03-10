package com.chat.client;

import com.chat.common.Message;
import com.chat.common.MessageType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class ClientWindow extends JFrame {
    private ChatClient client;
    private String username;

    private JPanel rootPanel;
    private JPanel loginPanel;
    private JPanel chatPanel;

    // Login Fields
    private JTextField serverAddressField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton connectButton;

    // Chat Fields
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JPanel multimediaPanel; 

    // Multimedia Buttons
    private JButton sendImageButton;
    private JButton sendFileButton;
    private JButton recordAudioButton;
    
    // Audio 
    private AudioRecorder audioRecorder;

    public ClientWindow() {
        setTitle("LAN Chat Client");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootPanel = new JPanel(new CardLayout());
        
        initLoginUI();
        initChatUI();

        rootPanel.add(loginPanel, "login");
        rootPanel.add(chatPanel, "chat");

        add(rootPanel);
        ((CardLayout) rootPanel.getLayout()).show(rootPanel, "login");
    }

    private void initLoginUI() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Join LAN Chat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1;
        serverAddressField = new JTextField("localhost", 15);
        loginPanel.add(serverAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("8080", 15);
        loginPanel.add(portField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        connectButton = new JButton("Connect");
        connectButton.setBackground(new Color(0, 122, 255));
        connectButton.setForeground(Color.WHITE);
        loginPanel.add(connectButton, gbc);

        connectButton.addActionListener(e -> connectToServer());
    }

    private void initChatUI() {
        chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setText("<html><head><style>body { font-family: Segoe UI, Arial, sans-serif; font-size: 13px; }</style></head><body><div id='content'></div></body></html>");
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 122, 255));
        sendButton.setForeground(Color.WHITE);

        multimediaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        sendImageButton = new JButton("Send Image");
        sendFileButton = new JButton("Send File");
        recordAudioButton = new JButton("Hold to Record");
        
        multimediaPanel.add(sendImageButton);
        multimediaPanel.add(sendFileButton);
        multimediaPanel.add(recordAudioButton);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(multimediaPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        sendImageButton.addActionListener(e -> selectAndSendFile(MessageType.IMAGE));
        sendFileButton.addActionListener(e -> selectAndSendFile(MessageType.FILE));
        
        recordAudioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (client != null && client.isRunning()) startRecording();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (client != null && client.isRunning()) stopRecordingAndSend();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.disconnect();
                }
            }
        });
    }

    private void connectToServer() {
        String serverAddress = serverAddressField.getText().trim();
        String portStr = portField.getText().trim();
        username = usernameField.getText().trim();

        if (serverAddress.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number.");
            return;
        }

        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");

        new Thread(() -> {
            try {
                client = new ChatClient(serverAddress, port, username, this::onMessageReceived, this::onDisconnect);
                client.start();

                SwingUtilities.invokeLater(() -> {
                    ((CardLayout) rootPanel.getLayout()).show(rootPanel, "chat");
                    setTitle("LAN Chat Client - " + username);
                    messageField.requestFocus();
                    connectButton.setText("Connect");
                    connectButton.setEnabled(true);
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Failed to connect: " + ex.getMessage());
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                });
            }
        }).start();
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && client != null) {
            client.sendMessage(text);
            messageField.setText("");
        }
    }

    private void selectAndSendFile(MessageType type) {
        JFileChooser fileChooser = new JFileChooser();
        if (type == MessageType.IMAGE) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
        }
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            client.sendMultimedia(file, type);
            // Append a local message saying it's uploading
            appendHtmlMessage("System", "#gray", "<i>Sending " + type.name().toLowerCase() + ": " + file.getName() + "...</i>");
        }
    }

    private void startRecording() {
        if (audioRecorder == null) audioRecorder = new AudioRecorder();
        try {
            audioRecorder.startRecording();
            recordAudioButton.setBackground(Color.RED);
            recordAudioButton.setForeground(Color.WHITE);
            recordAudioButton.setText("Recording...");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Microphone unavailable: " + e.getMessage());
        }
    }

    private void stopRecordingAndSend() {
        if (audioRecorder != null) {
            byte[] audioData = audioRecorder.stopRecording();
            recordAudioButton.setBackground(UIManager.getColor("Button.background"));
            recordAudioButton.setForeground(UIManager.getColor("Button.foreground"));
            recordAudioButton.setText("Hold to Record");
            if (audioData.length > 0 && client != null) {
                client.sendAudio(audioData);
                appendHtmlMessage("System", "#gray", "<i>Voice message sent.</i>");
            }
        }
    }

    private void onMessageReceived(Message message) {
        SwingUtilities.invokeLater(() -> {
            String sender = (message.getSender() != null) ? message.getSender() : "Unknown";
            String content = message.getContent() != null ? message.getContent() : "";
            MessageType type = message.getType();
            
            String color = "black";
            boolean isServer = sender.equals("Server");
            boolean isMe = sender.equals(username);
            
            if (isServer) color = "gray";
            else if (isMe) color = "#007AFF";
            else color = "#34C759";
            
            content = content.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
            
            if (type == MessageType.TEXT || type == MessageType.CONNECT || type == MessageType.DISCONNECT) {
                if (isServer) {
                    appendHtmlMessage(sender, color, "<div style='text-align: center; color: " + color + "; font-size: 11px; margin: 5px 0;'><i>" + content + "</i></div>", true);
                } else {
                    String htmlMsg = String.format("<div style='margin-bottom: 5px;'><b style='color: %s'>%s:</b> %s</div>", color, sender, content);
                    appendHtmlMessage(sender, color, htmlMsg, true);
                }
            } else if (type == MessageType.IMAGE) {
                try {
                    // Save image locally to display using file:// protocol
                    String extension = content.contains(".") ? content.substring(content.lastIndexOf(".")) : ".jpg";
                    File tempFile = File.createTempFile("chat_img_" + UUID.randomUUID(), extension);
                    tempFile.deleteOnExit();
                    Files.write(tempFile.toPath(), message.getData(), StandardOpenOption.CREATE);
                    
                    String fileUrl = tempFile.toURI().toURL().toString();
                    String htmlMsg = String.format("<div style='margin-bottom: 5px;'><b style='color: %s'>%s:</b><br><img src='%s' height='150'></div>", color, sender, fileUrl);
                    appendHtmlMessage(sender, color, htmlMsg, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (type == MessageType.FILE) {
                String htmlMsg = String.format("<div style='margin-bottom: 5px;'><b style='color: %s'>%s:</b> sent a file: <b>%s</b></div>", color, sender, content);
                appendHtmlMessage(sender, color, htmlMsg, true);
                
                // Prompt user to save incoming file, skip if I sent it myself
                if (!isMe) {
                    int response = JOptionPane.showConfirmDialog(this, sender + " sent a file: " + content + ".\nDo you want to save it?", "Incoming File", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(content));
                        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                            File saveFile = fileChooser.getSelectedFile();
                            try {
                                Files.write(saveFile.toPath(), message.getData());
                                JOptionPane.showMessageDialog(this, "File saved successfully!");
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(this, "Failed to save file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            } else if (type == MessageType.AUDIO) {
                String htmlMsg = String.format("<div style='margin-bottom: 5px;'><b style='color: %s'>%s:</b> sent a voice message \uD83C\uDFA4 (Autoplaying...)</div>", color, sender);
                appendHtmlMessage(sender, color, htmlMsg, true);
                
                if (!isMe && message.getData() != null) {
                    AudioPlayer.play(message.getData());
                }
            }
        });
    }

    private void appendHtmlMessage(String sender, String color, String rawHtmlMsg) {
        appendHtmlMessage(sender, color, String.format("<div style='margin-bottom: 5px;'><b style='color: %s'>%s:</b> %s</div>", color, sender, rawHtmlMsg), true);
    }
    
    private void appendHtmlMessage(String sender, String color, String rawHtmlMsg, boolean isPreformatted) {
        try {
            HTMLDocument doc = (HTMLDocument) chatArea.getDocument();
            Element element = doc.getElement("content");
            if (element != null) {
                doc.insertBeforeEnd(element, rawHtmlMsg);
            }
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Disconnected from server.");
            ((CardLayout) rootPanel.getLayout()).show(rootPanel, "login");
            setTitle("LAN Chat Client");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new ClientWindow().setVisible(true);
        });
    }
}
