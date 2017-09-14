package com.example.manbot.audiodemo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.InputStream;

/**
 * 播放 PCM 格式 音频
 */
public class AudioManagment {
    private Context mContext;
    private static AudioManagment manager;
    private AudioManager audioManager;
    private AudioTrack at;
    private int bufferSizeInBytes;


    private AudioManagment(Context context) {
        this.mContext = context;
        init();
    }

    public static AudioManagment getAudioManagerInstance(Context context) {
        if (null == manager) {
            synchronized (AudioManagment.class) {
                if (null == manager) {
                    manager = new AudioManagment(context);
                }
            }
        }
        return manager;
    }

    private void init() {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        //获取当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //获取系统的最大音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        Log.e("TAG", "current volume: " + currentVolume + "  , " + "max volume:  " + maxVolume);


        bufferSizeInBytes = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        at = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
    }

    public void play(String fileName) {
        if (at == null) {
            at = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        }

        if (at != null && at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            at.stop();
            run(fileName);
        } else if (at != null && at.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            run(fileName);
        }
    }

    public void release() {
        if (at != null && at.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            if (at != null) {
                at.release();
            }
        } else {
            if (at != null) {
                at.flush();
                at.stop();
                at.release();
            }
        }
        at = null;
        manager = null;
    }

    public void run(String path) {

        int resId = -1;

        switch (path) {
            case "net_connecting":
                resId = R.raw.net_connecting;
                break;
            case "net_connected_success":
                resId = R.raw.net_connected_success;
                break;
            case "net_connected_failed":
                resId = R.raw.net_connected_failed;
                break;
            case "net_not":
                resId = R.raw.net_not;
                break;
            case "system_start_failed":
                resId = R.raw.system_start_failed;
                break;
            case "system_starting":
                resId = R.raw.system_starting;
                break;
            case "system_starting_succese":
                resId = R.raw.system_starting_succese;
                break;
            case "try_reconnection":
                resId = R.raw.try_reconnection;
                break;
            case "tip_connect":
                resId = R.raw.tip_connect;
                break;
        }

        Log.e("tag", "" + resId);

        InputStream is = mContext.getResources().openRawResource(resId);
        byte[] out_bytes = new byte[16000];

        int length;
        try {
            if (at != null) {
                at.play();
            }
            while ((length = is.read(out_bytes)) != -1) {
                at.write(out_bytes, 0, length);//该方法会阻塞
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //播放完成
        if (at != null && at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            try {
                if (audioListener != null)
                    audioListener.onCpmpleted();
                release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private AudioListener audioListener;

    public AudioManagment addListener(AudioListener audioListener) {
        this.audioListener = audioListener;
        return manager;
    }

    public interface AudioListener {
        void onCpmpleted();
    }
}
