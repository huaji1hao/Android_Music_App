package com.example.cwk_mwe.player;

public class EnhancedAudiobookPlayer extends AudiobookPlayer {
    private OnCompletionListener onCompletionListener;
    public interface OnCompletionListener {
        void onCompletion();
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.onCompletionListener = listener;
    }

    // When the media player finishes playing the audio file, call the onCompletion method
    @Override
    public void load(String filePath, float speed) {
        super.load(filePath, speed);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion();
                }
            });
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(null);
        }
        super.stop();
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        if (mediaPlayer != null) {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            if (this.state == AudiobookPlayerState.PAUSED) {
                mediaPlayer.pause();
            }
        }
    }
}