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

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


import com.yumeng.util.AsyncHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import bean.UserInfo;
import utils.AppSharePre;

/**
 * 协商与https://appr聊天的信令。tc“room”。
 * 使用apprtc AppEngine webapp的客户端<->服务器细节。
 * <p>
 * 要使用:创建此对象的实例(注册消息处理程序)和
 * 调用connectToRoom()。一旦建立了房间连接
 * 使用房间参数调用onConnectedToRoom()回调。
 * 给对方的信息(与当地的Ice候选人和回答SDP)可以。
 * 在WebSocket连接建立后发送。
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelClient.WebSocketChannelEvents {
    private static final String TAG = "WSRTCClient";
    private static final String ROOM_JOIN = "join";
    private static final String ROOM_MESSAGE = "message";
    private static final String ROOM_LEAVE = "leave";

    private enum ConnectionState {NEW, CONNECTED, CLOSED, ERROR}

    private enum MessageType {MESSAGE, LEAVE}

    private final Handler handler;
    private boolean initiator;
    private SignalingEvents events;
    private WebSocketChannelClient wsClient;
    private ConnectionState roomState;
    private RoomConnectionParameters connectionParameters;
    private String messageUrl;
    private String leaveUrl;
    private UserInfo userInfo;
    public WebSocketRTCClient(SignalingEvents events) {
        userInfo= AppSharePre.getPersonalInfo();
        this.events = events;
        roomState = ConnectionState.NEW;
        final HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    // --------------------------------------------------------------------
    // AppRTCClient接口实现。
    //使用提供的连接异步连接到AppRTC房间URL
    //参数，检索房间参数和连接到WebSocket服务器。
    @Override
    public void connectToRoom(RoomConnectionParameters connectionParameters) {
        Log.e("TAG", "连接到房间");
        this.connectionParameters = connectionParameters;
        handler.post(new Runnable() {
            @Override
            public void run() {
                connectToRoomInternal();
            }
        });
    }

    @Override
    public void disconnectFromRoom() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                disconnectFromRoomInternal();
                handler.getLooper().quit();
            }
        });
    }

    //连接到运行在本地循环线上的房间功能。
    private void connectToRoomInternal() {
        String connectionUrl = getConnectionUrl(connectionParameters);
        Log.d(TAG, "Connect to room: " + connectionUrl);
        roomState = ConnectionState.NEW;
        wsClient = new WebSocketChannelClient(handler, this);

        RoomParametersFetcher.RoomParametersFetcherEvents callbacks = new RoomParametersFetcher.RoomParametersFetcherEvents() {
            @Override
            public void onSignalingParametersReady(final SignalingParameters params) {
                WebSocketRTCClient.this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        WebSocketRTCClient.this.signalingParametersReady(params);
                    }
                });
            }

            @Override
            public void onSignalingParametersError(String description) {
                WebSocketRTCClient.this.reportError(description);
            }
        };

        new RoomParametersFetcher(connectionUrl, null, callbacks).makeRequest();
    }

    //断开与房间的连接并发送bye消息——运行在本地的looper线程上。
    private void disconnectFromRoomInternal() {
        Log.d(TAG, "Disconnect. Room state: " + roomState);
        if (roomState == ConnectionState.CONNECTED) {
            Log.d(TAG, "Closing room.");
            sendPostMessage(MessageType.LEAVE, leaveUrl, null);
        }
        roomState = ConnectionState.CLOSED;
        if (wsClient != null) {
            wsClient.disconnect(true);
        }
    }

    //帮助函数来获取连接、发布消息和留下消息url
    private String getConnectionUrl(RoomConnectionParameters connectionParameters) {
        return connectionParameters.roomUrl + "/" + ROOM_JOIN + "/" + connectionParameters.roomId
                +"/"+userInfo.getId()+ getQueryString(connectionParameters);
    }

    private String getMessageUrl(
            RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
        return connectionParameters.roomUrl + "/" + ROOM_MESSAGE + "/" + connectionParameters.roomId
                + "/" + signalingParameters.clientId + getQueryString(connectionParameters);
    }

    private String getLeaveUrl(
            RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
        return connectionParameters.roomUrl + "/" + ROOM_LEAVE + "/" + connectionParameters.roomId + "/"
                + signalingParameters.clientId + getQueryString(connectionParameters);
    }

    private String getQueryString(RoomConnectionParameters connectionParameters) {
        if (connectionParameters.urlParameters != null) {
            return "?" + connectionParameters.urlParameters;
        } else {
            return "";
        }
    }

    //在提取房间参数时发出回调。运行在本地
    //缝合线。
    private void signalingParametersReady(final SignalingParameters signalingParameters) {
        Log.d(TAG, "Room connection completed.");
        if (connectionParameters.loopback
                && (!signalingParameters.initiator || signalingParameters.offerSdp != null)) {
            reportError("Loopback room is busy.");
            return;
        }
        if (!connectionParameters.loopback && !signalingParameters.initiator
                && signalingParameters.offerSdp == null) {
            Log.w(TAG, "No offer SDP in room response.");
        }
        initiator = signalingParameters.initiator;
        messageUrl = getMessageUrl(connectionParameters, signalingParameters);
        leaveUrl = getLeaveUrl(connectionParameters, signalingParameters);
        Log.d(TAG, "Message URL: " + messageUrl);
        Log.d(TAG, "Leave URL: " + leaveUrl);
        roomState = ConnectionState.CONNECTED;

        //火灾连接和信号参数事件。
        events.onConnectedToRoom(signalingParameters);

        //连接并注册WebSocket客户端。
        wsClient.connect(signalingParameters.wssUrl, signalingParameters.wssPostUrl);
        wsClient.register(connectionParameters.roomId, signalingParameters.clientId);
    }

    //向其他参与者发送本地报价SDP。
    @Override
    public void sendOfferSdp(final SessionDescription sdp) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (roomState != ConnectionState.CONNECTED) {
                    reportError("Sending offer SDP in non connected state.");
                    return;
                }
                JSONObject json = new JSONObject();
                jsonPut(json, "sdp", sdp.description);
                jsonPut(json, "type", "offer");
                sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
                if (connectionParameters.loopback) {
                    // In loopback mode rename this offer to answer and route it back.
                    SessionDescription sdpAnswer = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm("answer"), sdp.description);
                    events.onRemoteDescription(sdpAnswer);
                }
            }
        });
    }

    //向其他参与者发送本地应答SDP。
    @Override
    public void sendAnswerSdp(final SessionDescription sdp) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectionParameters.loopback) {
                    Log.e(TAG, "Sending answer in loopback mode.");
                    return;
                }
                JSONObject json = new JSONObject();
                jsonPut(json, "sdp", sdp.description);
                jsonPut(json, "type", "answer");
                wsClient.send(json.toString());
            }
        });
    }

    //将Ice候选对象发送给其他参与者。
    @Override
    public void sendLocalIceCandidate(final IceCandidate candidate) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                jsonPut(json, "type", "candidate");
                jsonPut(json, "label", candidate.sdpMLineIndex);
                jsonPut(json, "id", candidate.sdpMid);
                jsonPut(json, "candidate", candidate.sdp);
                if (initiator) {
                    //调用发起者向GAE服务器发送ice候选者.
                    if (roomState != ConnectionState.CONNECTED) {
                        reportError("Sending ICE candidate in non connected state.");
                        return;
                    }
                    sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
                    if (connectionParameters.loopback) {
                        events.onRemoteIceCandidate(candidate);
                    }
                } else {
                    //Call receiver将ice候选者发送到websocket服务器。
                    wsClient.send(json.toString());
                }
            }
        });
    }

    //向其他参与者发送删除的Ice候选项。
    @Override
    public void sendLocalIceCandidateRemovals(final IceCandidate[] candidates) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                jsonPut(json, "type", "remove-candidates");
                JSONArray jsonArray = new JSONArray();
                for (final IceCandidate candidate : candidates) {
                    jsonArray.put(toJsonCandidate(candidate));
                }
                jsonPut(json, "candidates", jsonArray);
                if (initiator) {
                    // 调用启动程序将ice候选程序发送到GAE服务器。
                    if (roomState != ConnectionState.CONNECTED) {
                        reportError("Sending ICE candidate removals in non connected state.");
                        return;
                    }
                    sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
                    if (connectionParameters.loopback) {
                        events.onRemoteIceCandidatesRemoved(candidates);
                    }
                } else {
                    //Call receiver将ice候选者发送到websocket服务器。
                    wsClient.send(json.toString());
                }
            }
        });
    }

    // --------------------------------------------------------------------
    //WebSocketChannelEvents接口实现。
    //所有事件都由本地循环线程上的WebSocketChannelClient调用
    //(传递给WebSocket客户端构造函数)。
    @Override
    public void onWebSocketMessage(final String msg) {
        if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
            Log.e(TAG, "Got WebSocket message in non registered state.");
            return;
        }
        try {
            JSONObject json = new JSONObject(msg);
            String msgText = json.getString("msg");
            String errorText = json.optString("error");
            if (msgText.length() > 0) {
                json = new JSONObject(msgText);
                String type = json.optString("type");
                if (type.equals("candidate")) {
                    events.onRemoteIceCandidate(toJavaCandidate(json));
                } else if (type.equals("remove-candidates")) {
                    JSONArray candidateArray = json.getJSONArray("candidates");
                    IceCandidate[] candidates = new IceCandidate[candidateArray.length()];
                    for (int i = 0; i < candidateArray.length(); ++i) {
                        candidates[i] = toJavaCandidate(candidateArray.getJSONObject(i));
                    }
                    events.onRemoteIceCandidatesRemoved(candidates);
                } else if (type.equals("answer")) {
                    if (initiator) {
                        SessionDescription sdp = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type), json.getString("sdp"));
                        events.onRemoteDescription(sdp);
                    } else {
                        reportError("Received answer for call initiator: " + msg);
                    }
                } else if (type.equals("offer")) {
                    if (!initiator) {
                        SessionDescription sdp = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type), json.getString("sdp"));
                        events.onRemoteDescription(sdp);
                    } else {
                        reportError("Received offer for call receiver: " + msg);
                    }
                } else if (type.equals("bye")) {
                    events.onChannelClose();
                } else {
                    reportError("Unexpected WebSocket message: " + msg);
                }
            } else {
                if (errorText != null && errorText.length() > 0) {
                    reportError("WebSocket error message: " + errorText);
                } else {
                    reportError("Unexpected WebSocket message: " + msg);
                }
            }
        } catch (JSONException e) {
            reportError("WebSocket message JSON parsing error: " + e.toString());
        }
    }

    @Override
    public void onWebSocketClose() {
        events.onChannelClose();
    }

    @Override
    public void onWebSocketError(String description) {
        reportError("WebSocket error: " + description);
    }

    // --------------------------------------------------------------------
    //辅助功能。
    private void reportError(final String errorMessage) {
        Log.e(TAG, errorMessage);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (roomState != ConnectionState.ERROR) {
                    roomState = ConnectionState.ERROR;
                    events.onChannelError(errorMessage);
                }
            }
        });
    }

    //在|json|中输入|键|->|值|映射。
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //发送SDP或ICE候选服务器到房间服务器。
    private void sendPostMessage(
            final MessageType messageType, final String url, final String message) {
        String logInfo = url;
        if (message != null) {
            logInfo += ". Message: " + message;
        }
        Log.d(TAG, "C->GAE: " + logInfo);
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("POST", url, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        reportError("GAE POST error: " + errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        if (messageType == MessageType.MESSAGE) {
                            try {
                                JSONObject roomJson = new JSONObject(response);
                                String result = roomJson.getString("result");
                                if (!result.equals("SUCCESS")) {
                                    reportError("GAE POST error: " + result);
                                }
                            } catch (JSONException e) {
                                reportError("GAE POST JSON error: " + e.toString());
                            }
                        }
                    }
                });
        httpConnection.send();
    }

    //将Java候选对象转换为JSONObject。
    private JSONObject toJsonCandidate(final IceCandidate candidate) {
        JSONObject json = new JSONObject();
        jsonPut(json, "label", candidate.sdpMLineIndex);
        jsonPut(json, "id", candidate.sdpMid);
        jsonPut(json, "candidate", candidate.sdp);
        return json;
    }

    //将JSON对象转换为Java对象。
    private IceCandidate toJavaCandidate(JSONObject json) throws JSONException {
        return new IceCandidate(
                json.getString("id"), json.getInt("label"), json.getString("candidate"));
    }
}
