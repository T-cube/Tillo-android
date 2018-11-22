/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.yumeng.rtcclient;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;

/**
 * AppRTCClient是代表AppRTC客户端的接口。
 */
public interface AppRTCClient {
    /**
     * 保持AppRTC机房连接参数的结构。
     */
    class RoomConnectionParameters {
        public final String roomUrl;
        public final String roomId;
        public final boolean loopback;
        public final String urlParameters;

        public RoomConnectionParameters(
                String roomUrl, String roomId, boolean loopback, String urlParameters) {
            this.roomUrl = roomUrl;
            this.roomId = roomId;
            this.loopback = loopback;
            this.urlParameters = urlParameters;
        }

        public RoomConnectionParameters(String roomUrl, String roomId, boolean loopback) {
            this(roomUrl, roomId, loopback, null /* urlParameters */);
        }
    }

    /**
     * 使用提供的连接异步连接到AppRTC房间URL
     * 参数。一旦建立了连接onConnectedToRoom()
     * 调用带有房间参数的回调。
     */
    void connectToRoom(RoomConnectionParameters connectionParameters);

    /**
     * 向其他参与者发送报价SDP。
     */
    void sendOfferSdp(final SessionDescription sdp);

    /**
     * 发送回答SDP给其他参与者。
     */
    void sendAnswerSdp(final SessionDescription sdp);

    /**
     * 将Ice候选对象发送给其他参与者。
     */
    void sendLocalIceCandidate(final IceCandidate candidate);

    /**
     * 将删除的ICE候选对象发送给其他参与者。
     */
    void sendLocalIceCandidateRemovals(final IceCandidate[] candidates);

    /**
     * 脱离房间。
     */
    void disconnectFromRoom();

    /**
     * 设置AppRTC房间的信号参数。
     */
    class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        public final boolean initiator;
        public final String clientId;
        public final String wssUrl;
        public final String wssPostUrl;
        public final SessionDescription offerSdp;
        public final List<IceCandidate> iceCandidates;

        public SignalingParameters(List<PeerConnection.IceServer> iceServers, boolean initiator,
                                   String clientId, String wssUrl, String wssPostUrl, SessionDescription offerSdp,
                                   List<IceCandidate> iceCandidates) {
            this.iceServers = iceServers;
            this.initiator = initiator;
            this.clientId = clientId;
            this.wssUrl = wssUrl;
            this.wssPostUrl = wssPostUrl;
            this.offerSdp = offerSdp;
            this.iceCandidates = iceCandidates;
        }
    }

    /**
     * 用于在信令通道上传递的消息的回调接口。
     * <p>
     * <p>方法保证在|活动|的UI线程上调用。
     */
    interface SignalingEvents {
        /**
         * 一旦房间的信号参数触发回调
         * SignalingParameters提取。
         */
        void onConnectedToRoom(final SignalingParameters params);

        /**
         * 一旦接收到远程SDP，就会触发回调。
         */
        void onRemoteDescription(final SessionDescription sdp);

        /**
         * 一旦收到远程Ice候选程序，就会触发回调。
         */
        void onRemoteIceCandidate(final IceCandidate candidate);

        /**
         * 一旦收到远程Ice候选删除，就会触发回调。
         */
        void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * 当通道关闭时触发回调。
         */
        void onChannelClose();

        /**
         * 一旦发生通道错误，就触发回调。
         */
        void onChannelError(final String description);
    }
}
