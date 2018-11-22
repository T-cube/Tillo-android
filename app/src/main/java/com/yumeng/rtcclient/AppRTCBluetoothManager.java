/*
 *  Copyright 2016 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.yumeng.rtcclient;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;


import com.yumeng.util.AppRTCUtils;

import org.webrtc.ThreadUtils;

import java.util.List;
import java.util.Set;

/**
 * AppRTCProximitySensor管理与Bluetoth设备相关的功能。
 * AppRTC演示。
 */
public class AppRTCBluetoothManager {
    private static final String TAG = "AppRTCBluetoothManager";

    //启动或停止音频到蓝牙SCO设备的超时间隔。
    private static final int BLUETOOTH_SCO_TIMEOUT_MS = 4000;
    //SCO连接尝试的最大数量。
    private static final int MAX_SCO_CONNECTION_ATTEMPTS = 2;

    //蓝牙连接状态。
    public enum State {
        //蓝牙是不可用的;没有适配器或蓝牙被关闭。
        UNINITIALIZED,
        //当尝试启动蓝牙时发生蓝牙错误。
        ERROR,
        //耳机配置的蓝牙代理对象存在，但没有连接的耳机设备，
        //SCO没有启动或断开连接。
        HEADSET_UNAVAILABLE,
        //蓝牙代理对象耳机配置连接，连接蓝牙耳机。
        //目前，但SCO尚未启动或断开。
        HEADSET_AVAILABLE,
        //蓝牙音频SCO与远程设备的连接正在关闭。
        SCO_DISCONNECTING,
        //启动与远程设备的蓝牙音频SCO连接。
        SCO_CONNECTING,
        //建立与远程设备的蓝牙音频SCO连接。
        SCO_CONNECTED
    }

    private final Context apprtcContext;
    private final AppRTCAudioManager apprtcAudioManager;
    private final AudioManager audioManager;
    private final Handler handler;

    int scoConnectionAttempts;
    private State bluetoothState;
    private final BluetoothProfile.ServiceListener bluetoothServiceListener;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset bluetoothHeadset;
    private BluetoothDevice bluetoothDevice;
    private final BroadcastReceiver bluetoothHeadsetReceiver;

    // 当蓝牙超时过期时运行。我们在调用后使用超时
    //startScoAudio()或stopScoAudio()因为我们不能保证得到a
    //回调后调用。
    private final Runnable bluetoothTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            bluetoothTimeout();
        }
    };

    /**
     * 接口的实现，当BluetoothProfile IPC客户端连接到或断开服务时，该接口通知客户端。
     */
    private class BluetoothServiceListener implements BluetoothProfile.ServiceListener {
        @Override
        // 当代理对象被连接到服务时调用，以通知客户端。
        // 一旦我们有了profile proxy对象，我们就可以使用它来监视状态
        //连接并执行与耳机配置文件相关的其他操作。
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile != BluetoothProfile.HEADSET || bluetoothState == State.UNINITIALIZED) {
                return;
            }
            Log.d(TAG, "BluetoothServiceListener.onServiceConnected: BT state=" + bluetoothState);
            //Android一次只支持一个蓝牙耳机。
            bluetoothHeadset = (BluetoothHeadset) proxy;
            updateAudioDeviceState();
            Log.d(TAG, "onServiceConnected done: BT state=" + bluetoothState);
        }

        @Override
        /** 当代理对象与服务断开连接时通知客户端。 */
        public void onServiceDisconnected(int profile) {
            if (profile != BluetoothProfile.HEADSET || bluetoothState == State.UNINITIALIZED) {
                return;
            }
            Log.d(TAG, "BluetoothServiceListener.onServiceDisconnected: BT state=" + bluetoothState);
            stopScoAudio();
            bluetoothHeadset = null;
            bluetoothDevice = null;
            bluetoothState = State.HEADSET_UNAVAILABLE;
            updateAudioDeviceState();
            Log.d(TAG, "onServiceDisconnected done: BT state=" + bluetoothState);
        }
    }

    //意图广播接收器，处理改变蓝牙设备的可用性。
    //检测耳机更改和蓝牙SCO状态更改。
    private class BluetoothHeadsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bluetoothState == State.UNINITIALIZED) {
                return;
            }
            final String action = intent.getAction();
            //更改耳机配置文件的连接状态。请注意,
            //变化并不能告诉我们我们是否在流媒体
            //从上海合作组织向英国电信提供音频。通常在用户打开BT时接收
            //使用另一个音频设备激活音频时的耳机。
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                final int state =
                        intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
                Log.d(TAG, "BluetoothHeadsetBroadcastReceiver.onReceive: "
                        + "a=ACTION_CONNECTION_STATE_CHANGED, "
                        + "s=" + stateToString(state) + ", "
                        + "sb=" + isInitialStickyBroadcast() + ", "
                        + "BT state: " + bluetoothState);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    scoConnectionAttempts = 0;
                    updateAudioDeviceState();
                } else if (state == BluetoothHeadset.STATE_CONNECTING) {
                    // No action needed.
                } else if (state == BluetoothHeadset.STATE_DISCONNECTING) {
                    // No action needed.
                } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    //蓝牙很可能在通话过程中被关闭。
                    stopScoAudio();
                    updateAudioDeviceState();
                }
                //更改耳机配置文件的音频(SCO)连接状态。
                //通常在调用startScoAudio()之后收到。
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                final int state = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                Log.d(TAG, "BluetoothHeadsetBroadcastReceiver.onReceive: "
                        + "a=ACTION_AUDIO_STATE_CHANGED, "
                        + "s=" + stateToString(state) + ", "
                        + "sb=" + isInitialStickyBroadcast() + ", "
                        + "BT state: " + bluetoothState);
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    cancelTimer();
                    if (bluetoothState == State.SCO_CONNECTING) {
                        Log.d(TAG, "+++ Bluetooth audio SCO is now connected");
                        bluetoothState = State.SCO_CONNECTED;
                        scoConnectionAttempts = 0;
                        updateAudioDeviceState();
                    } else {
                        Log.w(TAG, "Unexpected state BluetoothHeadset.STATE_AUDIO_CONNECTED");
                    }
                } else if (state == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                    Log.d(TAG, "+++ Bluetooth audio SCO is now connecting...");
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Log.d(TAG, "+++ Bluetooth audio SCO is now disconnected");
                    if (isInitialStickyBroadcast()) {
                        Log.d(TAG, "Ignore STATE_AUDIO_DISCONNECTED initial sticky broadcast.");
                        return;
                    }
                    updateAudioDeviceState();
                }
            }
            Log.d(TAG, "onReceive done: BT state=" + bluetoothState);
        }
    }

    ;

    /**
     * Construction.
     */
    static AppRTCBluetoothManager create(Context context, AppRTCAudioManager audioManager) {
        Log.d(TAG, "create" + AppRTCUtils.getThreadInfo());
        return new AppRTCBluetoothManager(context, audioManager);
    }

    protected AppRTCBluetoothManager(Context context, AppRTCAudioManager audioManager) {
        Log.d(TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        apprtcContext = context;
        apprtcAudioManager = audioManager;
        this.audioManager = getAudioManager(context);
        bluetoothState = State.UNINITIALIZED;
        bluetoothServiceListener = new BluetoothServiceListener();
        bluetoothHeadsetReceiver = new BluetoothHeadsetBroadcastReceiver();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 返回内部状态。
     */
    public State getState() {
        ThreadUtils.checkIsOnMainThread();
        return bluetoothState;
    }

    /**
     * 激活用于检测蓝牙设备和启用的组件
     * BT SCO(音频通过BT SCO传输)用于耳机配置。结束
     * 状态将是HEADSET_UNAVAILABLE，但是状态机已经启动了
     * 将启动一个状态更改序列，最终结果取决于什么?
     * 当启用BT耳机时。
     * 当启动()时，状态更改序列的例子被称为BT设备。
     * 连接和启用:
     * 未初始化——> HEADSET_UNAVAILABLE——> HEADSET_AVAILABLE——>
     * SCO_CONNECTED——> SCO_CONNECTED <= >音频现在通过BT SCO被路由。
     * 请注意，AppRTCAudioManager也参与了这个状态的驱动。
     * 改变。
     */
    public void start() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "start");
        if (!hasPermission(apprtcContext, android.Manifest.permission.BLUETOOTH)) {
            Log.w(TAG, "Process (pid=" + Process.myPid() + ") lacks BLUETOOTH permission");
            return;
        }
        if (bluetoothState != State.UNINITIALIZED) {
            Log.w(TAG, "Invalid BT state");
            return;
        }
        bluetoothHeadset = null;
        bluetoothDevice = null;
        scoConnectionAttempts = 0;
        //获取默认本地蓝牙适配器的句柄。
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Device does not support Bluetooth");
            return;
        }
        //确保该设备支持使用BT SCO音频，用于关闭呼叫用例。
        if (!audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "Bluetooth SCO audio is not available off call");
            return;
        }
        logBluetoothAdapterInfo(bluetoothAdapter);
        // 建立连接到耳机配置文件(包括蓝牙耳机和免提耳机)的代理对象，并安装一个监听器。
        if (!getBluetoothProfileProxy(
                apprtcContext, bluetoothServiceListener, BluetoothProfile.HEADSET)) {
            Log.e(TAG, "BluetoothAdapter.getProfileProxy(HEADSET) failed");
            return;
        }
        //为蓝牙耳机更改通知注册接收器。
        IntentFilter bluetoothHeadsetFilter = new IntentFilter();
        //在耳机配置文件的连接状态下注册接收器。
        bluetoothHeadsetFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        //在耳机配置文件的音频连接状态中注册接收器。
        bluetoothHeadsetFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        registerReceiver(bluetoothHeadsetReceiver, bluetoothHeadsetFilter);
        Log.d(TAG, "HEADSET profile state: "
                + stateToString(bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)));
        Log.d(TAG, "Bluetooth proxy for headset profile has started");
        bluetoothState = State.HEADSET_UNAVAILABLE;
        Log.d(TAG, "start done: BT state=" + bluetoothState);
    }

    /**
     * 停止并关闭与蓝牙音频相关的所有组件。
     */
    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "stop: BT state=" + bluetoothState);
        if (bluetoothAdapter == null) {
            return;
        }
        //如果需要，停止与远程设备的BT SCO连接。
        stopScoAudio();
        //关闭剩余的BT资源。
        if (bluetoothState == State.UNINITIALIZED) {
            return;
        }
        unregisterReceiver(bluetoothHeadsetReceiver);
        cancelTimer();
        if (bluetoothHeadset != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
            bluetoothHeadset = null;
        }
        bluetoothAdapter = null;
        bluetoothDevice = null;
        bluetoothState = State.UNINITIALIZED;
        Log.d(TAG, "stop done: BT state=" + bluetoothState);
    }

    /**
     * 启动与远程设备的蓝牙SCO连接。
     * 请注意，电话应用程序总是优先使用SCO连接
     * 电话。如果在调用该方法时调用该方法，则该方法将被忽略。
     * 同样，如果在应用程序使用SCO连接时接收或发送呼叫，
     * 应用程序的连接将丢失，在调用时不会自动返回
     * 结束。还要注意:直到并且包括API版本JELLY_BEAN_MR1，此方法将启动一个
     * 对蓝牙耳机进行虚拟语音通话。API版本JELLY_BEAN_MR2之后只有一个原始的SCO
     * 音频连接建立。
     * TODO(henrika): should we add support for virtual voice call to BT headset also for JBMR2 and
     * 更高。可能需要发起虚拟语音呼叫，因为许多设备都不需要
     * 接受上海合作组织音频，无需“呼叫”。
     */
    public boolean startScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "startSco: BT state=" + bluetoothState + ", "
                + "attempts: " + scoConnectionAttempts + ", "
                + "SCO is on: " + isScoOn());
        if (scoConnectionAttempts >= MAX_SCO_CONNECTION_ATTEMPTS) {
            Log.e(TAG, "BT SCO connection fails - no more attempts");
            return false;
        }
        if (bluetoothState != State.HEADSET_AVAILABLE) {
            Log.e(TAG, "BT SCO connection fails - no headset available");
            return false;
        }
        //启动BT SCO通道，等待ACTION_AUDIO_STATE_CHANGED。
        Log.d(TAG, "Starting Bluetooth SCO and waits for ACTION_AUDIO_STATE_CHANGED...");
        //上海合作组织建立联系可能需要几秒钟，因此我们不能依赖
        //当方法返回时连接可用，但是注册以接收
        //意图ACTION_SCO_AUDIO_STATE_UPDATED并等待状态为SCO_AUDIO_STATE_CONNECTED。
        bluetoothState = State.SCO_CONNECTING;
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);
        scoConnectionAttempts++;
        startTimer();
        Log.d(TAG, "startScoAudio done: BT state=" + bluetoothState + ", "
                + "SCO is on: " + isScoOn());
        return true;
    }

    /**
     * 停止与远程设备的蓝牙SCO连接。
     */
    public void stopScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "stopScoAudio: BT state=" + bluetoothState + ", "
                + "SCO is on: " + isScoOn());
        if (bluetoothState != State.SCO_CONNECTING && bluetoothState != State.SCO_CONNECTED) {
            return;
        }
        cancelTimer();
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        bluetoothState = State.SCO_DISCONNECTING;
        Log.d(TAG, "stopScoAudio done: BT state=" + bluetoothState + ", "
                + "SCO is on: " + isScoOn());
    }

    /**
     * 使用蓝牙耳机代理对象(控制蓝牙耳机)
     * 通过IPC服务)更新耳机的连接设备列表
     * 概要文件。内部状态将更改为HEADSET_UNAVAILABLE或to
     * HEADSET_AVAILABLE和|蓝牙设备|将被映射到连接。
     * 设备如果可用。
     */
    public void updateDevice() {
        if (bluetoothState == State.UNINITIALIZED || bluetoothHeadset == null) {
            return;
        }
        Log.d(TAG, "updateDevice");
        // 获取耳机配置文件的连接设备。返回的集合
        // 状态为STATE_CONNECTED的设备。BluetoothDevice类
        //只是一个简单的蓝牙硬件地址包装。
        List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
        if (devices.isEmpty()) {
            bluetoothDevice = null;
            bluetoothState = State.HEADSET_UNAVAILABLE;
            Log.d(TAG, "No connected bluetooth headset");
        } else {
            // Always use first device in list. Android only supports one device.
            bluetoothDevice = devices.get(0);
            bluetoothState = State.HEADSET_AVAILABLE;
            Log.d(TAG, "Connected bluetooth headset: "
                    + "name=" + bluetoothDevice.getName() + ", "
                    + "state=" + stateToString(bluetoothHeadset.getConnectionState(bluetoothDevice))
                    + ", SCO audio=" + bluetoothHeadset.isAudioConnected(bluetoothDevice));
        }
        Log.d(TAG, "updateDevice done: BT state=" + bluetoothState);
    }

    /**
     * Stubs for test mocks.
     */
    protected AudioManager getAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    protected void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        apprtcContext.registerReceiver(receiver, filter);
    }

    protected void unregisterReceiver(BroadcastReceiver receiver) {
        apprtcContext.unregisterReceiver(receiver);
    }

    protected boolean getBluetoothProfileProxy(
            Context context, BluetoothProfile.ServiceListener listener, int profile) {
        return bluetoothAdapter.getProfileProxy(context, listener, profile);
    }

    protected boolean hasPermission(Context context, String permission) {
        return apprtcContext.checkPermission(permission, Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 记录本地蓝牙适配器的状态。
     */
    @SuppressLint("HardwareIds")
    protected void logBluetoothAdapterInfo(BluetoothAdapter localAdapter) {
        Log.d(TAG, "BluetoothAdapter: "
                + "enabled=" + localAdapter.isEnabled() + ", "
                + "state=" + stateToString(localAdapter.getState()) + ", "
                + "name=" + localAdapter.getName() + ", "
                + "address=" + localAdapter.getAddress());
        // Log the set of BluetoothDevice objects that are bonded (paired) to the local adapter.
        Set<BluetoothDevice> pairedDevices = localAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            Log.d(TAG, "paired devices:");
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, " name=" + device.getName() + ", address=" + device.getAddress());
            }
        }
    }

    /**
     * 确保音频管理器更新其可用音频设备列表。
     */
    private void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "updateAudioDeviceState");
        apprtcAudioManager.updateAudioDeviceState();
    }

    /**
     * 启动计时器，在BLUETOOTH_SCO_TIMEOUT_MS毫秒后超时。
     */
    private void startTimer() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "startTimer");
        handler.postDelayed(bluetoothTimeoutRunnable, BLUETOOTH_SCO_TIMEOUT_MS);
    }

    /**
     * 取消任何未完成的计时器任务。
     */
    private void cancelTimer() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "cancelTimer");
        handler.removeCallbacks(bluetoothTimeoutRunnable);
    }

    /**
     * 在英国电信上海合作组织频道启动时调用需要很长时间。通常
     * 当BT设备在正在进行的呼叫中打开时发生。
     */
    private void bluetoothTimeout() {
        ThreadUtils.checkIsOnMainThread();
        if (bluetoothState == State.UNINITIALIZED || bluetoothHeadset == null) {
            return;
        }
        Log.d(TAG, "bluetoothTimeout: BT state=" + bluetoothState + ", "
                + "attempts: " + scoConnectionAttempts + ", "
                + "SCO is on: " + isScoOn());
        if (bluetoothState != State.SCO_CONNECTING) {
            return;
        }
        //蓝牙SCO连接;检查最新的结果。
        boolean scoConnected = false;
        List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
        if (devices.size() > 0) {
            bluetoothDevice = devices.get(0);
            if (bluetoothHeadset.isAudioConnected(bluetoothDevice)) {
                Log.d(TAG, "SCO connected with " + bluetoothDevice.getName());
                scoConnected = true;
            } else {
                Log.d(TAG, "SCO is not connected with " + bluetoothDevice.getName());
            }
        }
        if (scoConnected) {
            //我们认为英国电信已经超时了，但它实际上是开着的;更新状态。
            bluetoothState = State.SCO_CONNECTED;
            scoConnectionAttempts = 0;
        } else {
            //放弃和“取消”我们的请求，通过调用stopBluetoothSco()。
            Log.w(TAG, "BT failed to connect after timeout");
            stopScoAudio();
        }
        updateAudioDeviceState();
        Log.d(TAG, "bluetoothTimeout done: BT state=" + bluetoothState);
    }

    /**
     * 检查音频是否使用蓝牙SCO。
     */
    private boolean isScoOn() {
        return audioManager.isBluetoothScoOn();
    }

    /**
     * 将BluetoothAdapter状态转换为本地字符串表示形式。
     */
    private String stateToString(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_DISCONNECTED:
                return "DISCONNECTED";
            case BluetoothAdapter.STATE_CONNECTED:
                return "CONNECTED";
            case BluetoothAdapter.STATE_CONNECTING:
                return "CONNECTING";
            case BluetoothAdapter.STATE_DISCONNECTING:
                return "DISCONNECTING";
            case BluetoothAdapter.STATE_OFF:
                return "OFF";
            case BluetoothAdapter.STATE_ON:
                return "ON";
            case BluetoothAdapter.STATE_TURNING_OFF:
                // 指示本地蓝牙适配器正在关闭。本地客户端应立即尝试优雅地断开任何远程链接。
                return "TURNING_OFF";
            case BluetoothAdapter.STATE_TURNING_ON:
                // 指示本地蓝牙适配器正在打开。然而，本地客户端应该在尝试使用适配器之前等待STATE_ON。
                return "TURNING_ON";
            default:
                return "INVALID";
        }
    }
}
