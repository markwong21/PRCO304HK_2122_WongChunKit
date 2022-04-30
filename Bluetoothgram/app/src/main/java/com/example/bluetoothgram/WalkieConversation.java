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
    private Thread recordingThread = null;
    private Thread playThread = null;
    private AudioRecord recorder = null;
    private AudioTrack track = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte buffer[] = null;
    private byte playBuffer[] = null;
    int minSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private int bufferSize = minSize;
    private boolean isRecording = false;

    public void startRecord() {
        Log.d("AUDIO", "Assigning recorder");
        buffer = new byte[bufferSize];

        // Start Recording
        recorder.startRecording();
        isRecording = true;
        // Start a thread
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendRecord();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void sendRecord() {
        // Infinite loop until microphone button is released
        while (isRecording) {
            try {
                recorder.read(buffer, 0, bufferSize);
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
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
        }
    }

    @SuppressLint("MissingPermission")
    public void CreateAudio() {
        // Audio track object
        track = new AudioTrack(AudioManager.STREAM_MUSIC,
                16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        // Audio record object
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
    }

    // Playback received audio
    public void startPlay() {
        Log.d("AUDIO", "Assigning player");
        // Receive Buffer
        playBuffer = new byte[minSize];

        track.play();
        // Receive and play audio
        playThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveRecording();
            }
        }, "AudioTrack Thread");
        playThread.start();
    }

    // Receive audio and write into audio track object for playback
    public void receiveRecording() {
        int i = 0;
        while (!isRecording) {
            try {
                if (inputStream.available() == 0) {
                    //Do nothing
                } else {
                    inputStream.read(playBuffer);
                    track.write(playBuffer, 0, playBuffer.length);
                }
            } catch (IOException e) {
                Log.d("AUDIO", "Error when receiving recording");
            }
        }
    }

    // Stop playing and free up resources
    public void stopPlay() {
        if (track != null) {
            isRecording = true;
            track.stop();
        }
    }

    public void destroyProcesses() {
        //Release resources for audio objects
        track.release();
        recorder.release();
    }

    // Setter for socket object
    public void setSocket(BluetoothSocket WalkieBluetoothSocket) {
        this.WalkieBluetoothSocket = WalkieBluetoothSocket;
    }
}
