/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.yumeng.rtcclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;


import com.yumeng.tillo.R;
import com.yumeng.util.AppRTCUtils;

import org.webrtc.ThreadUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AppRTCAudioManager manages all audio related parts of the AppRTC demo.
 * 音频管理类
 */
public class AppRTCAudioManager {
    private static final String TAG = "AppRTCAudioManager";
    private static final String SPEAKERPHONE_AUTO = "auto";
    private static final String SPEAKERPHONE_TRUE = "true";
    private static final String SPEAKERPHONE_FALSE = "false";

    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     * 表示当前可能的音频设备的名称
     * 支持
     */
    public enum AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE
    }

    /**
     * AudioManager state.
     * 状态
     */
    public enum AudioManagerState {
        UNINITIALIZED,
        PREINITIALIZED,
        RUNNING,
    }

    /**
     * 一旦音频设备发生更改或可用音频设备列表发生更改，将触发回调。
     */
    public static interface AudioManagerEvents {
        // Callback fired once audio device is changed or list of available audio devices changed.
        void onAudioDeviceChanged(
                AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }

    private final Context apprtcContext;
    private AudioManager audioManager;
    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState amState;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private boolean hasWiredHeadset = false;

    // 默认的音频设备;可视电话或音频耳机
    // 只有电话。
    private AudioDevice defaultAudioDevice;

    //包含当前选定的音频设备。
    //这个装置是用某种方案自动改变的。
    //有线耳机比扬声器电话“赢”。a也有可能
    //用户显式地选择一个设备(并删除任何预定义的方案)。
    //详情见| userSelectedAudioDevice |。
    private AudioDevice selectedAudioDevice;

    // 包含用户选择的音频设备，该设备覆盖预定义的内容
    //选择方案。
    // TODO(henrika): always set to AudioDevice.NONE today. Add support for
    // explicit selection based on choice by userSelectedAudioDevice.
    private AudioDevice userSelectedAudioDevice;

    // 包含扬声器设置
    private final String useSpeakerphone;

    // 接近传感器对象。它测量了cm中一个物体的接近程度。
    //相对于设备的视图屏幕，因此可以用于
    //协助设备切换(靠近耳朵<=>使用耳机耳机耳机听筒
    //              可用，远离耳朵<=>使用扬声器电话)。
    private AppRTCProximitySensor proximitySensor = null;

    //处理与蓝牙耳机相关的所有任务。
    private final AppRTCBluetoothManager bluetoothManager;

    // 包含可用音频设备的列表。集合被用来
    //避免重复元素。
    private Set<AudioDevice> audioDevices = new HashSet<AudioDevice>();
    //有线耳机意图广播接收器。
    private BroadcastReceiver wiredHeadsetReceiver;

    //用于改变音频焦点的回调方法。
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    /**
     * 当接近传感器报告状态变化时，调用此方法，
     * from“NEAR to FAR”或“FAR to NEAR”。
     */
    private void onProximitySensorChangedState() {
        if (!useSpeakerphone.equals(SPEAKERPHONE_AUTO)) {
            return;
        }

        // 接近传感器只有当恰好有两个时才会被激活
        // 可用的音频设备。
        if (audioDevices.size() == 2 && audioDevices.contains(AppRTCAudioManager.AudioDevice.EARPIECE)
                && audioDevices.contains(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)) {
            if (proximitySensor.sensorReportsNearState()) {
                //传感器报告说，“手机被放在一个人的耳朵上”，
                //或者“有东西覆盖在光传感器上”。
                setAudioDeviceInternal(AppRTCAudioManager.AudioDevice.EARPIECE);
            } else {
                //传感器报告称，“手机被从人的耳朵中移除”。
                //“光传感器不再覆盖”。
                setAudioDeviceInternal(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            }
        }
    }

    /* 用于处理有线耳机可用性变化的接收器。 */
    private class WiredHeadsetReceiver extends BroadcastReceiver {
        private static final int STATE_UNPLUGGED = 0;
        private static final int STATE_PLUGGED = 1;
        private static final int HAS_NO_MIC = 0;
        private static final int HAS_MIC = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", STATE_UNPLUGGED);
            int microphone = intent.getIntExtra("microphone", HAS_NO_MIC);
            String name = intent.getStringExtra("name");
            Log.d(TAG, "WiredHeadsetReceiver.onReceive" + AppRTCUtils.getThreadInfo() + ": "
                    + "a=" + intent.getAction() + ", s="
                    + (state == STATE_UNPLUGGED ? "unplugged" : "plugged") + ", m="
                    + (microphone == HAS_MIC ? "mic" : "no mic") + ", n=" + name + ", sb="
                    + isInitialStickyBroadcast());
            hasWiredHeadset = (state == STATE_PLUGGED);
            updateAudioDeviceState();
        }
    }

    ;

    /**
     * 建设。
     */
   public static AppRTCAudioManager create(Context context) {
        return new AppRTCAudioManager(context);
    }

    private AppRTCAudioManager(Context context) {
        Log.d(TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        apprtcContext = context;
        audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        bluetoothManager = AppRTCBluetoothManager.create(context, this);
        wiredHeadsetReceiver = new WiredHeadsetReceiver();
        amState = AudioManagerState.UNINITIALIZED;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        useSpeakerphone = sharedPreferences.getString(context.getString(R.string.pref_speakerphone_key),
                context.getString(R.string.pref_speakerphone_default));
        Log.d(TAG, "useSpeakerphone: " + useSpeakerphone);
        if (useSpeakerphone.equals(SPEAKERPHONE_FALSE)) {
            defaultAudioDevice = AudioDevice.EARPIECE;
        } else {
            defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        }

        //创建并初始化接近传感器。
        //平板设备(如Nexus 7)不支持接近传感器。
        //注意，在调用start()之前，传感器不会处于活动状态
        proximitySensor = AppRTCProximitySensor.create(context, new Runnable() {
            // This method will be called each time a state change is detected.
            // Example: user holds his hand over the device (closer than ~5 cm),
            // or removes his hand from the device.
            public void run() {
                onProximitySensorChangedState();
            }
        });

        Log.d(TAG, "defaultAudioDevice: " + defaultAudioDevice);
        AppRTCUtils.logDeviceInfo(TAG);
    }

    public void start(AudioManagerEvents audioManagerEvents) {
        Log.d(TAG, "start");
        ThreadUtils.checkIsOnMainThread();
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active");
            return;
        }
        // TODO(henrika): 如果没有初始化，可以在这里调用名为preInitAudio()的新方法。

        Log.d(TAG, "AudioManager starts...");
        this.audioManagerEvents = audioManagerEvents;
        amState = AudioManagerState.RUNNING;

        //存储当前音频状态，以便在调用stop()时恢复它。
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadset();

        //创建一个AudioManager。OnAudioFocusChangeListener实例。
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            // Called on the listener to notify if the audio focus for this listener has been changed.
            // The |focusChange| value indicates whether the focus was gained, whether the focus was lost,
            // and whether that loss is transient, or whether the new focus holder will hold it for an
            // unknown amount of time.
            // TODO(henrika): possibly extend support of handling audio-focus changes. Only contains
            // logging for now.
            @Override
            public void onAudioFocusChange(int focusChange) {
                String typeOfChange = "AUDIOFOCUS_NOT_DEFINED";
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        typeOfChange = "AUDIOFOCUS_GAIN";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        typeOfChange = "AUDIOFOCUS_LOSS";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                        break;
                    default:
                        typeOfChange = "AUDIOFOCUS_INVALID";
                        break;
                }
                Log.d(TAG, "onAudioFocusChange: " + typeOfChange);
            }
        };

        //请求音频播放播放焦点(不要隐藏)，并安装监听器以更改焦点。
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(TAG, "Audio focus request failed");
        }

        //首先将MODE_IN_COMMUNICATION设置为默认音频模式。它是
        //当播放和/或录音开始时，需要处于这种模式。
        //最好的网络性能。
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false);

        //设置初始设备状态。
        userSelectedAudioDevice = AudioDevice.NONE;
        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();

        //初始化和启动蓝牙，如果一个BT设备可用或启动。
        //检测新(启用)BT设备。
        bluetoothManager.start();

        //做音频设备的初始选择。稍后可以更改此设置
        //通过增加/移除一个BT耳机或有线耳机或通过覆盖/揭露
        //接近传感器。
        updateAudioDeviceState();

        //为与添加/删除a有关的广播意图注册接收器
        //有线耳机。
        registerReceiver(wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        Log.d(TAG, "AudioManager started");
    }

    public void stop() {
        Log.d(TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (amState != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: " + amState);
            return;
        }
        amState = AudioManagerState.UNINITIALIZED;

        unregisterReceiver(wiredHeadsetReceiver);

        bluetoothManager.stop();

        //恢复先前存储的音频状态。
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);

        //放弃音频的焦点。给出先前的焦点所有者(如果有的话)的焦点。
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        audioFocusChangeListener = null;
        Log.d(TAG, "Abandoned audio focus for VOICE_CALL streams");

        if (proximitySensor != null) {
            proximitySensor.stop();
            proximitySensor = null;
        }

        audioManagerEvents = null;
        Log.d(TAG, "AudioManager stopped");
    }

    /**
     * 更改当前活动音频设备的选择。
     */
    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(TAG, "setAudioDeviceInternal(device=" + device + ")");
        AppRTCUtils.assertIsTrue(audioDevices.contains(device));

        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                break;
            case BLUETOOTH:
                setSpeakerphoneOn(false);
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
        selectedAudioDevice = device;
    }

    /**
     * 更改默认的音频设备。
     * TODO(henrika):在AppRTCMobile客户端中添加此方法的用法。
     */
    public void setDefaultAudioDevice(AudioDevice defaultDevice) {
        ThreadUtils.checkIsOnMainThread();
        switch (defaultDevice) {
            case SPEAKER_PHONE:
                defaultAudioDevice = defaultDevice;
                break;
            case EARPIECE:
                if (hasEarpiece()) {
                    defaultAudioDevice = defaultDevice;
                } else {
                    defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
                }
                break;
            default:
                Log.e(TAG, "Invalid default audio device selection");
                break;
        }
        Log.d(TAG, "setDefaultAudioDevice(device=" + defaultAudioDevice + ")");
        updateAudioDeviceState();
    }

    /**
     * 更改当前活动音频设备的选择。
     */
    public void selectAudioDevice(AudioDevice device) {
        ThreadUtils.checkIsOnMainThread();
        if (!audioDevices.contains(device)) {
            Log.e(TAG, "Can not select " + device + " from available " + audioDevices);
        }
        userSelectedAudioDevice = device;
        updateAudioDeviceState();
    }

    /**
     * 返回当前可用的/可选择的音频设备集。
     */
    public Set<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet<AudioDevice>(audioDevices));
    }

    /**
     * 返回当前选定的音频设备。
     */
    public AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return selectedAudioDevice;
    }

    /**
     * 接收器注册的辅助方法。
     */
    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        apprtcContext.registerReceiver(receiver, filter);
    }

    /**
     * 辅助方法，用于取消现有接收器的注册。
     */
    private void unregisterReceiver(BroadcastReceiver receiver) {
        apprtcContext.unregisterReceiver(receiver);
    }

    /**
     * 设置扬声器电话模式。
     */
    public void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }

    /**
     * 设置麦克风静音状态
     */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }

    /**
     * 获取当前的earpiece状态。
     */
    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * 检查有线耳机是否连接。
     * 这并不是音频播放实际上已经结束的有效标志
     * 有线耳机作为音频路由取决于其他条件。我们
     * 只将它用作附件的早期指示符(初始化期间)
     * 有线耳机。
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 更新可能的音频设备列表并选择新的设备。
     * TODO(henrika): 添加单元测试以验证所有状态转换。
     */
    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "--- updateAudioDeviceState: "
                + "wired headset=" + hasWiredHeadset + ", "
                + "BT state=" + bluetoothManager.getState());
        Log.d(TAG, "Device status: "
                + "available=" + audioDevices + ", "
                + "selected=" + selectedAudioDevice + ", "
                + "user selected=" + userSelectedAudioDevice);

        // 检查是否连接了蓝牙耳机。内部BT状态会
        //相应地改变。
        // TODO(henrika): perhaps wrap required state into BT manager.
        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_UNAVAILABLE
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_DISCONNECTING) {
            bluetoothManager.updateDevice();
        }

        //更新可用的音频设备集。
        Set<AudioDevice> newAudioDevices = new HashSet<>();

        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE) {
            newAudioDevices.add(AudioDevice.BLUETOOTH);
        }

        if (hasWiredHeadset) {
            //如果连接了有线耳机，那么这是唯一的选择。
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            //没有有线耳机，因此音频设备列表可以包含扬声器
            //电话(在平板电脑上)，或扬声器电话和耳机(在移动电话上)。
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE);
            }
        }
        // 如果设备列表已经更改，则将其设置为true的存储状态。
        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);
        //更新现有的音频设备集。
        audioDevices = newAudioDevices;
        //如果需要，纠正用户选择的音频设备。
        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_UNAVAILABLE
                && userSelectedAudioDevice == AudioDevice.BLUETOOTH) {
            //如果BT不能使用，它不能是用户选择。
            userSelectedAudioDevice = AudioDevice.NONE;
        }
        if (hasWiredHeadset && userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            // 如果用户选择了扬声器电话，然后插上有线耳机再制作
            //有线耳机作为用户选择的设备。
            userSelectedAudioDevice = AudioDevice.WIRED_HEADSET;
        }
        if (!hasWiredHeadset && userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            //如果用户选择有线耳机，则先不插有线耳机再制作
            //扬声器电话作为用户选择的设备。
            userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE;
        }

        //如果可用，需要启动蓝牙，用户可以显式地选择它，或者用户没有选择任何输出设备。
        boolean needBluetoothAudioStart =
                bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
                        && (userSelectedAudioDevice == AudioDevice.NONE
                        || userSelectedAudioDevice == AudioDevice.BLUETOOTH);

        //如果用户选择了不同的设备，需要停止蓝牙音频
        //建立或进行蓝牙SCO连接。
        boolean needBluetoothAudioStop =
                (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED
                        || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING)
                        && (userSelectedAudioDevice != AudioDevice.NONE
                        && userSelectedAudioDevice != AudioDevice.BLUETOOTH);

        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING
                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED) {
            Log.d(TAG, "Need BT audio: start=" + needBluetoothAudioStart + ", "
                    + "stop=" + needBluetoothAudioStop + ", "
                    + "BT state=" + bluetoothManager.getState());
        }

        //启动或停止蓝牙SCO连接。
        if (needBluetoothAudioStop) {
            bluetoothManager.stopScoAudio();
            bluetoothManager.updateDevice();
        }

        if (needBluetoothAudioStart && !needBluetoothAudioStop) {
            //尝试启动Bluetooth SCO音频(启动需要几秒钟)。
            if (!bluetoothManager.startScoAudio()) {
                //自SCO失败后，从可用设备列表中删除蓝牙。
                audioDevices.remove(AudioDevice.BLUETOOTH);
                audioDeviceSetUpdated = true;
            }
        }

        //更新选中的音频设备。
        AudioDevice newAudioDevice = selectedAudioDevice;

        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED) {
            //如果连接了蓝牙，那么它应该被用作输出音频
            //设备。请注意，仅使用耳机是不够的;
            //一个活跃的SCO频道也必须启动和运行。
            newAudioDevice = AudioDevice.BLUETOOTH;
        } else if (hasWiredHeadset) {
            //如果连接了有线耳机，但蓝牙没有连接，那么有线耳机就被用作音频设备。
            newAudioDevice = AudioDevice.WIRED_HEADSET;
        } else {
            // 没有有线耳机和蓝牙，因此音频设备列表可以包含扬声器电话(在平板电脑上)，
            // 扬声器电话和耳机(在移动电话上)。| defaultAudioDevice | AudioDevice包含。
            // SPEAKER_PHONE或AudioDevice。这取决于用户的选择。
            newAudioDevice = defaultAudioDevice;
        }
        //切换到新的设备，但只有当有任何变化。
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            setAudioDeviceInternal(newAudioDevice);
            Log.d(TAG, "New device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + newAudioDevice);
            if (audioManagerEvents != null) {
                //通知监听客户，音频设备已被更改。
                audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState done");
    }
}
