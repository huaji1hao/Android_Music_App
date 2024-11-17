package com.example.cwk_mwe.utils;

public class EnhancedAudiobookPlayer extends AudiobookPlayer {

    private OnCompletionListener onCompletionListener;

    public interface OnCompletionListener {
        void onCompletion();
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.onCompletionListener = listener;
    }

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
}