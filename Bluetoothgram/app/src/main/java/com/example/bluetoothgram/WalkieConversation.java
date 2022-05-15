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
    private byte Buffer[] = null;
    private byte PlayBuffer[] = null;
    int MIN_BufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private int BufferSize = MIN_BufferSize;
    private boolean RecordingStatus = false;

    public void startRecord() {
        Log.d("VOICE", "Recording voice");
        // the voice will be record in buffer
        Buffer = new byte[BufferSize];

        // record voice
        VoiceRecorder.startRecording();
        RecordingStatus = true;

        // Create a new thread
        RecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendRecord();
            }
        }, "VoiceRecorder Thread");
        RecordThread.start();
    }

    public void sendRecord() {
        while (RecordingStatus) {
            try {
                // read the buffer from 0 byte to the end of buffer
                VoiceRecorder.read(Buffer, 0, BufferSize);
                // write the buffer in outputStream and send out
                outputStream.write(Buffer);
            } catch (IOException e) {
                Log.d("AUDIO", "Fail to send audio");
            }

        }
    }

    public void setupStreams() {
        try {
            // set input stream
            inputStream = WalkieBluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.e("BluetoothSocket", "Fail to create input stream", e);
        }
        try {
            // set output stream
            outputStream = WalkieBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("BluetoothSocket", "Fail to create output stream", e);
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
                AudioFormat.ENCODING_PCM_16BIT, MIN_BufferSize, AudioTrack.MODE_STREAM);
        // Audio record object
        VoiceRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                BufferSize);
    }

    // Playback received audio
    public void startPlay() {
        Log.d("VOICE", "Play voice");
        // Get Buffer from sender
        PlayBuffer = new byte[MIN_BufferSize];
        VoiceTrack.play();
        PlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveAudio();
            }
        }, "AudioTrack Thread");
        // play audio
        PlayThread.start();
    }

    public void receiveAudio() {
        while (!RecordingStatus) {
            try {
                if (inputStream.available() == 0) {
                    // Nothing do because no input stream received
                } else {
                    // receive audio
                    inputStream.read(PlayBuffer);
                    // write the audio to the track for play
                    VoiceTrack.write(PlayBuffer, 0, PlayBuffer.length);
                }
            } catch (IOException e) {
                Log.d("VOICE", "Fail to receive audio");
            }
        }
    }

    public void stopPlay() {
        // if still playing
        if (VoiceTrack != null) {
            RecordingStatus = true;
            // stop playing
            VoiceTrack.stop();
        }
    }

    // release resource
    public void destroyProcesses() {
        VoiceTrack.release();
        VoiceRecorder.release();
    }

    // Set the socket
    public void setSocket(BluetoothSocket WalkieBluetoothSocket) {
        this.WalkieBluetoothSocket = WalkieBluetoothSocket;
    }
}
