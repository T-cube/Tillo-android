/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */


package mpush;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.yumeng.tillo.BuildConfig;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.GroupChatActivity;
import com.yumeng.tillo.VideoCallActivity;
import com.yumeng.tillo.VoiceCallActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import application.MyApplication;
import bean.ChatMessage;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import receiver.NetWorkStateReceiver;
import service.PomeloService;
import utils.AppSharePre;
import utils.SharedDataTool;
import utils.SoundPlayUtils;
import utils.fileutil.FileUtils;

/**
 * Created by yxx on 2016/2/13.
 *
 * @author ohun@live.cn
 */
public final class MPushService extends Service implements ClientListener {
    public static final String ACTION_MESSAGE_RECEIVED = "com.mpush.MESSAGE_RECEIVED";
    public static final String ACTION_NOTIFICATION_OPENED = "com.mpush.NOTIFICATION_OPENED";
    public static final String ACTION_KICK_USER = "com.mpush.KICK_USER";
    public static final String ACTION_CONNECTIVITY_CHANGE = "com.mpush.CONNECTIVITY_CHANGE";
    public static final String ACTION_HANDSHAKE_OK = "com.mpush.HANDSHAKE_OK";
    public static final String ACTION_BIND_USER = "com.mpush.BIND_USER";
    public static final String ACTION_UNBIND_USER = "com.mpush.UNBIND_USER";
    public static final String EXTRA_PUSH_MESSAGE = "push_message";
    public static final String EXTRA_PUSH_MESSAGE_ID = "push_message_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_DEVICE_ID = "device_id";
    public static final String EXTRA_BIND_RET = "bind_ret";
    public static final String EXTRA_CONNECT_STATE = "connect_state";
    public static final String EXTRA_HEARTBEAT = "heartbeat";
    private int SERVICE_START_DELAYED = 5;

    public boolean isConnected = true;
    NetWorkStateReceiver netWorkStateReceiver;
    private UserInfo userInfo;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cancelAutoStartService(this);
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!MPush.I.hasStarted()) {
            MPush.I.checkInit(this).create(this);
        }
        if (MPush.I.hasStarted()) {
            if (MPushReceiver.hasNetwork(this)) {
                MPush.I.client.start();
            }
            MPushFakeService.startForeground(this);
            flags = START_STICKY;
            SERVICE_START_DELAYED = 5;
            return super.onStartCommand(intent, flags, startId);
        } else {
            int ret = super.onStartCommand(intent, flags, startId);
            stopSelf();
            SERVICE_START_DELAYED += SERVICE_START_DELAYED;
            return ret;
        }
    }

    public void init() {
        registerNetWorkChangedReceiver();
        EventBus.getDefault().register(this);
        userInfo = AppSharePre.getPersonalInfo();
        initEventMessage();
        SoundPlayUtils.init(getApplicationContext());
    }


    //注册网络监听广播
    public void registerNetWorkChangedReceiver() {
        netWorkStateReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);

    }

    //初始化
    public void initEventMessage() {
        //初始化消息界面
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_INIT_CONVERSATION, null));
        //初始化群聊消息界面
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_FRAGMENT, Constants.MESSAGE_EVENT_UPDATE_GROUP_SESSION, null));
        //消息离线记录
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_OFFLINE_MESSAGE, null));
        //群消息离线记录
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_GROUP_OFFLINE_MESSAGE, null));
        //更新好友列表
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
    }

    /**
     * service停掉后自动启动应用
     *
     * @param context
     * @param delayed 延后启动的时间，单位为秒
     */
    private static void startServiceAfterClosed(Context context, int delayed) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayed * 1000, getOperation(context));
    }

    public static void cancelAutoStartService(Context context) {
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getOperation(context));
    }

    private static PendingIntent getOperation(Context context) {
        Intent intent = new Intent(context, MPushService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return operation;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MPushReceiver.cancelAlarm(this);
        MPush.I.destroy();
        unregisterReceiver(netWorkStateReceiver);
        EventBus.getDefault().unregister(this);
        SoundPlayUtils.release();
        startServiceAfterClosed(this, SERVICE_START_DELAYED);//5s后重启
    }


    @Override
    public void onReceivePush(Client client, byte[] content, int messageId) {
        String message = new String(content, com.mpush.api.Constants.UTF_8);
        Log.e("TAG_receive", "message:" + message);
        JSONObject jsonObject = JSON.parseObject(message);
        saveMessageToDatabase(jsonObject);
//        sendBroadcast(new Intent(ACTION_MESSAGE_RECEIVED)
//                .addCategory(BuildConfig.APPLICATION_ID)
//                .putExtra(EXTRA_PUSH_MESSAGE, content)
//                .putExtra(EXTRA_PUSH_MESSAGE_ID, messageId)
//        );
    }

    @Override
    public void onKickUser(String deviceId, String userId) {
        MPush.I.unbindAccount();
        sendBroadcast(new Intent(ACTION_KICK_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onBind(boolean success, String userId) {
        Log.e("TAG_bind", "绑定用户");
        sendBroadcast(new Intent(ACTION_BIND_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_BIND_RET, success)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onUnbind(boolean success, String userId) {
        Log.e("TAG_unbind", "解除绑定");
        sendBroadcast(new Intent(ACTION_UNBIND_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_BIND_RET, success)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onConnected(Client client) {
        Log.e("TAG_connnect", "连接成功");
        sendBroadcast(new Intent(ACTION_CONNECTIVITY_CHANGE)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_CONNECT_STATE, true)
        );
    }

    @Override
    public void onDisConnected(Client client) {
        Log.e("TAG_disconnnect", "断开连接");
        MPushReceiver.cancelAlarm(this);
        sendBroadcast(new Intent(ACTION_CONNECTIVITY_CHANGE)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_CONNECT_STATE, false)
        );
    }

    @Override
    public void onHandshakeOk(Client client, int heartbeat) {
        MPushReceiver.startAlarm(this, heartbeat - 1000);
        sendBroadcast(new Intent(ACTION_HANDSHAKE_OK)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_HEARTBEAT, heartbeat)
        );
    }


    /**
     * 网络环境监听
     */
    class NetWorkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //检测API是不是小于21，因为到了API21之后getNetworkInfo(int networkType)方法被弃用
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取ConnectivityManager对象对应的NetworkInfo对象
                //获取WIFI连接的信息
                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //获取移动数据连接的信息
                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (!wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                    //没有网络
                    isConnected = false;
                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_DISCONNECT_ERROR, null));
                    SharedDataTool.setBoolean(context, "network", false);
                } else {
                    SharedDataTool.setBoolean(context, "network", true);
                    //判断是否是断网后重新连接
                    if (isConnected) {
                        //不做操作
                        isConnected = true;
                    } else {
                        //重新请求
//                        if (client != null)
//                            client.disconnect();
//                        initClient();
//                        initEventMessage();
                        //通知界面拉取离线消息

                        isConnected = true;
                    }

                }
//API大于23时使用下面的方式进行网络监听
            } else {
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取所有网络连接的信息
                Network[] networks = connMgr.getAllNetworks();
                //用于存放网络连接信息
                StringBuilder sb = new StringBuilder();
//            //通过循环将网络信息逐个取出来
//            for (int i = 0; i < networks.length; i++) {
//                //获取ConnectivityManager对象对应的NetworkInfo对象
//                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
//                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
//            }
                //获取wifi链接的信息
                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //获取移动数据连接的信息
                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (!wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                    //没有网络
                    isConnected = false;
                    SharedDataTool.setBoolean(context, "network", false);
                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_DISCONNECT_ERROR, null));
                } else {
                    SharedDataTool.setBoolean(context, "network", true);
                    //判断是否是断网后重新连接
                    if (isConnected) {
                        //不做操作
                        isConnected = true;
                    } else {
                        //重新请求
//                        if (client != null)
//                            client.disconnect();
//                        initClient();
//                        initEventMessage();
                        isConnected = true;
                    }
                }

            }
        }
    }


    /**
     * EventBus消息接收
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventReceiver(MessageEvent messageEvent) {
        String target = messageEvent.getTarget();
        String behavior = messageEvent.getBehavior();
        if (target.equals(Constants.TARGET_SERVICE)) {
            if (behavior.equals(Constants.MESSAGE_INIT_CLIENT)) {
//                if (client != null)
//                    client.disconnect();
//                initClient();
            }
        }
    }

    //保存消息
    public void saveMessageToDatabase(com.alibaba.fastjson.JSONObject jsonObject) {
        com.alibaba.fastjson.JSONObject dataJSON = JSON.parseObject(jsonObject.getString("content"));
        String route = dataJSON.getString("route");
        if (route.equals("addFriend")) {
            //通知通讯录界面
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_NEW_FRIEND_REQUEST, null));
            return;
        } else if (route.equals("passFriend")) {
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
            return;
        } else if (route.equals("groupInvite")) {
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_GROUP_INVITE, null));
            return;
        } else if (route.equals("rtc")) {
            String requestType = dataJSON.getString("type");
            String mediaType = dataJSON.getString("mediaType");
            String status = dataJSON.getString("status");
            String senderId = dataJSON.getString("senderId");
            String receiverId = dataJSON.getString("receiverId");
            if (requestType.equals("call")) {
                if (mediaType.equals("audio")) {
                    //语音
                    if (status != null) {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VOICECALL_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
                    } else {
                        Intent voiceCallIntent = new Intent(getApplicationContext(), VoiceCallActivity.class);
                        voiceCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        voiceCallIntent.putExtra("message", senderId);
                        voiceCallIntent.putExtra("type", 2);//接受者传2
                        startActivity(voiceCallIntent);
                    }

                } else if (mediaType.equals("video")) {
                    //视频
                    if (status != null) {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VIDEOCALL_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
                    } else {
                        Intent voiceCallIntent = new Intent(getApplicationContext(), VideoCallActivity.class);
                        voiceCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        voiceCallIntent.putExtra("message", senderId);
                        voiceCallIntent.putExtra("type", 2);//接受者传2
                        startActivity(voiceCallIntent);
                    }

                }

            } else if (requestType.equals("callback")) {
                if (mediaType.equals("audio")) {
                    if (status.equals("true")) {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VOICE_ACTIVITY, Constants.MESSAGE_EVENT_JOIN_VOICE, null));
                    } else {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VOICE_ACTIVITY, Constants.MESSAGE_EVENT_FINISH_VOICE, null));
                    }

                } else if (mediaType.equals("video")) {
                    //视频
                    if (status.equals("true")) {
                        //点击接受
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VIDEO_ACTIVITY, Constants.MESSAGE_EVENT_JOIN_VOICE, null));
                    } else {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VIDEO_ACTIVITY, Constants.MESSAGE_EVENT_FINISH_VOICE, null));
                    }

                }
            }

        } else {
            String sender = dataJSON.getString("sender");
            if (sender.equals(userInfo.getId())) {
                return;
            }
            ChatMessage item = new ChatMessage();
            item.setType(dataJSON.getString("type"));
            switch (dataJSON.getString("type")) {
                case "text":
                    item.setContent(dataJSON.getString("content"));
                    break;
                case "image":
                    item.setContent("...[图片]");
                    item.setSourceId(dataJSON.getString("sourceId"));
                    break;
                case "file":
                    item.setContent("...[文件]");
                    item.setSourceId(dataJSON.getString("sourceId"));
                    break;
                case "audio":
                    item.setContent("...[语音]");
                    item.setSourceId(dataJSON.getString("sourceId"));
                    item.setDuration(dataJSON.getString("duration"));
                    item.setFileName(dataJSON.getString("fileName"));
                    item.setSendState(0);
                    break;
                case "video":
                    item.setContent("...[视频]");
                    break;

            }
            item.setSender(dataJSON.getString("sender"));
            if (route.equals("singleChat"))
                item.setReceiver(dataJSON.getString("receiver"));
            item.setMessageId(dataJSON.getString("messageId"));
            item.setTimestamp(dataJSON.getLong("timestamp"));
            item.setRoomid(dataJSON.getString("roomId"));
            item.setBackId(dataJSON.getString("backId"));
            item.save();
            Log.e("TAG_messageString", item.messageToString());
            if (dataJSON.getString("type").equals("audio")) {
                //下载音视频文件到本地
                downloadAudioFile(item.getSourceId(), item, item.getFileName());
            }
            //给对话界面、聊天界面分发消息
            if (route.equals("singleChat"))
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
            else if (route.equals("groupChat"))
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_GROUP_HAS_MESSAGE, item));
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
        }
    }

    //下载音频文件
    public void downloadAudioFile(String url, final ChatMessage message, String fileName) {
        String fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        Activity currentActivity = MyApplication.getCurrentActivity();
        OkGo.<File>get(Constants.FileUrl + Constants.downloadFile)
                .headers("Authorization", userInfo.getToken())
                .params("sourceId", url)
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        Log.e("TAG_file", "file Succeess!");
                        File file = response.body();
                        //保存文件
                        ChatMessage item = new ChatMessage();
                        item.setLocal_path(file.getAbsolutePath());
                        item.setSourceId(message.getSourceId());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setReceiver(message.getReceiver());
                        item.setSender(message.getSender());
                        item.setMessageId(message.getMessageId());
                        item.setDuration(message.getDuration());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        item.setFileName(fileName);
                        item.setBackId(message.getBackId());
                        item.setSendState(1);
//                        if (type.equals("group"))
//                            item.setGroup(message.getGroup());
                        if (message.getReceiver() != null) {
                            if (currentActivity instanceof ChatActivity) {
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_AUDIO, item));
                            }
                        } else {
                            if (currentActivity instanceof GroupChatActivity)
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_GROUP_AUDIO, item));
                        }
                        item.saveOrUpdate("messageid=?", item.getMessageId());
                    }

                    @Override
                    public void onError(Response<File> response) {
                        ChatMessage item = new ChatMessage();
                        item.setSourceId(message.getSourceId());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setReceiver(message.getReceiver());
                        item.setSender(message.getSender());
                        item.setMessageId(message.getMessageId());
                        item.setDuration(message.getDuration());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        item.setFileName(fileName);
                        item.setBackId(message.getBackId());
                        item.setSendState(2);
//                        if (type.equals("group"))
//                            item.setGroup(message.getGroup());
                        if (message.getReceiver() != null) {
                            if (currentActivity instanceof ChatActivity) {
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_AUDIO, item));
                            }
                        } else {
                            if (currentActivity instanceof GroupChatActivity)
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_GROUP_AUDIO, item));
                        }
                        item.saveOrUpdate("messageid=?", item.getMessageId());
                    }
                });
    }

}
