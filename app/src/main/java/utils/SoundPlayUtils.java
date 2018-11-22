package utils;

import android.content.Context;
import android.media.*;
import android.media.AudioManager;

import com.yumeng.tillo.R;

public class SoundPlayUtils {
    // SoundPool对象
    public static SoundPool mSoundPlayer = new SoundPool(10,
            AudioManager.STREAM_RING, 5);
    public static SoundPlayUtils soundPlayUtils;
    // 上下文
    static Context mContext;

    /**
     * 初始化
     * @param context
     */
    public static SoundPlayUtils init(Context context) {
        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }
        // 初始化声音
        mContext = context;
        mSoundPlayer.load(mContext, R.raw.message, 1);
        mSoundPlayer.load(mContext, R.raw.qqring, 2);// 1
        return soundPlayUtils;
    }

    /**
     * 播放声音
     */
    public static void play() {
        mSoundPlayer.play(1, 1, 1, 0, 0, 1);
    }

    /**
     * 释放资源
     */
    public static void release(){
        mSoundPlayer.release();
    }

}
