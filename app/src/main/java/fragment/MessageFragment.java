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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.GroupChatActivity;
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
import bean.GroupInfo;
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
        userInfo = AppSharePre.getPersonalInfo();
        getFriendList();
    }


//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        if (isVisibleToUser) {
//            userInfo = AppSharePre.getPersonalInfo();
//            getFriendList();
//            getGroupList();
//            getSessionList();
//        }
//    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
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

        messageRv = myFindViewsById(R.id.fragment_message_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageRv.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(getActivity());
        messageRv.setAdapter(messageAdapter);
        initReceiver();
        messageAdapter.setOnItemClickListener((view1, position) -> {
            if (dataList.get(position).getChatType().equals("single")) {
                //单聊
                FriendInfo friendInfo = dataList.get(position).getFriendInfo().get(0);
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("friendInfo", friendInfo);
                startActivity(chatIntent);
            } else {
                //群聊
                Intent chatIntent = new Intent(getActivity(), GroupChatActivity.class);
                chatIntent.putExtra("groupInfo", getGroupInfo(dataList.get(position)));
                startActivity(chatIntent);
            }

        });
        messageAdapter.setOnItemLongClickListener((view12, position) -> {
            //弹出删除按钮
            showClearDialog(position, dataList.get(position).getRoomId());
        });
    }

    //获取groupInfo
    public GroupInfo getGroupInfo(Conversation conversation) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setRoomId(conversation.getRoomId());
        groupInfo.setAvatar(conversation.getAvatar());
        groupInfo.setOwner(conversation.getOwner());
        groupInfo.setName(conversation.getName());
        return groupInfo;
    }


    //获取聊天会话列表
    public void getSessionList() {
        OkGo.<String>get(Constants.FriendUrl + Constants.getChatSessionList)
                .headers("Authorization", userInfo.getToken())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("TAG_conversation", response.body());
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                        List<Conversation> messageList = JSON.parseArray(jsonObject.getString("data"), Conversation.class);
                        if (messageList.size() > 0) {
                            //加载首页消息数据
                            dataList.clear();
                            getChatListData(messageList);
                            DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                                @Override
                                public <T> void onFinish(List<T> t) {
                                    dataList = (List<Conversation>) t;
//                                        Collections.sort(dataList, new Comparator<Conversation>() {
//                                            @Override
//                                            public int compare(Conversation o1, Conversation o2) {
//                                                int friend1 = o1.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o1.getFriendInfo().get(0).getIstop());
//                                                int friend2 = o2.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o2.getFriendInfo().get(0).getIstop());
//                                                if (friend1 > friend2)
//                                                    return -1;
//                                                else if (friend2 > friend1)
//                                                    return 1;
//                                                else
//                                                    return 0;
//                                            }
//                                        });
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            messageAdapter.setData(dataList);
                                        }
                                    }, 200);
                                }
                            });
                            //如果有离线消息，显示消息红点
                            EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_CHAT_DOT, null));

                        } else {
                            DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                                @Override
                                public <T> void onFinish(List<T> t) {
                                    dataList = (List<Conversation>) t;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                                Collections.sort(dataList, new Comparator<Conversation>() {
//                                                    @Override
//                                                    public int compare(Conversation o1, Conversation o2) {
//                                                        int friend1 = o1.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o1.getFriendInfo().get(0).getIstop());
//                                                        int friend2 = o2.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o2.getFriendInfo().get(0).getIstop());
//                                                        if (friend1 > friend2)
//                                                            return -1;
//                                                        else if (friend2 > friend1)
//                                                            return 1;
//                                                        else
//                                                            return 0;
//                                                    }
//                                                });
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
                        //无网络情况下 拉取本地记录
                        DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
                            @Override
                            public <T> void onFinish(List<T> t) {
                                dataList.clear();
                                dataList = (List<Conversation>) t;
//                    Collections.sort(dataList, new Comparator<Conversation>() {
//                        @Override
//                        public int compare(Conversation o1, Conversation o2) {
//                            int friend1 = o1.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o1.getFriendInfo().get(0).getIstop());
//                            int friend2 = o2.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o2.getFriendInfo().get(0).getIstop());
//                            if (friend1 > friend2)
//                                return -1;
//                            else if (friend2 > friend1)
//                                return 1;
//                            else
//                                return 0;
//                        }
//                    });
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageAdapter.setData(dataList);
                                    }
                                }, 200);
                            }
                        });
                    }
                });


    }

    //获取对话列表数据
    public List<Conversation> getChatListData(List<Conversation> list) {
        List<Conversation> tempList = new ArrayList<>();
        if (list.size() == 0)
            return tempList;
        for (int i = 0; i < list.size(); i++) {
            Conversation conversation = new Conversation();
            //房间号
            conversation.setRoomId(list.get(i).getRoomId());
            //时间戳
            conversation.setTimestamp(list.get(i).getTimestamp());
            //未读消息
            conversation.setMsgNum(list.get(i).getMsgNum());
            //接收者
            conversation.setChatType(list.get(i).getChatType());
            if (conversation.getChatType().equals("single")) {
                //昵称
                conversation.setName(list.get(i).getFriendInfo().get(0).getShowname());
                //头像
                conversation.setAvatar(list.get(i).getFriendInfo().get(0).getAvatar());
            } else {
                conversation.setName(list.get(i).getGroupInfo().get(0).getName());
                //头像
                conversation.setAvatar(list.get(i).getGroupInfo().get(0).getAvatar());
            }
            //类型
            conversation.setType(list.get(i).getType());
            switch (list.get(i).getType()) {
                case "text":
                    conversation.setContent(list.get(i).getContent());
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
            boolean isSave = conversation.saveOrUpdate("roomid=?", list.get(i).getRoomId());
            tempList.add(conversation);
        }
        return tempList;
    }

    //收到新消息
    public void receiveNewsMessage(ChatMessage messageBean) {
        Activity currentActivity = MyApplication.getCurrentActivity();
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
            conversation.setRoomId(messageBean.getRoomid());
            unreadTemp = 1;
        }
        if (messageBean.getReceiver() != null) {
            //单聊
            FriendInfo friendInfo = conversation.getFriendInfo().get(0);
            conversation.setAvatar(friendInfo.getAvatar());
            conversation.setName(friendInfo.getName());
            conversation.setChatType("single");
            //不存在的会话,则判断当前界面是否是主界面
            if (currentActivity instanceof MainActivity) {
                //如果是主界面，未读消息为1
                conversation.setOnlineMessage(unreadTemp);
//                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_CHAT_DOT, null));
            } else if (currentActivity instanceof ChatActivity) {
                //聊天界面不是 对应的消息会话界面
                if (!((ChatActivity) currentActivity).getRoomId().equals(messageBean.getRoomid())) {
                    conversation.setOnlineMessage(unreadTemp);
//                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_CHAT_DOT, null));
                }
            }
        } else {
            //群聊
            List<GroupInfo> groupInfoList = DataSupport.where("roomid=?", messageBean.getRoomid()).find(GroupInfo.class);
            conversation.setAvatar(groupInfoList.get(0).getAvatar());
            conversation.setRoomId(groupInfoList.get(0).getRoomId());
            conversation.setName(groupInfoList.get(0).getName());
            conversation.setOwner(groupInfoList.get(0).getOwner());
            conversation.setChatType("group");
            if (currentActivity instanceof MainActivity) {
                //如果是主界面，未读消息为1
                conversation.setOnlineMessage(unreadTemp);
//                if (!messageBean.getSender().equals(userInfo.getId()))
//                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_GROUP_CHAT_DOT, null));
//            if (!messageBean.getFrom().equals(userInfo.getUid()))
//                messageBean.save();
            } else if (currentActivity instanceof GroupChatActivity) {
                //聊天界面不是 对应的消息会话界面
                if (!((GroupChatActivity) currentActivity).getRoomId().equals(messageBean.getRoomid())) {
//                    if (!messageBean.getSender().equals(userInfo.getId())) {
                    //不是自己发送的消息
                    conversation.setOnlineMessage(unreadTemp);
//                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_GROUP_CHAT_DOT, null));
//                    }
                }
            }
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
//                Collections.sort(dataList, new Comparator<Conversation>() {
//                    @Override
//                    public int compare(Conversation o1, Conversation o2) {
//                        int friend1 = o1.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o1.getFriendInfo().get(0).getIstop());
//                        int friend2 = o2.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o2.getFriendInfo().get(0).getIstop());
//                        if (friend1 > friend2)
//                            return -1;
//                        else if (friend2 > friend1)
//                            return 1;
//                        else
//                            return 0;
//                    }
//                });
                messageAdapter.setData(dataList);
            }
        });
    }

    //获取好友列表
    public void getFriendList() {
        OkGo.<String>get(Constants.FriendUrl + Constants.getFriendList)
                .headers("Authorization", userInfo.getToken())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("TAG_getFriend_message", response.body());
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
                        Log.e("TAG_getFriend_error", response.getException().getMessage());
                    }
                });

    }

    //保存好友列表
    public void updateFriendList(final List<FriendInfo> dataList) {
        if (dataList == null) {
//            getSessionList();
            getGroupList();
            return;
        }
        if (dataList.size() == 0) {
//            getSessionList();
            getGroupList();
            return;
        }
        for (FriendInfo friendInfo : dataList) {
            FriendInfo tempFriend = new FriendInfo();
            tempFriend.setFriend_id(friendInfo.getFriend_id());
            tempFriend.setAvatar(friendInfo.getAvatar());
            tempFriend.setMobile(friendInfo.getMobile());
            tempFriend.setName(friendInfo.getName());
            tempFriend.setHeaderWord(friendInfo.getHeaderWord());
            tempFriend.setNickname(friendInfo.getNickname());
            tempFriend.setRoomId(friendInfo.getRoomId());
            tempFriend.setShowname(friendInfo.getShowname());
//            tempFriend.setBlock(friendInfo.getSettings().getBlock() + "");
//            tempFriend.setNot_disturb(friendInfo.getSettings().getNot_disturb() + "");
            tempFriend.saveOrUpdate("roomid=?", friendInfo.getRoomId());
        }
        getGroupList();
    }

    //获取群列表
    public void getGroupList() {
        Log.e("TAG_group", "获取群列表");
        OkGo.<String>get(Constants.GroupUrl + Constants.getGroupList)
                .headers("Authorization", userInfo.getToken())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("TAG_groupList", response.body());
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                        List<GroupInfo> tempList = JSONArray.parseArray(jsonObject.getString("data"), GroupInfo.class);
                        if (tempList != null) {
                            if (tempList.size() > 0) {
                                //保存列表到数据库
                                saveGroupListToDatabase(tempList);
                            }
                        }
                        getSessionList();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        //从数据库获取
                        Log.e("TAG_groupList", response.getException().getMessage());
                    }
                });
    }

    public void saveGroupListToDatabase(List<GroupInfo> templist) {
        if (templist == null)
            return;
        if (templist.size() == 0)
            return;
        for (GroupInfo info : templist) {
            GroupInfo tempInfo = new GroupInfo();
            tempInfo.setOwner(info.getOwner());
            tempInfo.setRoomId(info.getRoomId());
            tempInfo.setName(info.getName());
            tempInfo.setAvatar(info.getAvatar());
            tempInfo.saveOrUpdate("roomid=?", info.getRoomId());
        }
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
//                        Collections.sort(dataList, new Comparator<Conversation>() {
//                            @Override
//                            public int compare(Conversation o1, Conversation o2) {
//                                int friend1 = o1.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o1.getFriendInfo().get(0).getIstop());
//                                int friend2 = o2.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(o2.getFriendInfo().get(0).getIstop());
//                                if (friend1 > friend2)
//                                    return -1;
//                                else if (friend2 > friend1)
//                                    return 1;
//                                else
//                                    return 0;
//                            }
//                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                messageAdapter.setData(dataList);
                            }
                        }, 200);
                    }
                });
            }else if (behavior.equals(Constants.MESSAGE_EVENT_GROUP_INVITE)){
                //重新获取群组列表
                Log.e("TAG_GroupList","重新获取群列表");
                getGroupList();
            }

        }
    }

    public void showClearDialog(int itemPosition, String roomid) {
        final String[] stringItems = {"清除所有信息"};
        final ActionSheetDialog dialog = new ActionSheetDialog(getActivity(), stringItems, null);
        dialog.title("删除信息")//
                .titleTextSize_SP(14.5f)//
                .show();
        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    clearChatRecorder(itemPosition, roomid);
                }
                dialog.dismiss();
            }
        });
    }

    //清除聊天记录
    public void clearChatRecorder(int itemPosition, String roomid) {
        DataSupport.deleteAll(ChatMessage.class, "roomid=?", roomid);
        DataSupport.deleteAll(Conversation.class, "roomid=?", roomid);
        dataList.remove(itemPosition);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        }, 200);
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
