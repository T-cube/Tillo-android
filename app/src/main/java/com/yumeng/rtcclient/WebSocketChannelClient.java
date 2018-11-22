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

import android.os.Handler;
import android.util.Log;


import com.yumeng.util.AsyncHttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * WebSocket client implementation.
 * <p>
 * <p>所有公共方法都应该从looper executor线程调用。
 * 在构造函数中传递，否则将抛出异常。
 * 所有事件都被分派到同一个线程上。
 */

public class WebSocketChannelClient {
    private static final String TAG = "WSChannelRTCClient";
    private static final int CLOSE_TIMEOUT = 1000;
    private final WebSocketChannelEvents events;
    private final Handler handler;
    private WebSocketConnection ws;
    private WebSocketObserver wsObserver;
    private String wsServerUrl;
    private String postServerUrl;
    private String roomID;
    private String clientID;
    private WebSocketConnectionState state;
    private final Object closeEventLock = new Object();
    private boolean closeEvent;
    // WebSocket发送队列。当WebSocket时，消息被添加到队列中。
    //客户端未注册，并在register()调用中使用。
    private final LinkedList<String> wsSendQueue;

    /**
     * 可能的WebSocket连接状态。
     */
    public enum WebSocketConnectionState {
        NEW, CONNECTED, REGISTERED, CLOSED, ERROR
    }

    /**
     * WebSocket中传递的消息的回调接口。
     * 所有事件都由执行线程分派。
     */
    public interface WebSocketChannelEvents {
        void onWebSocketMessage(final String message);

        void onWebSocketClose();

        void onWebSocketError(final String description);
    }

    public WebSocketChannelClient(Handler handler, WebSocketChannelEvents events) {
        this.handler = handler;
        this.events = events;
        roomID = null;
        clientID = null;
        wsSendQueue = new LinkedList<String>();
        state = WebSocketConnectionState.NEW;
    }

    public WebSocketConnectionState getState() {
        return state;
    }

    public void connect(final String wsUrl, final String postUrl) {
        Log.e("TAG", "connect WebSocketConnection");
        checkIfCalledOnValidThread();
        if (state != WebSocketConnectionState.NEW) {
            Log.e(TAG, "WebSocket is already connected.");
            return;
        }
        wsServerUrl = wsUrl;
        postServerUrl = postUrl;
        closeEvent = false;

        Log.d(TAG, "Connecting WebSocket to: " + wsUrl + ". Post URL: " + postUrl);
        ws = new WebSocketConnection();
        wsObserver = new WebSocketObserver();
        try {
            ws.connect(new URI(wsServerUrl), wsObserver);
        } catch (URISyntaxException e) {
            reportError("URI error: " + e.getMessage());
        } catch (WebSocketException e) {
            reportError("WebSocket connection error: " + e.getMessage());
        }
    }

    public void register(final String roomID, final String clientID) {
        checkIfCalledOnValidThread();
        this.roomID = roomID;
        this.clientID = clientID;
        if (state != WebSocketConnectionState.CONNECTED) {
            Log.w(TAG, "WebSocket register() in state " + state);
            return;
        }
        Log.d(TAG, "Registering WebSocket for room " + roomID + ". ClientID: " + clientID);
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "register");
            json.put("roomid", roomID);
            json.put("clientid", clientID);
            Log.d(TAG, "C->WSS: " + json.toString());
            ws.sendTextMessage(json.toString());
            state = WebSocketConnectionState.REGISTERED;
            // Send any previously accumulated messages.
            for (String sendMessage : wsSendQueue) {
                send(sendMessage);
            }
            wsSendQueue.clear();
        } catch (JSONException e) {
            reportError("WebSocket register JSON error: " + e.getMessage());
        }
    }

    public void send(String message) {
        checkIfCalledOnValidThread();
        switch (state) {
            case NEW:
            case CONNECTED:
                //存储传出消息并在websocket客户端之后发送它们
                //登记。
                Log.d(TAG, "WS ACC: " + message);
                wsSendQueue.add(message);
                return;
            case ERROR:
            case CLOSED:
                Log.e(TAG, "WebSocket send() in error or closed state : " + message);
                return;
            case REGISTERED:
                JSONObject json = new JSONObject();
                try {
                    json.put("cmd", "send");
                    json.put("msg", message);
                    message = json.toString();
                    Log.d(TAG, "C->WSS: " + message);
                    ws.sendTextMessage(message);
                } catch (JSONException e) {
                    reportError("WebSocket send JSON error: " + e.getMessage());
                }
                break;
        }
    }

    //此调用可用于在WebSocket之前发送WebSocket消息。
    //连接打开。
    public void post(String message) {
        checkIfCalledOnValidThread();
        sendWSSMessage("POST", message);
    }

    public void disconnect(boolean waitForComplete) {
        checkIfCalledOnValidThread();
        Log.d(TAG, "Disconnect WebSocket. State: " + state);
        if (state == WebSocketConnectionState.REGISTERED) {
            // 发送“bye”到WebSocket服务器。
            send("{\"type\": \"bye\"}");
            state = WebSocketConnectionState.CONNECTED;
            //发送http DELETE到http WebSocket服务器。
//            sendWSSMessage("DELETE", "");
        }
        //仅在连接或错误状态下关闭WebSocket。
        if (state == WebSocketConnectionState.CONNECTED || state == WebSocketConnectionState.ERROR) {
            ws.disconnect();
            state = WebSocketConnectionState.CLOSED;

            //等待websocket关闭事件来阻止websocket库
            //将任何挂起的消息发送到已删除的循环线程。
            if (waitForComplete) {
                synchronized (closeEventLock) {
                    while (!closeEvent) {
                        try {
                            closeEventLock.wait(CLOSE_TIMEOUT);
                            break;
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Wait error: " + e.toString());
                        }
                    }
                }
            }
        }
        Log.d(TAG, "Disconnecting WebSocket done.");
    }

    private void reportError(final String errorMessage) {
        Log.e(TAG, errorMessage);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (state != WebSocketConnectionState.ERROR) {
                    state = WebSocketConnectionState.ERROR;
                    events.onWebSocketError(errorMessage);
                }
            }
        });
    }

    //异步发送POST/DELETE到WebSocket服务器。
    private void sendWSSMessage(final String method, final String message) {
        String postUrl = postServerUrl + "/" + roomID + "/" + clientID;
        Log.d(TAG, "WS " + method + " : " + postUrl + " : " + message);
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection(method, postUrl, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        reportError("WS " + method + " error: " + errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                    }
                });
        httpConnection.send();
    }

    // 用于调试的助手方法。确保WebSocket方法是
    //调用一个活线。
    private void checkIfCalledOnValidThread() {
        if (Thread.currentThread() != handler.getLooper().getThread()) {
            throw new IllegalStateException("WebSocket method is not called on valid thread");
        }
    }

    private class WebSocketObserver implements WebSocketConnectionObserver {
        @Override
        public void onOpen() {
            Log.d(TAG, "WebSocket connection opened to: " + wsServerUrl);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    state = WebSocketConnectionState.CONNECTED;
                    // Check if we have pending register request.
                    if (roomID != null && clientID != null) {
                        register(roomID, clientID);
                    }
                }
            });
        }

        @Override
        public void onClose(WebSocketCloseNotification code, String reason) {
            Log.d(TAG, "WebSocket connection closed. Code: " + code + ". Reason: " + reason + ". State: "
                    + state);
            synchronized (closeEventLock) {
                closeEvent = true;
                closeEventLock.notify();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (state != WebSocketConnectionState.CLOSED) {
                        state = WebSocketConnectionState.CLOSED;
                        events.onWebSocketClose();
                    }
                }
            });
        }

        @Override
        public void onTextMessage(String payload) {
            Log.d(TAG, "WSS->C: " + payload);
            final String message = payload;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (state == WebSocketConnectionState.CONNECTED
                            || state == WebSocketConnectionState.REGISTERED) {
                        events.onWebSocketMessage(message);
                    }
                }
            });
        }

        @Override
        public void onRawTextMessage(byte[] payload) {
        }

        @Override
        public void onBinaryMessage(byte[] payload) {
        }
    }
}
