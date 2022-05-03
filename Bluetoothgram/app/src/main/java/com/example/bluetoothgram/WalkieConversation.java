package com.example.bluetoothgram;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WalkieConversation {
    private BluetoothSocket WalkieBluetoothSocket = null;
    private Thread RecordThread = null;
    private Thread PlayThread = null;
    private AudioRecord VoiceRecorder = null;
    private AudioTrack VoiceTrack = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte buffer[] = null;
    private byte PlayBuffer[] = null;
    int MIN_Size = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private int BufferSize = MIN_Size;
    private boolean RecordingStatus = false;

    public void startRecord() {
        Log.d("AUDIO", "Assigning recorder");
        buffer = new byte[BufferSize];

        // Start Recording
        VoiceRecorder.startRecording();
        RecordingStatus = true;
        // Start a thread
        RecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendRecord();
            }
        }, "AudioRecorder Thread");
        RecordThread.start();
    }

    public void sendRecord() {
        // Infinite loop until microphone button is released
        while (RecordingStatus) {
            try {
                VoiceRecorder.read(buffer, 0, BufferSize);
                outputStream.write(buffer);
            } catch (IOException e) {
                Log.d("AUDIO", "Error when sending recording");
            }

        }
    }

    // Set input & output streams
    public void setupStreams() {
        try {
            inputStream = WalkieBluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.e("SOCKET", "Error when creating input stream", e);
        }
        try {
            outputStream = WalkieBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("SOCKET", "Error when creating output stream", e);
        }
    }

    // Stop Recording and free up resources
    public void stopRecord() {
        if (VoiceRecorder != null) {
            RecordingStatus = false;
            VoiceRecorder.stop();
        }
    }

    @SuppressLint("MissingPermission")
    public void CreateAudio() {
        // Audio track object
        VoiceTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, MIN_Size, AudioTrack.MODE_STREAM);
        // Audio record object
        VoiceRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                BufferSize);
    }

    // Playback received audio
    public void startPlay() {
        Log.d("AUDIO", "Assigning player");
        // Receive Buffer
        PlayBuffer = new byte[MIN_Size];

        VoiceTrack.play();
        // Receive and play audio
        PlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveRecording();
            }
        }, "AudioTrack Thread");
        PlayThread.start();
    }

    // Receive audio and write into audio track object for playback
    public void receiveRecording() {
        while (!RecordingStatus) {
            try {
                if (inputStream.available() == 0) {
                    //
                } else {
                    inputStream.read(PlayBuffer);
                    VoiceTrack.write(PlayBuffer, 0, PlayBuffer.length);
                }
            } catch (IOException e) {
                Log.d("AUDIO", "Error when receiving recording");
            }
        }
    }

    // Stop playing and free up resources
    public void stopPlay() {
        if (VoiceTrack != null) {
            RecordingStatus = true;
            VoiceTrack.stop();
        }
    }

    public void destroyProcesses() {
        //Release resources for audio objects
        VoiceTrack.release();
        VoiceRecorder.release();
    }

    // Setter for socket object
    public void setSocket(BluetoothSocket WalkieBluetoothSocket) {
        this.WalkieBluetoothSocket = WalkieBluetoothSocket;
    }
}
