package com.chat.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String sender;
    private String content; // For text messages or filenames
    private byte[] data; // For multimedia or file content
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }
    
    public Message(MessageType type, String sender, String content, byte[] data) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.data = data;
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}
