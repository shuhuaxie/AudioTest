package com.example.gs.voicetest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TTSPlayer {

    private List<String> textList;
    private String textToPlay;
    private boolean isSpeaking;
    private OnTTSPlayerListener ttsListener;

    private SoundManager soundManager;

    private static final int MSG_SPEAK_NEXT = 1;
    private static final int MSG_SPEAK_FINISHED = 2;

    private static final int SPEAK_COMPLETE_DELAYED_MILLIS = 500;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SPEAK_NEXT: {
                    invokeSpeakNext();
                    break;
                }
                case MSG_SPEAK_FINISHED: {
                    onSpeakFinished();
                    break;
                }
            }
        }
    };

    public interface OnTTSPlayerListener {
        public void onTTSPlayerFinished();
    }

    public TTSPlayer(Context context) {
        textList = new ArrayList<String>();

        try {
            soundManager = new SoundManager(context);
            soundManager.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    handler.removeMessages(MSG_SPEAK_FINISHED);
                    handler.sendEmptyMessageDelayed(MSG_SPEAK_FINISHED, SPEAK_COMPLETE_DELAYED_MILLIS);
                }
            });
        } catch (Exception e) {
        }
    }

    public void speekAlot(List<String> listData) {
        if (listData == null) {
            return;
        }
        for (String text : listData) {
            if (!TextUtils.isEmpty(text)) {
                textList.add(text);
            }
        }

        if (!isSpeaking) {
            handler.removeMessages(MSG_SPEAK_NEXT);
            handler.sendEmptyMessage(MSG_SPEAK_NEXT);
        }
    }

    public void speekTest(String textTest) {
        if (textTest == null || isSpeaking) {
            return;
        }
        textList.add(textTest);
        if (!isSpeaking) {
            handler.removeMessages(MSG_SPEAK_NEXT);
            handler.sendEmptyMessage(MSG_SPEAK_NEXT);
        }
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    private boolean hasNext() {
        return textList.size() > 0;
    }

    private void invokeSpeakNext() {
        if (textList.size() <= 0 || isSpeaking) {
            return;
        }
        String text = textList.get(0);
        textList.remove(0);
        isSpeaking = true;
        textToPlay = text;
        playTextAfter();
    }

    private void playTextAfter() {
        /**
         if(textToPlay == null) {
         textToPlay = "";
         }
         mSpeechSynthesizer.speak(textToPlay);
         Config.Log("wowo", "TTSPlayer.playTextAfter");
         */
        List<String> mediaSourceList = new ArrayList<String>();
        if (!TextUtils.isEmpty(textToPlay)) {
            String[] textSplit = textToPlay.split(",");
            if (textSplit != null) {
                for (String textToAdd : textSplit) {
                    if (textToAdd != null) {
                        textToAdd = textToAdd.trim();
                    }
                    if (!TextUtils.isEmpty(textToAdd)) {
                        mediaSourceList.add(textToAdd);
                    }
                }
            }
        }
        /**
         String textTTS = Tools.priceToTTS(textToPlay);
         if(!TextUtils.isEmpty(textTTS)) {
         mediaSourceList = Tools.priceMediaSourceByText(textTTS);
         }
         */
        if (mediaSourceList != null
                && mediaSourceList.size() > 0) {
            soundManager.playSeqSound(mediaSourceList);
        } else {
            onSpeakFinished();
        }
    }

    private void onSpeakFinished() {
        isSpeaking = false;
        String broadcast = "1";
        if ("1".equals(broadcast)) {
            if (hasNext()) {
                invokeSpeakNext();
            } else {
                if (ttsListener != null) {
                    ttsListener.onTTSPlayerFinished();
                }
            }
        } else {
            textList.clear();
            if (ttsListener != null) {
                ttsListener.onTTSPlayerFinished();
            }
        }
    }

    public void setOnTTSPlayerListener(OnTTSPlayerListener listener) {
        this.ttsListener = listener;
    }


    public void release() {
        if (soundManager != null) {
            soundManager.stop();
        }
        handler = null;
    }
}