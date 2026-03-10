package com.chat.client;

import javax.sound.sampled.*;

public class AudioPlayer {
    public static void play(byte[] audioData) {
        if (audioData == null || audioData.length == 0) return;
        new Thread(() -> {
            try {
                AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                
                sourceLine.open(audioFormat);
                sourceLine.start();
                
                sourceLine.write(audioData, 0, audioData.length);
                
                sourceLine.drain();
                sourceLine.stop();
                sourceLine.close();
            } catch (LineUnavailableException e) {
                System.err.println("Audio playback line unavailable: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
