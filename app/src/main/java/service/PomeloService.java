package service;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.GroupChatActivity;
import com.yumeng.tillo.VideoCallActivity;
import com.yumeng.tillo.VoiceCallActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import adapter.ChatAdapter;
import application.MyApplication;
import bean.ChatMessage;
import bean.FriendInfo;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import pomelo.DataCallBack;
import pomelo.DataEvent;
import pomelo.DataListener;
import pomelo.PomeloClient;
import receiver.NetWorkStateReceiver;
import utils.AppSharePre;
import utils.SharedDataTool;
import utils.SoundPlayUtils;
import utils.ToastUtils;
import utils.fileutil.FileUtils;

/**
 * Socket 服务
 */

public class PomeloService extends Service {
    private PomeloClient client;
    private static PomeloService instance = null;

    public static PomeloService getInstance() {
        return instance;
    }

    public boolean isConnected = true;
    NetWorkStateReceiver netWorkStateReceiver;
    private UserInfo userInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initClient();
        registerNetWorkChangedReceiver();
        EventBus.getDefault().register(this);
        userInfo = AppSharePre.getPersonalInfo();
        initEventMessage();
        SoundPlayUtils.init(getApplicationContext());
    }

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public PomeloClient getClient() {
        return client;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        client.disconnect();
        instance = null;
        unregisterReceiver(netWorkStateReceiver);
        EventBus.getDefault().unregister(this);
        SoundPlayUtils.release();
    }

    //初始化
    public void initClient() {
        client = new PomeloClient(Constants.SOCKET_IP, Constants.SOCKET_PORT);
        client.init();
        queryEntry();

    }

    //获取ip、port
    public void queryEntry() {
        UserInfo userInfo = AppSharePre.getPersonalInfo();
        try {
            JSONObject data = new JSONObject();
            data.put("token", userInfo.getToken());
            data.put("uid", userInfo.getId());
            client.request("gate.gateHandler.queryEntry", data, new DataCallBack() {
                @Override
                public void responseData(JSONObject jsonObject) {
                    client.disconnect();
                    try {
                        Log.e("queryEntry", "queryEntry Success!");
                        int port = jsonObject.getInt("port");
                        String token = jsonObject.getString("init_token");
                        enter(port, token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //建立连接
    public void enter(final int port, String initToken) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("client", "android");
            msg.put("init_token", initToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.disconnect();
        client = new PomeloClient(Constants.SOCKET_IP, port);
        client.init();
        client.request("connector.entryHandler.enter", msg, new DataCallBack() {
            @Override
            public void responseData(JSONObject msg) {
                Log.e("enter", "enter Success!");
                //初始化群列表界面
                EventListener();
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_CONNECT_SUCCESS, null));
            }
        });
    }

    //添加事件绑定
    public void EventListener() {
        //聊天信息
        client.on("onChat", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                JSONObject msg = event.getMessage();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(msg.toString());
                saveMessageToDatabase(jsonObject);
//                mTabHost.getChatFrgment().receiveNewsMessage(item);
            }
        });
        //群聊天
        client.on("onChat.group", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                JSONObject msg = event.getMessage();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(msg.toString());
                saveMessageToDatabase(jsonObject);
            }
        });
        client.on("friendRequest", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                String jsonString = event.getMessage().toString();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonString);
                com.alibaba.fastjson.JSONObject bodyJson = JSON.parseObject(jsonObject.getString("body"));
                String type = bodyJson.getString("type");
                if (type.equals("agree"))
                //通知主界面更新列表
                {
                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
                } else if (type.equals("new")) {
                    //通知界面刷新验证信息
                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_NEW_FRIEND_REQUEST, null));
                }
            }
        });
        client.on("roomRequest", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                String jsonString = event.getMessage().toString();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonString);
                com.alibaba.fastjson.JSONObject bodyJson = JSON.parseObject(jsonObject.getString("body"));
                Log.e("TAG", bodyJson.getString("message"));
                String friendId = bodyJson.getString("message");
                int type = bodyJson.getIntValue("style");
                if (type == 0) {
                    //语音
                    Intent voiceCallIntent = new Intent(getApplicationContext(), VoiceCallActivity.class);
                    voiceCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    voiceCallIntent.putExtra("message", friendId);
                    voiceCallIntent.putExtra("type", 2);//接受者传2
                    startActivity(voiceCallIntent);
                } else {
                    Intent voiceCallIntent = new Intent(getApplicationContext(), VideoCallActivity.class);
                    voiceCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    voiceCallIntent.putExtra("message", friendId);
                    voiceCallIntent.putExtra("type", 2);//接受者传2
                    startActivity(voiceCallIntent);
                }

            }
        });
//        room-request-feedback
        client.on("roomRequestFeedback", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                Log.e("TAG", "feedback");
                String jsonString = event.getMessage().toString();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonString);
                com.alibaba.fastjson.JSONObject bodyJson = JSON.parseObject(jsonObject.getString("body"));
                int choice = bodyJson.getIntValue("choice");
                int type = bodyJson.getIntValue("style");
                if (type == 0) {
                    //语音
                    if (choice == 0) {
                        //通知进入房间号
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VOICE_ACTIVITY, Constants.MESSAGE_EVENT_JOIN_VOICE, null));
                    } else {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VOICE_ACTIVITY, Constants.MESSAGE_EVENT_FINISH_VOICE, null));
                    }


                } else {
                    //视频
                    if (choice == 0) {
                        //点击接受
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VIDEO_ACTIVITY, Constants.MESSAGE_EVENT_JOIN_VOICE, null));
                    } else {
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_VIDEO_ACTIVITY, Constants.MESSAGE_EVENT_FINISH_VOICE, null));
                    }
                }
                Log.e("TAG", "feedback" + bodyJson.toString());

            }
        });
        client.on("group.join", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                //通知更新群列表
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_FRAGMENT, Constants.MESSAGE_EVENT_UPDATE_GROUP, null));
            }
        });


    }

    //保存消息
    public void saveMessageToDatabase(com.alibaba.fastjson.JSONObject jsonObject) {
        com.alibaba.fastjson.JSONObject dataJSON = JSON.parseObject(jsonObject.getString("body"));
        ChatMessage item = new ChatMessage();
        item.setType(dataJSON.getString("type"));
        switch (dataJSON.getString("type")) {
            case "text":
                item.setContent(dataJSON.getString("content"));
                break;
            case "image":
                item.setContent("...[图片]");
                item.setSourceId(dataJSON.getString("sourceid"));
                break;
            case "file":
                item.setContent("...[文件]");
                item.setSourceId(dataJSON.getString("sourceid"));
                break;
            case "audio":
                item.setContent("...[语音]");
                item.setSourceId(dataJSON.getString("sourceid"));
                item.setDuration(dataJSON.getString("duration"));
                item.setFileName(dataJSON.getString("filename"));
                item.setSendState(0);
                break;
            case "video":
                item.setContent("...[视频]");
                break;

        }
        if (dataJSON.getString("group") == null) {
            item.setSender(dataJSON.getString("from"));
            item.setReceiver(dataJSON.getString("target"));
            item.setMessageId(dataJSON.getString("_id"));
            item.setTimestamp(dataJSON.getLong("timestamp"));
            item.setRoomid(dataJSON.getString("roomid"));
            item.save();
            if (dataJSON.getString("type").equals("audio")) {
                //下载音视频文件到本地
                downloadAudioFile(item.getSourceId(), item, item.getFileName(), "chat");
            }
            //给对话界面、聊天界面分发消息
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
        } else {
            if (dataJSON.getString("from").equals(userInfo.getId())) {
                //自己发的消息
                item.setMessageId(dataJSON.getString("_id"));
                item.setSender(dataJSON.getString("from"));
                item.setTimestamp(dataJSON.getLong("timestamp"));
                item.setRoomid(dataJSON.getString("roomid"));
                //给对话界面、聊天界面分发消息
            } else {
                //群消息
                item.setSender(dataJSON.getString("from"));
                item.setMessageId(dataJSON.getString("_id"));
                item.setTimestamp(dataJSON.getLong("timestamp"));
                item.setRoomid(dataJSON.getString("roomid"));
                item.save();
                if (dataJSON.getString("type").equals("audio")) {
                    //下载音视频文件到本地
                    downloadAudioFile(item.getSourceId(), item, item.getFileName(), "group");
                }
                //给对话界面、聊天界面分发消息
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_GROUP_HAS_MESSAGE, item));
            }
            //给对话界面分发消息
            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_FRAGMENT, Constants.MESSAGE_EVENT_GROUP_HAS_MESSAGE, item));
        }
    }

    //注册网络监听广播
    public void registerNetWorkChangedReceiver() {
        netWorkStateReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);

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
                if (client != null)
                    client.disconnect();
                initClient();
//                initEventMessage();
            }
        }
    }

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
                        if (client != null)
                            client.disconnect();
                        initClient();
                        initEventMessage();
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
                        if (client != null)
                            client.disconnect();
                        initClient();
                        initEventMessage();
                        isConnected = true;
                    }
                }
//                if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
//                } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
//                } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
//                }
            }
        }
    }

    //下载音频文件
    public void downloadAudioFile(String url, final ChatMessage message, String fileName, String type) {
        String fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        Activity currentActivity = MyApplication.getCurrentActivity();
        OkGo.<File>get(Constants.TSHION_URL + Constants.downloadFile + url)
                .headers("Authorization", "Bearer " + userInfo.getToken())
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
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
                        if (type.equals("chat")) {
                            if (currentActivity instanceof ChatActivity) {
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_AUDIO, item));
                            }
                        } else if (type.equals("group")) {
                            if (currentActivity instanceof GroupChatActivity)
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_GROUP_AUDIO, item));
                        }
                        item.saveOrUpdate("message_id=?", item.getMessageId());
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
                        if (type.equals("chat")) {
                            if (currentActivity instanceof ChatActivity) {
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_AUDIO, item));
                            }
                        } else if (type.equals("group")) {
                            if (currentActivity instanceof GroupChatActivity)
                                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_GROUP_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_UPDATE_GROUP_AUDIO, item));
                        }
                        item.saveOrUpdate("message_id=?", item.getMessageId());
                    }
                });
    }


}
