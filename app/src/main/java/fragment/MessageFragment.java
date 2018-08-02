package fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.MainActivity;
import com.yumeng.tillo.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.crud.callback.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import adapter.MessageAdapter;
import application.MyApplication;
import base.BaseFragment;
import bean.ChatBean;
import bean.ChatMessage;
import bean.Conversation;
import bean.FriendInfo;
import bean.Group;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import io.reactivex.functions.Consumer;
import utils.AppSharePre;
import utils.SharedDataTool;
import utils.ToastUtils;
import utils.Utils;

/**
 * 消息界面
 */

public class MessageFragment extends BaseFragment {
    private List<Conversation> dataList = new ArrayList<>();
    ReceiverMessageReceiver receiverMessageReceiver;
    private MessageAdapter messageAdapter;
    private List<FriendInfo> mList = new ArrayList<>();
    private RecyclerView messageRv;
    private UserInfo userInfo;
    private String groupId;
    private RxPermissions rxPermissions;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_message;
    }

    @Override
    public void onResume() {
        super.onResume();
        getFriendList();
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        rxPermissions = new RxPermissions(getActivity());
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                        }
                    }
                });
        userInfo = AppSharePre.getPersonalInfo();
        messageRv = myFindViewsById(R.id.fragment_message_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageRv.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(getActivity());
        messageRv.setAdapter(messageAdapter);
        initReceiver();
        messageAdapter.setOnItemClickListener((view1, position) -> {
            FriendInfo friendInfo = dataList.get(position).getFriendInfo().get(0);
            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
            chatIntent.putExtra("friendInfo", friendInfo);
            startActivity(chatIntent);
        });
        messageAdapter.setOnItemLongClickListener((view12, position) -> {
            //弹出删除按钮
            initBottomDialog(position);
        });
    }


    //获取聊天会话列表
    public void getSessionList() {
        boolean hasNetWork = SharedDataTool.getBoolean(getActivity(), "network");
        if (hasNetWork) {
            OkGo.<String>get(Constants.TSHION_URL + Constants.getChatSessionList)
                    .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                            List<ChatBean> messageList = JSON.parseArray(jsonObject.getString("data"), ChatBean.class);
                            if (messageList.size() > 0) {
                                //加载首页消息数据
                                dataList.clear();
                                getChatListData(messageList);
                                DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                                    @Override
                                    public <T> void onFinish(List<T> t) {
                                        dataList = (List<Conversation>) t;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                messageAdapter.setData(dataList);
                                            }
                                        }, 200);
                                    }
                                });
                            } else {
                                DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                                    @Override
                                    public <T> void onFinish(List<T> t) {
                                        dataList = (List<Conversation>) t;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                messageAdapter.setData(dataList);
                                            }
                                        }, 200);
                                    }
                                });
                            }

                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                        }
                    });
        } else {
            //无网络情况下 拉取本地记录
            DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                @Override
                public <T> void onFinish(List<T> t) {
                    dataList.clear();
                    dataList = (List<Conversation>) t;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            messageAdapter.setData(dataList);
                        }
                    }, 200);
                }
            });
        }

    }

    //获取对话列表数据
    public List<Conversation> getChatListData(List<ChatBean> list) {
        List<Conversation> tempList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Conversation conversation = new Conversation();
            //房间号
            conversation.setRoomid(list.get(i).getMessage().getRoomid());
            //时间戳
            conversation.setTimestamp(list.get(i).getMessage().getTimestamp());
            //未读消息
            conversation.setUnread(list.get(i).getUnread());
            //昵称
            conversation.setName(list.get(i).getMember().getName());
            //头像
            conversation.setAvatar(list.get(i).getMember().getAvatar());
            //用户id
            conversation.setFriendId(list.get(i).getMember().get_id());
            switch (list.get(i).getMessage().getType()) {
                case "text":
                    conversation.setContent(list.get(i).getMessage().getContent());
                    break;
                case "image":
                    conversation.setContent("...[图片]");
                    break;
                case "audio":
                    conversation.setContent("...[语音]");
                    break;
                case "file":
                    conversation.setContent("...[文件]");
                    break;
                case "video":
                    conversation.setContent("...[视频]");
                    break;
//                case "link":
//                    conversation.setContent();
//                    break;
            }
            boolean isSave = conversation.saveOrUpdate("roomid=?", list.get(i).getMessage().getRoomid());
            tempList.add(conversation);
        }
        return tempList;
    }

    //收到新消息
    public void receiveNewsMessage(ChatMessage messageBean) {
        Conversation conversation = null;
        List<Conversation> conversationsList = DataSupport.where("roomid=?", messageBean.getRoomid()).find(Conversation.class);
        int unreadTemp = 0;
        if (conversationsList.size() > 0) {
            //之前的会话
            conversation = conversationsList.get(0);
            unreadTemp = conversation.getOnlineMessage() + 1;
        } else {
            //新的会话
            conversation = new Conversation();
            conversation.setRoomid(messageBean.getRoomid());
            unreadTemp = 1;
        }
        FriendInfo friendInfo = conversation.getFriendInfo().get(0);
        conversation.setFriendId(friendInfo.getFriend_id());
        conversation.setAvatar(friendInfo.getAvatar());
        conversation.setName(friendInfo.getName());
        Activity currentActivity = MyApplication.getCurrentActivity();
        //不存在的会话,则判断当前界面是否是主界面
        if (currentActivity instanceof MainActivity) {
            //如果是主界面，未读消息为1
            conversation.setOnlineMessage(unreadTemp);
//            if (!messageBean.getFrom().equals(userInfo.getUid()))
//                messageBean.save();
        } else if (currentActivity instanceof ChatActivity) {
            //聊天界面不是 对应的消息会话界面
            if (!((ChatActivity) currentActivity).getRoomId().equals(messageBean.getRoomid()))
                conversation.setOnlineMessage(unreadTemp);
        }
        String content = "";
        switch (messageBean.getType()) {
            case "text":
                content = messageBean.getContent();
                break;
            case "image":
                content = "...[图片]";
                break;
            case "file":
                content = "...[文件]";
                break;
            case "audio":
                content = "...[语音]";
                break;
            case "video":
                content = "...[视频]";
                break;
        }
        conversation.setContent(content);
        conversation.setTimestamp(messageBean.getTimestamp());
        conversation.saveOrUpdate("roomid=?", messageBean.getRoomid());
        DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                dataList = (List<Conversation>) t;
                messageAdapter.setData(dataList);
            }
        });
    }

    //获取好友列表
    public void getFriendList() {
        boolean hasNetWork = SharedDataTool.getBoolean(getActivity(), "network");
        if (hasNetWork) {
            OkGo.<String>get(Constants.TSHION_URL + Constants.getFriendList)
                    .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                            String message = jsonObject.getString("message");
                            int status = jsonObject.getIntValue("status");
                            if (status == 200) {
                                //获取好友列表
                                mList = JSON.parseArray(jsonObject.getString("data"), FriendInfo.class);
                                //对集合排序
                                Collections.sort(mList, new Comparator<FriendInfo>() {
                                    @Override
                                    public int compare(FriendInfo lhs, FriendInfo rhs) {
                                        //根据拼音进行排序
                                        return lhs.getPinyin().compareTo(rhs.getPinyin().toUpperCase());
                                    }
                                });
                                //将列表存入数据库
                                updateFriendList(mList);

                            } else {
                                ToastUtils.getInstance().shortToast(message);
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                        }
                    });
        } else {
            getSessionList();
        }
    }

    //保存好友列表
    public void updateFriendList(final List<FriendInfo> dataList) {
        if (dataList == null)
            return;
        if (dataList.size() == 0)
            return;
        for (FriendInfo friendInfo : dataList) {
            FriendInfo tempFriend = new FriendInfo();
            tempFriend.setFriend_id(friendInfo.getFriend_id());
            tempFriend.setAvatar(friendInfo.getAvatar());
            tempFriend.setMobile(friendInfo.getMobile());
            tempFriend.setName(friendInfo.getName());
            tempFriend.setHeaderWord(friendInfo.getHeaderWord());
            tempFriend.setNickname(friendInfo.getNickname());
            tempFriend.setRoomid(friendInfo.getRoomid());
            tempFriend.setShowname(friendInfo.getShowname());
            tempFriend.saveOrUpdate("roomid=?", friendInfo.getRoomid());
        }
        getSessionList();
    }
    public void initBottomDialog(int position) {
        final Dialog dialog = new Dialog(getActivity(), R.style.AlertDialogStyle);
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete, null);
        dialog.setContentView(inflate);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setAttributes(params);
        dialog.setCancelable(true);
        dialog.show();
        RelativeLayout deleteRl = Utils.findViewsById(inflate, R.id.dialog_delete_rl_delete);
        deleteRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(position);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messageAdapter.notifyDataSetChanged();
                    }
                }, 200);
                dialog.dismiss();
            }
        });
        RelativeLayout cancelRl = Utils.findViewsById(inflate, R.id.dialog_delete_rl_cancel);
        cancelRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventReceiver(MessageEvent messageEvent) {
        String target = messageEvent.getTarget();
        String behavior = messageEvent.getBehavior();
        if (target.equals(Constants.TARGET_CHAT_FRAGMENT)) {
            if (behavior.equals(Constants.MESSAGE_EVENT_HAS_MESSAGE)) {
                ChatMessage item = messageEvent.getChatMessage();
                receiveNewsMessage(item);
            } else if (behavior.equals(Constants.MESSAGE_EVENT_INIT_CONVERSATION)) {
                //重新获取列表
                getFriendList();
            } else if (behavior.equals(Constants.MESSAGE_EVENT_CLEAR_OFFLINE)) {
                DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                    @Override
                    public <T> void onFinish(List<T> t) {
                        dataList = (List<Conversation>) t;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                messageAdapter.setData(dataList);
                            }
                        }, 200);
                    }
                });
            }

        }
    }

    /**
     * 广播配置
     */
    private void initReceiver() {
        receiverMessageReceiver = new ReceiverMessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("receiverMessage");
        getActivity().registerReceiver(receiverMessageReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(receiverMessageReceiver);
    }

    public class ReceiverMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessage message = (ChatMessage) intent.getSerializableExtra("message");
            receiveNewsMessage(message);
        }
    }
}
