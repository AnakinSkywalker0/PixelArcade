package com.example.pixelarcade;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer nextMediaPlayer;
    private Context context;

    // Sound effect IDs
    private int shootSoundId;
    private int alienFlyingSoundId;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
                
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
                
        // Load short sounds
        shootSoundId = soundPool.load(this.context, R.raw.player_shoot, 1);
        alienFlyingSoundId = soundPool.load(this.context, R.raw.alien_flying, 1);
    }

    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void playShootSound() {
        if (soundPool != null) {
            soundPool.play(shootSoundId, 0.4f, 0.4f, 0, 0, 1.0f);
        }
    }
    
    public void playAlienFlyingSound() {
        if (soundPool != null) {
            soundPool.play(alienFlyingSoundId, 0.6f, 0.6f, 0, 0, 1.0f);
        }
    }

    // Stubs to prevent compilation errors in other minigames (2048, TTT)
    public void playSfx(String sfxName) {
        // Ignored. SoundManager is only used for Galaga.
    }

    public void loadPreferences() {
        // Ignored.
    }

    public void playBackgroundMusic(int resourceId, boolean loop) {
        stopBackgroundMusic();
        backgroundMusicPlayer = MediaPlayer.create(context, resourceId);
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(0.5f, 0.5f);
            if (loop) {
                setupNextMediaPlayer(resourceId);
            } else {
                backgroundMusicPlayer.setLooping(false);
            }
            backgroundMusicPlayer.start();
        }
    }

    private void setupNextMediaPlayer(int resourceId) {
        nextMediaPlayer = MediaPlayer.create(context, resourceId);
        if (nextMediaPlayer != null) {
            nextMediaPlayer.setVolume(0.5f, 0.5f);
            backgroundMusicPlayer.setNextMediaPlayer(nextMediaPlayer);
            backgroundMusicPlayer.setOnCompletionListener(mp -> {
                mp.release();
                backgroundMusicPlayer = nextMediaPlayer;
                setupNextMediaPlayer(resourceId);
            });
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            if (backgroundMusicPlayer.isPlaying()) {
                backgroundMusicPlayer.stop();
            }
            // Clear the completion listener so it doesn't fire when we release
            backgroundMusicPlayer.setOnCompletionListener(null);
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }
    }
    
    public void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
    }

    public void resumeBackgroundMusic() {
        if (backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        stopBackgroundMusic();
        instance = null;
    }
}
