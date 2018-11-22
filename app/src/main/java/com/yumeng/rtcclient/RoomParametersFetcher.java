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

import android.util.Log;


import com.yumeng.util.AsyncHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * AsyncTask将AppRTC的房间URL转换为信号的集合。
 * 这个房间的参数。
 */
public class RoomParametersFetcher {
    private static final String TAG = "RoomRTCClient";
    private static final int TURN_HTTP_TIMEOUT_MS = 5000;
    private final RoomParametersFetcherEvents events;
    private final String roomUrl;
    private final String roomMessage;
    private AsyncHttpURLConnection httpConnection;

    /**
     * 房间参数取物的回调。
     */
    public interface RoomParametersFetcherEvents {
        /**
         * 一旦房间的信号参数触发回调
         * SignalingParameters提取。
         */
        void onSignalingParametersReady(final AppRTCClient.SignalingParameters params);

        /**
         * 房间参数提取错误的回调。
         */
        void onSignalingParametersError(final String description);
    }

    public RoomParametersFetcher(
            String roomUrl, String roomMessage, final RoomParametersFetcherEvents events) {
        this.roomUrl = roomUrl;
        this.roomMessage = roomMessage;
        this.events = events;
    }

    public void makeRequest() {
        Log.d(TAG, "Connecting to room: " + roomUrl);
        httpConnection =
                new AsyncHttpURLConnection("POST", roomUrl, roomMessage, new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.e(TAG, "Room connection error: " + errorMessage);
                        events.onSignalingParametersError(errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        roomHttpResponseParse(response);
                    }
                });
        httpConnection.send();
    }

    private void roomHttpResponseParse(String response) {
        Log.d(TAG, "Room response: " + response);
        try {
            LinkedList<IceCandidate> iceCandidates = null;
            SessionDescription offerSdp = null;
            JSONObject roomJson = new JSONObject(response);

            String result = roomJson.getString("result");
            if (!result.equals("SUCCESS")) {
                events.onSignalingParametersError("Room response error: " + result);
                return;
            }
            response = roomJson.getString("params");
            roomJson = new JSONObject(response);
            String roomId = roomJson.getString("room_id");
            String clientId = roomJson.getString("client_id");
            String wssUrl = roomJson.getString("wss_url");
            String wssPostUrl = roomJson.getString("wss_post_url");
            boolean initiator = (roomJson.getBoolean("is_initiator"));
            if (!initiator) {
                iceCandidates = new LinkedList<IceCandidate>();
                String messagesString = roomJson.getString("messages");
                JSONArray messages = new JSONArray(messagesString);
                for (int i = 0; i < messages.length(); ++i) {
                    String messageString = messages.getString(i);
                    JSONObject message = new JSONObject(messageString);
                    String messageType = message.getString("type");
                    Log.d(TAG, "GAE->C #" + i + " : " + messageString);
                    if (messageType.equals("offer")) {
                        offerSdp = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(messageType), message.getString("sdp"));
                    } else if (messageType.equals("candidate")) {
                        IceCandidate candidate = new IceCandidate(
                                message.getString("id"), message.getInt("label"), message.getString("candidate"));
                        iceCandidates.add(candidate);
                    } else {
                        Log.e(TAG, "Unknown message: " + messageString);
                    }
                }
            }
            Log.d(TAG, "RoomId: " + roomId + ". ClientId: " + clientId);
            Log.d(TAG, "Initiator: " + initiator);
            Log.d(TAG, "WSS url: " + wssUrl);
            Log.d(TAG, "WSS POST url: " + wssPostUrl);

            LinkedList<PeerConnection.IceServer> iceServers =
                    iceServersFromPCConfigJSON(roomJson.getString("pc_config"));
            boolean isTurnPresent = false;
            for (PeerConnection.IceServer server : iceServers) {
                Log.d(TAG, "IceServer: " + server);
                if (server.uri.startsWith("turn:")) {
                    isTurnPresent = true;
                    break;
                }
            }
            //请求服务器。
            if (!isTurnPresent && !roomJson.optString("ice_server_url").isEmpty()) {
                LinkedList<PeerConnection.IceServer> turnServers =
                        requestTurnServers(roomJson.getString("ice_server_url"));
                for (PeerConnection.IceServer turnServer : turnServers) {
                    Log.d(TAG, "TurnServer: " + turnServer);
                    iceServers.add(turnServer);
                }
            }
            //iceServers.add(new PeerConnection.IceServer("turn:rtc.tlifang.com:3478", "myvm", "myvm"));
            AppRTCClient.SignalingParameters params = new AppRTCClient.SignalingParameters(
                    iceServers, initiator, clientId, wssUrl, wssPostUrl, offerSdp, iceCandidates);
            events.onSignalingParametersReady(params);
        } catch (JSONException e) {
            events.onSignalingParametersError("Room JSON parsing error: " + e.toString());
        } catch (IOException e) {
            events.onSignalingParametersError("Room IO error: " + e.toString());
        }
    }

    //请求&基于请求URL返回一个TURN ICE服务器。必须运行
    //主线程!
    private LinkedList<PeerConnection.IceServer> requestTurnServers(String url)
            throws IOException, JSONException {
        LinkedList<PeerConnection.IceServer> turnServers = new LinkedList<PeerConnection.IceServer>();
        Log.d(TAG, "Request TURN from: " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("REFERER", "http://119.28.51.83:8080");
        connection.setConnectTimeout(TURN_HTTP_TIMEOUT_MS);
        connection.setReadTimeout(TURN_HTTP_TIMEOUT_MS);
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Non-200 response when requesting TURN server from " + url + " : "
                    + connection.getHeaderField(null));
        }
        InputStream responseStream = connection.getInputStream();
        String response = drainStream(responseStream);
        connection.disconnect();
        Log.d(TAG, "TURN response: " + response);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray iceServers = responseJSON.getJSONArray("iceServers");
        for (int i = 0; i < iceServers.length(); ++i) {
            JSONObject server = iceServers.getJSONObject(i);
            JSONArray turnUrls = server.getJSONArray("urls");
            String username = server.has("username") ? server.getString("username") : "";
            String credential = server.has("credential") ? server.getString("credential") : "";
            for (int j = 0; j < turnUrls.length(); j++) {
                String turnUrl = turnUrls.getString(j);
                turnServers.add(new PeerConnection.IceServer(turnUrl, username, credential));
            }
        }
        return turnServers;
    }

    //返回WebRTCPeerConnection所描述的ICE服务器列表
    //配置字符串。
    private LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(String pcConfig)
            throws JSONException {
        JSONObject json = new JSONObject(pcConfig);
        JSONArray servers = json.getJSONArray("iceServers");
        LinkedList<PeerConnection.IceServer> ret = new LinkedList<PeerConnection.IceServer>();
        for (int i = 0; i < servers.length(); ++i) {
            JSONObject server = servers.getJSONObject(i);
            String url = server.getString("urls");
            String credential = server.has("credential") ? server.getString("credential") : "";
            ret.add(new PeerConnection.IceServer(url, "", credential));
        }
        return ret;
    }

    //将InputStream的内容作为字符串返回。
    private static String drainStream(InputStream in) {
        Scanner s = new Scanner(in).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
