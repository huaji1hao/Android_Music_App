// CallManager.java
package com.example.cwk_mwe.manager;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.example.cwk_mwe.service.AudioPlayerService;

import java.util.concurrent.Executor;

public class CallManager {
    private final TelephonyManager telephonyManager;
    private final CallStateListener callStateListener;

    public CallManager(Context context, AudioPlayerService audioPlayerService, Executor executor) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        callStateListener = new CallStateListener(audioPlayerService, executor);
    }

    public void register() {
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void unregister() {
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private static class CallStateListener extends PhoneStateListener {
        private final AudioPlayerService audioPlayerService;

        public CallStateListener(AudioPlayerService audioPlayerService, Executor executor) {
            super(executor);
            this.audioPlayerService = audioPlayerService;
        }

        // When the phone is ringing or in a call, pause the audio player
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (audioPlayerService.isPlaying()) {
                        audioPlayerService.pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!audioPlayerService.isPlaying()) {
                        audioPlayerService.play();
                    }
                    break;
            }
        }
    }
}