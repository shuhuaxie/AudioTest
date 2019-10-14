package com.example.gs.voicetest;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SoundManager {
    private Context context;
    private SoundPool soundPool;
    private SyncMediaPlayer mediaPlayer;
    private HashMap<String, Sound> soundMap = new HashMap<String, Sound>();
    private BlockingQueue<List<String>> mSoundQueue;
    private PlaySoundTask mPlaySoundTask;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;

    public SoundManager(Context context) {
        this.context = context;
        soundPool = new SoundPool(2, 3, 5);
        mediaPlayer = new SyncMediaPlayer();
        mSoundQueue = new ArrayBlockingQueue<List<String>>(10);

        mPlaySoundTask = new PlaySoundTask();
        mPlaySoundTask.start();
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private class PlaySoundTask extends Thread {
        boolean forceQuit = false;

        public void quit() {
            forceQuit = true;
            try {
                interrupt();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            try {
                guardRun();
            } catch (Exception e) {
            }
        }

        private void guardRun() {
            loadSoundList();

            while (true) {
                try {
                    List<String> soundList = mSoundQueue.take();
                    if (soundList != null) {
                        doPlayPinjieSound(soundList);
//                        doPlaySeqSound(soundList);
                    }
                } catch (Exception e) {
                    if (forceQuit) {
                        break;
                    }
                }
            }
        }

        private void doPlayPinjieSound(List<String> soundList) {
            try {
                String rawTargetPath = getRawTargetPath();
                List<String> soundPathList = new ArrayList<String>();
                if (soundList != null) {
                    for (String soundItem : soundList) {
                        soundPathList.add(FileUtils.concatPath(rawTargetPath, soundItem + ".mp3"));
                    }
                }
                String hechengTargetPath = getHechengTargetPath();
                hechengTargetPath = FileUtils.concatPath(hechengTargetPath, "hb_yuyin_" + System.currentTimeMillis() + ".mp3");
                boolean isComplete = CaoZuoMp3Utils.heBingMp3(soundPathList, hechengTargetPath);

                File tmpFile = new File(hechengTargetPath);
                if (isComplete
                        && tmpFile.exists()) {
                    try {
                        mediaPlayer.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setDataSource(hechengTargetPath);
                    mediaPlayer.prepare();
                    mediaPlayer.playSync();
                } else {
                    Log.e("xie", "yuyinStat hecheng failed");
                }

                if (tmpFile.exists()) {
                    tmpFile.delete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(null);
            }
        }

        private void doPlaySeqSound(List<String> soundList) {
            try {
                for (String soundKey : soundList) {
                    boolean playSucced = false;
                    Sound sound = soundMap.get(soundKey);
                    if (sound != null) {
                        playSucced = doPlaySingleSound(sound);
                    }
                    if (!playSucced) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(null);
            }
        }

        private boolean doPlaySingleSound(Sound sound) {
            boolean playSucced = false;
            try {
                if (sound.hasTimed()) {
                    int streamId = soundPool.play(sound.soundId, 1.0f, 1.0f, 1, 0, 1.0f);
                    Thread.sleep(sound.time);
                    soundPool.stop(streamId);
                } else {
                    AssetFileDescriptor afd = null;
                    afd = context.getResources().openRawResourceFd(sound.rawId);
                    if (afd == null) {
                        throw new NullPointerException();
                    }
                    try {
                        mediaPlayer.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setDataSource(afd.getFileDescriptor(),
                            afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.playSync();
                    afd.close();
                    playSucced = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return playSucced;
        }
    }

    public static class SyncMediaPlayer extends MediaPlayer {
        private final ReentrantLock lock;
        private final Condition notEmpty;

        public SyncMediaPlayer() {
            super();

            lock = new ReentrantLock();
            notEmpty = lock.newCondition();

            super.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    notifyPlayComplete();
                }
            });
            super.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    notifyPlayComplete();
                    return true;
                }
            });
        }

        public void playSync() {
            start();
            waitingForPlayComplete();
        }

        private void waitingForPlayComplete() {
            lock.lock();
            try {
                notEmpty.await();
            } catch (Exception e) {
            } finally {
                lock.unlock();
            }
        }

        private void notifyPlayComplete() {
            lock.lock();
            try {
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        }

        @Override
        public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        }
    }

    public void playSeqSound(final List<String> soundList) {
        mSoundQueue.add(soundList);
    }

    public void stop() {
        try {
            if (mPlaySoundTask != null) {
                mPlaySoundTask.quit();
                mPlaySoundTask = null;
            }
        } catch (Exception e) {
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void loadSoundList() {
        if (soundMap.size() <= 0) {
            soundMap.clear();

            addSoundNoTimed("ling");
            addSoundNoTimed("yi");
            addSoundNoTimed("er");
            addSoundNoTimed("san");
            addSoundNoTimed("si");
            addSoundNoTimed("wu");
            addSoundNoTimed("liu");
            addSoundNoTimed("qi");
            addSoundNoTimed("ba");
            addSoundNoTimed("jiu");
            addSoundNoTimed("dian");
            addSoundNoTimed("shi");
            addSoundNoTimed("bai");
            addSoundNoTimed("qian");
            addSoundNoTimed("wan");
            addSoundNoTimed("yii");
            addSoundNoTimed("yuan");

            addSoundNoTimed("kaka");
            addSoundNoTimed("chenggongshoukuan");
            addSoundNoTimed("wowoshoukuan");

            addSoundNoTimed("shibai");
        }

        initialEnvStatic(context);

    }

    private void addSoundNoTimed(String name) {
        Sound sound = Sound.fromNoTimed(ResourceUtil.getRawId(context, name));
        soundMap.put(name, sound);
    }

    private static class Sound {
        public int soundId;
        public int rawId;
        public int time;

        public static Sound fromTimed(int soundId, int time) {
            Sound sound = new Sound();
            sound.soundId = soundId;
            sound.time = time;
            return sound;
        }

        public static Sound fromNoTimed(int rawId) {
            Sound sound = new Sound();
            sound.rawId = rawId;
            sound.time = 0;
            return sound;
        }

        private Sound() {
        }

        public boolean hasTimed() {
            return (time > 0);
        }
    }

    public static void initialEnvStatic(Context context) {
        try {
            String rawTargetPath = getRawTargetPath();
            String hechengTargetPath = getHechengTargetPath();
            makeDir(rawTargetPath);
            makeDir(hechengTargetPath);

            List<CopyRawAttr> copyRawList = new ArrayList<CopyRawAttr>();

            copyRawList.add(new CopyRawAttr(context, "ling"));
            copyRawList.add(new CopyRawAttr(context, "yi"));
            copyRawList.add(new CopyRawAttr(context, "er"));
            copyRawList.add(new CopyRawAttr(context, "san"));
            copyRawList.add(new CopyRawAttr(context, "si"));
            copyRawList.add(new CopyRawAttr(context, "wu"));
            copyRawList.add(new CopyRawAttr(context, "liu"));
            copyRawList.add(new CopyRawAttr(context, "qi"));
            copyRawList.add(new CopyRawAttr(context, "ba"));
            copyRawList.add(new CopyRawAttr(context, "jiu"));
            copyRawList.add(new CopyRawAttr(context, "dian"));
            copyRawList.add(new CopyRawAttr(context, "shi"));
            copyRawList.add(new CopyRawAttr(context, "bai"));
            copyRawList.add(new CopyRawAttr(context, "qian"));
            copyRawList.add(new CopyRawAttr(context, "wan"));
            copyRawList.add(new CopyRawAttr(context, "yii"));
            copyRawList.add(new CopyRawAttr(context, "yuan"));
            copyRawList.add(new CopyRawAttr(context, "kaka"));
            copyRawList.add(new CopyRawAttr(context, "chenggongshoukuan"));
            copyRawList.add(new CopyRawAttr(context, "wowoshoukuan"));
            copyRawList.add(new CopyRawAttr(context, "shibai"));
            for (int i = 0; i < copyRawList.size(); i++) {
                CopyRawAttr rawAttr = copyRawList.get(i);
                String rawPath = concatPath(rawTargetPath, rawAttr.rawName + ".mp3");
                copyFromRawToSdcard(context, false,
                        rawAttr.rawId, rawPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class CopyRawAttr {
        public int rawId;
        public String rawName;

        public CopyRawAttr(Context context, String rawName) {
            this.rawName = rawName;
            this.rawId = ResourceUtil.getRawId(context, rawName);
        }
    }

    private static void copyFromRawToSdcard(Context context, boolean isCover, int rawId, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().openRawResource(rawId);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[4096];
                int size = 0;
                while ((size = is.read(buffer, 0, 4096)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String concatPath(String head, String tail) {
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        if (!head.endsWith(File.separator)) {
            builder.append(File.separator);
        }
        builder.append(tail);
        return builder.toString();
    }

    private static String getRawTargetPath() {
        String rawPath = "";
        try {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            rawPath = sdcardPath + "/wowo/res/raw/";
        } catch (Exception e) {
        }
        return rawPath;
    }

    private static String getHechengTargetPath() {
        String hechengPath = "";
        try {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            hechengPath = sdcardPath + "/wowo/res/hecheng/";
        } catch (Exception e) {
        }
        return hechengPath;
    }

    private static void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}


