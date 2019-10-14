package com.example.gs.voicetest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_PERMISSION_FILE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION_FILE);
        }
        findViewById(R.id.tv_demo_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceTest(view.getContext());
            }
        });
    }

    private void startVoiceTest(Context context) {
//        String strYuyin = "kaka,wowoshoukuan,ling,dian,ling,yi,yuan";
        String strYuyin = "kaka,wowoshoukuan,shibai";
        Intent intent = new Intent(Config.ACTION_TTS_PLAYER_SERVICE);
        intent.setPackage(context.getPackageName());
        intent.putExtra("text_test", strYuyin);
        context.startService(intent);
    }
}
