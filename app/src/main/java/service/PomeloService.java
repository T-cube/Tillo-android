package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.alibaba.fastjson.JSON;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import bean.ChatMessage;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import pomelo.DataCallBack;
import pomelo.DataEvent;
import pomelo.DataListener;
import pomelo.PomeloClient;
import utils.AppSharePre;

/**
 * Socket 服务
 */

public class PomeloService extends Service {
    private PomeloClient client;
    private static PomeloService instance = null;

    public static PomeloService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initClient();
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
            data.put("token", userInfo.getAccess_token());
            data.put("uid", userInfo.getUid());
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
        client = new PomeloClient(Constants.SOCKET_IP, port);
        client.init();
        client.request("connector.entryHandler.enter", msg, new DataCallBack() {
            @Override
            public void responseData(JSONObject msg) {
                Log.e("enter", "enter Success!");
                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_INIT_CONVERSATION, null));
                EventListener();
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
        client.on("friendRequest", new DataListener() {
            @Override
            public void receiveData(DataEvent event) {
                String jsonString = event.getMessage().toString();
                Log.e("TAG", jsonString);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonString);
                com.alibaba.fastjson.JSONObject bodyJson = JSON.parseObject(jsonObject.getString("body"));
                String type = bodyJson.getString("type");
                if (type.equals("agree"))
                //通知主界面更新列表
                {
                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_MAIN_ACTIVITY, Constants.MESSAGE_EVENT_FRIEND_AGREE, null));
                } else if (type.equals("new")) {
                    //通知界面刷新验证信息

                }
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
                item.setSourceid(dataJSON.getString("sourceid"));
                break;
            case "file":
                item.setContent("...[文件]");
                item.setSourceid(dataJSON.getString("sourceid"));
                break;
            case "audio":
                item.setContent("...[语音]");
                item.setSourceid(dataJSON.getString("sourceid"));
                item.setDuration(dataJSON.getString("duration"));
                break;
            case "video":
                item.setContent("...[视频]");
                break;

        }
        item.setFrom(dataJSON.getString("from"));
        item.setTarget(dataJSON.getString("target"));
        item.setMessage_id(dataJSON.getString("_id"));
        item.setTimestamp(dataJSON.getLong("timestamp"));
        item.setRoomid(dataJSON.getString("roomid"));
        item.save();
        //给对话界面、聊天界面分发消息
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_ACTIVITY, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_EVENT_HAS_MESSAGE, item));
    }


}
