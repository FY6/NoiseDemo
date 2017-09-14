package com.example.manbot.audiodemo;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

public class MainActivity extends Activity {

    static final String TAG = "MainActivity";

    private SpeechRecognizer mIat;
    boolean isRecording = false;//是否录放的标记
    static final int frequency = 16000;//44100;采样率
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize, playBufSize;
    AudioRecord audioRecord;
    AudioTrack audioTrack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIat = SpeechRecognizer.createRecognizer(this, null);

        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);

        playBufSize = AudioTrack.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);


        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, recBufSize * 10);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channelConfiguration, audioEncoding,
                playBufSize, AudioTrack.MODE_STREAM);


    }

    public void lunch(View v) {
        if (!isRecording) {
            setParam();
            new RecordPlayThread().start();// 开一条线程边录边放
            isRecording = true;
        }
    }

    StringBuffer sb = new StringBuffer();
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
        }

        @Override
        public void onBeginOfSpeech() {
            Log.e(TAG, "begin speech ....");
        }

        @Override
        public void onEndOfSpeech() {
            Log.e(TAG, "end speech ....");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.e(TAG, recognizerResult.getResultString() + "-----");
            sb.append(recognizerResult.getResultString());
            if (b) {
                Log.e(TAG, sb.toString());
                sb = new StringBuffer();
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e(TAG, "error...." + speechError.getPlainDescription(true));
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
        }
    };

    public void stop(View v) {
        isRecording = false;
    }

    /**
     * 此处可做：降噪、增益、回音处理
     */
    class RecordPlayThread extends Thread {
        public void run() {

            try {
                byte[] buffer = new byte[recBufSize];
                audioRecord.startRecording();//开始录制
                audioTrack.play();
                mIat.startListening(mRecognizerListener);
                while (isRecording) {
                    //从MIC保存数据到缓冲区
                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);

                    byte[] tmpBuf = new byte[bufferReadResult];
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                    mIat.writeAudio(tmpBuf, 0, tmpBuf.length);
//                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                mIat.stopListening();
                audioRecord.stop();
//                audioTrack.stop();
            } catch (Throwable t) {
            }

        }
    }

    /**
     * 初始化
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "10000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "3000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");//在写音频流方式(-1)下，应用层通过writeAudio函数送入音频；

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");

        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/msc/iat.pcm");
    }

    public void init() {
    }

    public void startListener() {
    }
}
