package com.example.pixelarcade;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private Map<String, Integer> soundMap;
    private MediaPlayer musicPlayer;
    private Context context;
    private SharedPreferences prefs;

    private float volume = 1.0f;
    private boolean isSfxEnabled = true;
    private boolean isMusicEnabled = true;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("PixelArcadePrefs", Context.MODE_PRIVATE);
        
        loadPreferences();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        soundMap = new HashMap<>();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void loadPreferences() {
        int volProgress = prefs.getInt("volume", 70);
        volume = volProgress / 100f;
        isSfxEnabled = prefs.getBoolean("sound_effects", true);
        isMusicEnabled = prefs.getBoolean("music", false);
        
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume, volume);
            if (!isMusicEnabled && musicPlayer.isPlaying()) {
                musicPlayer.pause();
            } else if (isMusicEnabled && !musicPlayer.isPlaying()) {
                musicPlayer.start();
            }
        }
    }

    /**
     * Synthesizes an 8-bit "Beep" sound using Android's ToneGenerator or AudioTrack.
     * This allows us to have sounds without needing external MP3 files.
     */
    public void playSfx(String soundName) {
        if (!isSfxEnabled) return;

        new Thread(() -> {
            switch (soundName) {
                case "click":
                case "swipe":
                    playTone(600, 10, 0.3f); // Very short, low-pitch, quiet "thump"
                    break;
                case "merge":
                    playTone(800, 40, 0.4f); // Subtle rewarding blip
                    break;
                case "game_over":
                    playTone(400, 100, 0.5f);
                    playTone(300, 200, 0.5f);
                    break;
            }
        }).start();
    }

    private void playTone(int freq, int durationMs, float intensity) {
        int sampleRate = 22050;
        int numSamples = durationMs * sampleRate / 1000;
        double[] sample = new double[numSamples];
        byte[] generatedSnd = new byte[2 * numSamples];

        for (int i = 0; i < numSamples; ++i) {
            double progress = (double) i / numSamples;
            double envelope = 1.0 - progress; // Simple linear fade for softness
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freq)) * envelope;
        }

        int idx = 0;
        float finalVolume = volume * intensity; // Apply intensity for subtlety
        for (double dVal : sample) {
            short val = (short) (dVal * 32767 * finalVolume);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        android.media.AudioTrack audioTrack = new android.media.AudioTrack(
                android.media.AudioManager.STREAM_MUSIC,
                sampleRate, android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                android.media.AudioTrack.MODE_STATIC);
        
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
        
        try {
            Thread.sleep(durationMs + 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioTrack.release();
    }

    public void startMusic(int musicResId) {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
        }

        musicPlayer = MediaPlayer.create(context, musicResId);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(volume, volume);
        
        if (isMusicEnabled) {
            musicPlayer.start();
        }
    }

    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (isMusicEnabled && musicPlayer != null && !musicPlayer.isPlaying()) {
            musicPlayer.start();
        }
    }
    
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}
