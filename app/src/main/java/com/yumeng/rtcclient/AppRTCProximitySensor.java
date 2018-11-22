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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;


import com.yumeng.util.AppRTCUtils;

import org.webrtc.ThreadUtils;

/**
 * AppRTCProximitySensor在AppRTC演示中管理与接近传感器相关的功能。
 * 在大多数设备上，接近传感器被实现为一个boolean-sensor。
 * 它只返回两个值“近”或“远”。阈值是在LUX上完成的
 * 将光传感器的LUX值与阈值进行比较。
 * 超过阈值的LUX-value意味着接近传感器返回“FAR”。
 * 任何小于阈值且传感器返回“接近”的值。
 */
public class AppRTCProximitySensor implements SensorEventListener {
    private static final String TAG = "AppRTCProximitySensor";
    // 这个类应该在一个线程上创建、启动和停止
    //(如主线程)。我们使用|非threadsafe |来确保它是这样的
    // 这个案子。只有当|调试|被设置为true时才会激活。
    private final ThreadUtils.ThreadChecker threadChecker = new ThreadUtils.ThreadChecker();

    private final Runnable onSensorStateListener;
    private final SensorManager sensorManager;
    private Sensor proximitySensor = null;
    private boolean lastStateReportIsNear = false;

    /**
     * Construction
     */
    static AppRTCProximitySensor create(Context context, Runnable sensorStateListener) {
        return new AppRTCProximitySensor(context, sensorStateListener);
    }

    private AppRTCProximitySensor(Context context, Runnable sensorStateListener) {
        Log.d(TAG, "AppRTCProximitySensor" + AppRTCUtils.getThreadInfo());
        onSensorStateListener = sensorStateListener;
        sensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
    }

    /**
     * 激活接近传感器。如果调用。也要进行初始化
     * 已经不是第一次了
     */
    public boolean start() {
        threadChecker.checkIsOnValidThread();
        Log.d(TAG, "start" + AppRTCUtils.getThreadInfo());
        if (!initDefaultSensor()) {
            // Proximity sensor is not supported on this device.
            return false;
        }
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }

    /**
     * 停用接近传感器。
     */
    public void stop() {
        threadChecker.checkIsOnValidThread();
        Log.d(TAG, "stop" + AppRTCUtils.getThreadInfo());
        if (proximitySensor == null) {
            return;
        }
        sensorManager.unregisterListener(this, proximitySensor);
    }

    /**
     * 最后报告状态的Getter。如果“near”被报告，则设置为true。
     */
    public boolean sensorReportsNearState() {
        threadChecker.checkIsOnValidThread();
        return lastStateReportIsNear;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(sensor.getType() == Sensor.TYPE_PROXIMITY);
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "The values returned by this sensor cannot be trusted");
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(event.sensor.getType() == Sensor.TYPE_PROXIMITY);
        // 作为一项最佳实践;在这个方法中尽量少做些什么?
        // 避免阻塞。
        float distanceInCentimeters = event.values[0];
        if (distanceInCentimeters < proximitySensor.getMaximumRange()) {
            Log.d(TAG, "Proximity sensor => NEAR state");
            lastStateReportIsNear = true;
        } else {
            Log.d(TAG, "Proximity sensor => FAR state");
            lastStateReportIsNear = false;
        }

        // 向客户报告新状态。客户机可以调用
        //sensorReportsNearState()查询当前状态(近或远)。
        if (onSensorStateListener != null) {
            onSensorStateListener.run();
        }

        Log.d(TAG, "onSensorChanged" + AppRTCUtils.getThreadInfo() + ": "
                + "accuracy=" + event.accuracy + ", timestamp=" + event.timestamp + ", distance="
                + event.values[0]);
    }

    /**
     * 如果存在，获取默认的接近传感器。平板设备(如Nexus 7)
     * 不支持这种类型的传感器，并且会在这样的传感器中返回false吗
     * 用例。
     */
    private boolean initDefaultSensor() {
        if (proximitySensor != null) {
            return true;
        }
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            return false;
        }
        logProximitySensorInfo();
        return true;
    }

    /**
     * 辅助方法，用于记录有关近距离传感器的信息。
     */
    private void logProximitySensorInfo() {
        if (proximitySensor == null) {
            return;
        }
        StringBuilder info = new StringBuilder("Proximity sensor: ");
        info.append("name=").append(proximitySensor.getName());
        info.append(", vendor: ").append(proximitySensor.getVendor());
        info.append(", power: ").append(proximitySensor.getPower());
        info.append(", resolution: ").append(proximitySensor.getResolution());
        info.append(", max range: ").append(proximitySensor.getMaximumRange());
        info.append(", min delay: ").append(proximitySensor.getMinDelay());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            // Added in API level 20.
            info.append(", type: ").append(proximitySensor.getStringType());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Added in API level 21.
            info.append(", max delay: ").append(proximitySensor.getMaxDelay());
            info.append(", reporting mode: ").append(proximitySensor.getReportingMode());
            info.append(", isWakeUpSensor: ").append(proximitySensor.isWakeUpSensor());
        }
        Log.d(TAG, info.toString());
    }
}
