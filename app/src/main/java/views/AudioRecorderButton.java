package views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.R;
import com.yumeng.tillo.UpLoadImageActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;

import bean.UserInfo;
import utils.AppSharePre;
import utils.AudioManager;
import utils.DialogManager;
import utils.fileutil.FileUtils;

/**
 * 自定义按钮 实现录音等功能
 * Created by Administrator on 2017/11/28.
 */

public class AudioRecorderButton extends android.support.v7.widget.AppCompatButton {
    // 按钮正常状态（默认状态）
    private static final int STATE_NORMAL = 1;
    //正在录音状态
    private static final int STATE_RECORDING = 2;
    //录音取消状态
    private static final int STATE_CANCEL = 3;
    //记录当前状态
    private int mCurrentState = STATE_NORMAL;
    //是否开始录音标志
    private boolean isRecording = false;
    //判断在Button上滑动距离，以判断 是否取消
    private static final int DISTANCE_Y_CANCEL = 100;
    //对话框管理工具类
    private DialogManager mDialogManager;
    //录音管理工具类
    private AudioManager mAudioManager;
    //记录录音时间
    private float mTime;
    // 是否触发longClick
    private boolean mReady;
    //录音准备
    private static final int MSG_AUDIO_PREPARED = 0x110;
    //音量发生改变
    private static final int MSG_VOICE_CHANGED = 0x111;
    //取消提示对话框
    private static final int MSG_DIALOG_DIMISS = 0x112;
    private RxPermissions rxPermissions;
    private boolean isRequest = false;
    /**
     * @description 获取音量大小的线程
     * @author ldm
     * @time 2016/6/25 9:30
     * @param
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        public void run() {
            while (isRecording) {//判断正在录音
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;//录音时间计算
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);//每0.1秒发送消息
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    //显示对话框
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    // 开启一个线程计算录音时间
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    //更新声音
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    //取消对话框
                    mDialogManager.dimissDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        rxPermissions = new RxPermissions((Activity) context);
        mDialogManager = new DialogManager(context);
        //录音文件存放地址
        UserInfo userInfo = AppSharePre.getPersonalInfo();
        String fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        mAudioManager = AudioManager.getInstance(fileForderPath);
        mAudioManager.setOnAudioStateListener(new AudioManager.AudioStateListener() {
            public void wellPrepared() {
                mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
            }
        });

        // 由于这个类是button所以在构造方法中添加监听事件
        setOnLongClickListener(new OnLongClickListener() {

            public boolean onLongClick(View v) {
                if (isRequest) {
                    mReady = true;
                    mAudioManager.prepareAudio();
                }
                return false;
            }
        });
    }

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    /**
     * @param
     * @author ldm
     * @description 录音完成后的回调
     * @time 2016/6/25 11:18
     */
    public interface AudioFinishRecorderCallBack {
        void onFinish(float seconds, String filePath);
    }

    private AudioFinishRecorderCallBack finishRecorderCallBack;

    public void setFinishRecorderCallBack(AudioFinishRecorderCallBack listener) {
        finishRecorderCallBack = listener;
    }

    /**
     * @param
     * @description 处理Button的OnTouchEvent事件
     * @author ldm
     * @time 2016/6/25 9:35
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取TouchEvent状态
        int action = event.getAction();
        // 获得x轴坐标
        int x = (int) event.getX();
        // 获得y轴坐标
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN://手指按下
                rxPermissions.request(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                isRequest = true;
                                changeState(STATE_RECORDING);
                            } else
                                isRequest = false;
                        });
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                if (isRecording) {
                    //根据x,y的坐标判断是否需要取消
                    if (wantToCancle(x, y)) {
                        changeState(STATE_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }

                break;
            case MotionEvent.ACTION_UP://手指放开
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime < 1.0f) {//如果时间少于1s，则提示录音过短
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    // 延迟显示对话框
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);
                } else if (mCurrentState == STATE_RECORDING) {
                    //如果状态为正在录音，则结束录制
                    mDialogManager.dimissDialog();
                    mAudioManager.release();

                    if (finishRecorderCallBack != null) {
                        finishRecorderCallBack.onFinish(mTime, mAudioManager.getCurrentFilePath());
                    }

                } else if (mCurrentState == STATE_CANCEL) { // 想要取消
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mTime = 0;
        mReady = false;
        changeState(STATE_NORMAL);
    }

    private boolean wantToCancle(int x, int y) {
        // 超过按钮的宽度
//        if (x < 0 || x > getWidth()) {
//            return true;
//        }
        // 超过按钮的高度
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }


    /**
     * @param
     * @description 根据状态改变Button显示
     * @author ldm
     * @time 2016/6/25 9:36
     */
    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (state) {
                case STATE_NORMAL:
//                    setBackgroundResource(R.drawable.btn_recorder_normal);
//                    setText(R.string.str_recorder_normal);
                    break;

                case STATE_RECORDING:
//                    setBackgroundResource(R.drawable.btn_recorder_recording);
//                    setText(R.string.str_recorder_recording);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;

                case STATE_CANCEL:
//                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    mDialogManager.wantToCancel();
//                    setText(R.string.str_recorder_want_cancel);
                    break;
            }
        }
    }
}
