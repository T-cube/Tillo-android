package utils;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import application.MyApplication;

/**
 * @param
 * @author ldm
 * @description 播放声音工具类
 * @time 2016/6/25 11:29
 */
public class MediaPlayerManager {
    //播放音频API类：MediaPlayer
    private static MediaPlayer mMediaPlayer;
    //是否暂停
    private static boolean isPause;

    /**
     * @param filePath：文件路径 onCompletionListener：播放完成监听
     * @description 播放声音
     * @author ldm
     * @time 2016/6/25 11:30
     */
    public static void playSound(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);

            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                //设置一个error监听器
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                        mMediaPlayer.reset();
                        return false;
                    }
                });
            } else {
                mMediaPlayer.reset();
            }
            mMediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(fis.getFD());
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (Exception e) {
            Log.e("IllegalStateException", e.getMessage());
            Log.e("IllegalStateException", filePath);
        }
    }

    /**
     * @param
     * @description 暂停播放
     * @author ldm
     * @time 2016/6/25 11:31
     */
    public static void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) { //正在播放的时候
            mMediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     * @param
     * @description 重新播放
     * @author ldm
     * @time 2016/6/25 11:31
     */
    public static void resume() {
        if (mMediaPlayer != null && isPause) {
            mMediaPlayer.start();
            isPause = false;
        }
    }

    /**
     * @param
     * @description 释放操作
     * @author ldm
     * @time 2016/6/25 11:32
     */
    public static void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public static boolean isPlaying() {
        return mMediaPlayer == null ? false : mMediaPlayer.isPlaying();
    }

    public static boolean isPause() {
        if (mMediaPlayer != null && isPause)
            return true;
        else
            return false;
    }

    public static void stop() {
        if (mMediaPlayer != null) {//mediaplayer 是MediaPlayer的 instance
            mMediaPlayer.stop();
//            try {
//                mMediaPlayer.prepare();//stop后下次重新播放要首先进入prepared状态
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

}