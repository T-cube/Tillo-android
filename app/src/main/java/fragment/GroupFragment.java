package fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.crud.callback.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import adapter.GroupAdapter;
import adapter.MessageAdapter;
import application.MyApplication;
import base.BaseFragment;
import bean.ChatMessage;
import bean.Conversation;
import bean.FriendInfo;
import bean.GroupInfo;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import listener.OnItemClickListener;
import listener.OnItemLongClickListener;
import utils.AppSharePre;
import utils.SharedDataTool;

/**
 * 群聊界面
 */

public class GroupFragment extends BaseFragment {
//    private List<GroupInfo> dataList = new ArrayList<>();
//    private RecyclerView groupRv;
//    private UserInfo userInfo;
//    private GroupAdapter groupAdapter;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };
//
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_group;
    }
//
    @Override
    protected void initView(View view, Bundle savedInstanceState) {
//        EventBus.getDefault().register(this);
//        userInfo = AppSharePre.getPersonalInfo();
//        groupRv = myFindViewsById(R.id.fragment_group_rv);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        groupAdapter = new GroupAdapter(getActivity());
//        groupRv.setLayoutManager(linearLayoutManager);
//        groupRv.setAdapter(groupAdapter);
//        groupAdapter.setData(dataList);
//        groupAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                //跳转到群聊天界面
//                Intent groupIntent = new Intent(getActivity(), GroupChatActivity.class);
//                GroupInfo item = dataList.get(position);
//                groupIntent.putExtra("groupInfo", item);
//                if (item.getOffline_count() > 0) {
//                    dataList.get(position).setOffline_count(0);
//                    groupAdapter.notifyDataSetChanged();
//                }
//                //如果有未读消息，更改为已读
//                updateGroupList(dataList);
//                startActivity(groupIntent);
//            }
//        });
//        groupAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
//            @Override
//            public void onItemLongClick(View view, int position) {
//                //长按删除
//                showClearDialog(position, dataList.get(position).getRoomid());
//            }
//        });
    }
//
//
//    //获取群会话列表
//    public void getGroupList() {
//        boolean hasNetWork = SharedDataTool.getBoolean(getActivity(), "network");
//        if (hasNetWork) {
//            OkGo.<String>get(Constants.TSHION_URL + Constants.getGroupSessionList)
//                    .headers("Authorization", "Bearer " + userInfo.getToken())
//                    .params("user_id", userInfo.getId())
//                    .execute(new StringCallback() {
//                        @Override
//                        public void onSuccess(Response<String> response) {
//                            JSONObject jsonObject = JSON.parseObject(response.body());
//                            List<GroupInfo> tempList = JSONArray.parseArray(jsonObject.getString("data"), GroupInfo.class);
//                            if (tempList != null) {
//                                if (tempList.size() > 0) {
//                                    //如果有未读消息，显示消息红点
//                                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_GROUP_CHAT_DOT, null));
//                                    //保存列表到数据库
//                                    saveGroupListToDatabase(tempList);
//                                } else
//                                    getGroupListFromDatabase();
//                            } else {
//                                getGroupListFromDatabase();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Response<String> response) {
//                            //从数据库获取
//                            getGroupListFromDatabase();
//                        }
//                    });
//        } else {
//            getGroupListFromDatabase();
//        }
//    }
//
//    //从数据库加载群列表
//    public void getGroupListFromDatabase() {
//        DataSupport.findAllAsync(GroupInfo.class)
//                .listen(new FindMultiCallback() {
//                    @Override
//                    public <T> void onFinish(List<T> t) {
//                        List<GroupInfo> tempList = getNotEmptyGroupInfo((List<GroupInfo>) t);
//                        dataList.clear();
//                        dataList.addAll(tempList);
//                        Collections.sort(dataList, new Comparator<GroupInfo>() {
//                            @Override
//                            public int compare(GroupInfo g1, GroupInfo g2) {
//                                int group1 = g1.getIsTop() == null ? 0 : Integer.parseInt(g1.getIsTop());
//                                int group2 = g2.getIsTop() == null ? 0 : Integer.parseInt(g2.getIsTop());
//                                if (group1 > group2)
//                                    return -1;
//                                else if (group2 > group1)
//                                    return 1;
//                                else
//                                    return 0;
//                            }
//                        });
//                        groupAdapter.notifyDataSetChanged();
//                    }
//                });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        getGroupList();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEventReceiver(MessageEvent messageEvent) {
//        String target = messageEvent.getTarget();
//        String behavior = messageEvent.getBehavior();
//        if (target.equals(Constants.TARGET_GROUP_FRAGMENT)) {
//            if (behavior.equals(Constants.MESSAGE_EVENT_UPDATE_GROUP_SESSION)) {
//                //重新拉取会话列表
//                getGroupList();
//            } else if (behavior.equals(Constants.MESSAGE_EVENT_GROUP_HAS_MESSAGE)) {
//                //更新会话
//                updateGroupSession(messageEvent.getChatMessage());
//            } else if (behavior.equals(Constants.MESSAGE_EVENT_CLEAR_GROUP_OFFLINE)) {
//                //清除群未读消息
//                DataSupport.findAllAsync(GroupInfo.class).listen(new FindMultiCallback() {
//                    @Override
//                    public <T> void onFinish(List<T> t) {
//                        List<GroupInfo> tempList = getNotEmptyGroupInfo((List<GroupInfo>) t);
//                        dataList.clear();
//                        dataList.addAll(tempList);
//                        Collections.sort(dataList, new Comparator<GroupInfo>() {
//                            @Override
//                            public int compare(GroupInfo g1, GroupInfo g2) {
//                                int group1 = g1.getIsTop() == null ? 0 : Integer.parseInt(g1.getIsTop());
//                                int group2 = g2.getIsTop() == null ? 0 : Integer.parseInt(g2.getIsTop());
//                                if (group1 > group2)
//                                    return -1;
//                                else if (group2 > group1)
//                                    return 1;
//                                else
//                                    return 0;
//                            }
//                        });
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                groupAdapter.setData(dataList);
//                            }
//                        }, 200);
//                    }
//                });
//
//            }
//        }
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//    }
//
//
//    public void saveGroupListToDatabase(List<GroupInfo> templist) {
//        if (templist == null)
//            return;
//        if (templist.size() == 0)
//            return;
//        for (GroupInfo info : templist) {
//            GroupInfo tempInfo = new GroupInfo();
//            tempInfo.setOwner(info.getOwner());
//            tempInfo.setCreator(info.getCreator());
//            tempInfo.setCreate_at(info.getCreate_at());
//            tempInfo.setRoomid(info.getRoomid());
//            tempInfo.setGroup_id(info.getGroup_id());
//            tempInfo.setName(info.getName());
//            tempInfo.setOffline_count(info.getOffline_count());
//            ChatMessage tempMsg = info.getMessage();
//            String content = "";
//            switch (tempMsg.getType()) {
//                case "text":
//                    content = tempMsg.getContent();
//                    break;
//                case "image":
//                    content = "...[图片]";
//                    break;
//                case "file":
//                    content = "...[文件]";
//                    break;
//                case "audio":
//                    content = "...[语音]";
//                    break;
//                case "video":
//                    content = "...[视频]";
//                    break;
//            }
//            tempInfo.setContent(content);
//            tempInfo.setAvatar(info.getAvatar());
//            boolean isSave = tempInfo.saveOrUpdate("roomid=?", info.getRoomid());
//        }
//        getGroupListFromDatabase();
//    }
//
//    public void updateGroupList(List<GroupInfo> templist) {
//        if (templist == null)
//            return;
//        if (templist.size() == 0)
//            return;
//        for (GroupInfo info : templist) {
//            GroupInfo tempInfo = new GroupInfo();
//            tempInfo.setOwner(info.getOwner());
//            tempInfo.setCreator(info.getCreator());
//            tempInfo.setCreate_at(info.getCreate_at());
//            tempInfo.setRoomid(info.getRoomid());
//            tempInfo.setGroup_id(info.getGroup_id());
//            tempInfo.setName(info.getName());
//            tempInfo.setOffline_count(info.getOffline_count());
//            tempInfo.setContent(info.getContent());
//            tempInfo.saveOrUpdate("roomid=?", info.getRoomid());
//        }
//    }
//
//    //更新群会话界面
//    public void updateGroupSession(ChatMessage messageBean) {
//        GroupInfo groupInfo = null;
//        List<GroupInfo> groupInfoList = DataSupport.where("roomid=?", messageBean.getRoomid()).find(GroupInfo.class);
//        int unreadTemp = 0;
//        if (groupInfoList.size() > 0) {
//            //之前的会话
//            groupInfo = groupInfoList.get(0);
//            if (!messageBean.getSender().equals(userInfo.getId()))
//                //不是自己发的
//                unreadTemp = groupInfo.getOnlineMessage() + 1;
//        }
//        Activity currentActivity = MyApplication.getCurrentActivity();
//        //不存在的会话,则判断当前界面是否是主界面
//        if (currentActivity instanceof MainActivity) {
//            //如果是主界面，未读消息为1
//            groupInfo.setOnlineMessage(unreadTemp);
//            if (!messageBean.getSender().equals(userInfo.getId()))
//                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_GROUP_CHAT_DOT, null));
////            if (!messageBean.getFrom().equals(userInfo.getUid()))
////                messageBean.save();
//        } else if (currentActivity instanceof GroupChatActivity) {
//            //聊天界面不是 对应的消息会话界面
//            if (!((GroupChatActivity) currentActivity).getRoomId().equals(messageBean.getRoomid())) {
//                if (!messageBean.getSender().equals(userInfo.getId())) {
//                    //不是自己发送的消息
//                    groupInfo.setOnlineMessage(unreadTemp);
//                    EventBus.getDefault().post(new MessageEvent(Constants.TARGET_CHAT_FRAGMENT, Constants.MESSAGE_GROUP_CHAT_DOT, null));
//                }
//            }
//        }
//        String content = "";
//        switch (messageBean.getType()) {
//            case "text":
//                content = messageBean.getContent();
//                break;
//            case "image":
//                content = "...[图片]";
//                break;
//            case "file":
//                content = "...[文件]";
//                break;
//            case "audio":
//                content = "...[语音]";
//                break;
//            case "video":
//                content = "...[视频]";
//                break;
//        }
//        groupInfo.setContent(content);
//        groupInfo.setMessage(messageBean);
//        groupInfo.saveOrUpdate("roomid=?", messageBean.getRoomid());
//        DataSupport.findAllAsync(GroupInfo.class).listen(new FindMultiCallback() {
//            @Override
//            public <T> void onFinish(List<T> t) {
//                List<GroupInfo> tempList = getNotEmptyGroupInfo((List<GroupInfo>) t);
//                dataList.clear();
//                dataList.addAll(tempList);
//                Collections.sort(dataList, new Comparator<GroupInfo>() {
//                    @Override
//                    public int compare(GroupInfo g1, GroupInfo g2) {
//                        int group1 = g1.getIsTop() == null ? 0 : Integer.parseInt(g1.getIsTop());
//                        int group2 = g2.getIsTop() == null ? 0 : Integer.parseInt(g2.getIsTop());
//                        if (group1 > group2)
//                            return -1;
//                        else if (group2 > group1)
//                            return 1;
//                        else
//                            return 0;
//                    }
//                });
//                groupAdapter.setData(dataList);
//            }
//        });
//    }
//
//    //筛选出非空的GroupInfo
//    public List<GroupInfo> getNotEmptyGroupInfo(List<GroupInfo> list) {
//        List<GroupInfo> tempList = new ArrayList<>();
//        for (GroupInfo groupInfo : list) {
//            if (!TextUtils.isEmpty(groupInfo.getContent())) {
//                tempList.add(groupInfo);
//            }
//        }
//        return tempList;
//    }
//
//    public void showClearDialog(int itemPosition, String roomid) {
//        final String[] stringItems = {"清除群信息"};
//        final ActionSheetDialog dialog = new ActionSheetDialog(getActivity(), stringItems, null);
//        dialog.title("删除信息")//
//                .titleTextSize_SP(14.5f)//
//                .show();
//        dialog.setOnOperItemClickL(new OnOperItemClickL() {
//            @Override
//            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    clearGroupChatRecorder(itemPosition, roomid);
//                }
//                dialog.dismiss();
//            }
//        });
//    }
//
//    //清除群聊记录
//    public void clearGroupChatRecorder(int itemPosition, String roomid) {
//        DataSupport.deleteAll(ChatMessage.class, "roomid=?", roomid);
//        GroupInfo tempGroup = new GroupInfo();
//        tempGroup.setContent("");
//        //清空群最新消息
//        tempGroup.saveOrUpdate("roomid=?", roomid);
//        //清楚群会话
//        dataList.remove(itemPosition);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                groupAdapter.notifyDataSetChanged();
//            }
//        }, 200);
//    }

}
