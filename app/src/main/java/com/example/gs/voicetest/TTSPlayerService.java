package com.example.gs.voicetest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;

public class TTSPlayerService extends Service implements
		TTSPlayer.OnTTSPlayerListener {
	
	TTSPlayer ttsPlayer;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		ttsPlayer = new TTSPlayer(this);
		ttsPlayer.setOnTTSPlayerListener(this);
	}
	
	@Override
	public void onDestroy() {
		if(ttsPlayer != null) {
			ttsPlayer.release();
			ttsPlayer = null;
		}
		super.onDestroy();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if(intent != null) {
				ArrayList<String> text_list = intent.getStringArrayListExtra("text_list");
                if(text_list != null) {
                    startWork(text_list);
                }
                String text_test = intent.getStringExtra("text_test");
                if(text_test != null) {
                    startTest(text_test);
                }
			}
		} catch(Exception e) {
		}
        return super.onStartCommand(intent, flags, startId); 
    }
	
	private void startWork(ArrayList<String> textList) {
		ttsPlayer.speekAlot(textList);
	}

    private void startTest(String textTest) {
        ttsPlayer.speekTest(textTest);
    }
	
	@Override
	public void onTTSPlayerFinished() {
		
	}
}
