package com.chat.client;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioRecorder {
    private TargetDataLine targetLine;
    private AudioFormat audioFormat;
    private boolean isRecording;
    private ByteArrayOutputStream out;

    public AudioRecorder() {
        // Sample rate: 8000, sample size in bits: 16, channels: 1, signed: true, bigEndian: false
        audioFormat = new AudioFormat(8000.0f, 16, 1, true, false);
    }

    public void startRecording() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported.");
        }
        
        targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(audioFormat);
        targetLine.start();
        
        out = new ByteArrayOutputStream();
        isRecording = true;
        
        Thread recordThread = new Thread(() -> {
            byte[] data = new byte[1024];
            while (isRecording) {
                int readBytes = targetLine.read(data, 0, data.length);
                if (readBytes > 0) {
                    out.write(data, 0, readBytes);
                }
            }
        });
        recordThread.start();
    }

    public byte[] stopRecording() {
        isRecording = false;
        if (targetLine != null) {
            targetLine.stop();
            targetLine.close();
        }
        return out != null ? out.toByteArray() : new byte[0];
    }
}
