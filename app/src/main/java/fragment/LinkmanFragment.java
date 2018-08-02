package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.AddFriendActivity;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.ChooseLinkmanActivity;
import com.yumeng.tillo.GroupListActivity;
import com.yumeng.tillo.MainActivity;
import com.yumeng.tillo.R;
import com.yumeng.tillo.VerifyInfoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import adapter.AddressBookAdapter;
import base.BaseFragment;
import bean.FriendInfo;
import bean.Group;
import bean.MessageEvent;
import bean.Room;
import bean.UserInfo;
import constants.Constants;
import utils.AppSharePre;
import utils.CommonPopupWindow;
import utils.ToastUtils;
import utils.Utils;
import views.WordsNavigation;

/**
 * 联系人界面
 */

public class LinkmanFragment extends BaseFragment implements WordsNavigation.onWordsChangeListener, AbsListView.OnScrollListener {
    private ListView addressBookLv;
    private List<FriendInfo> mList = new ArrayList<>();
    private AddressBookAdapter addressBookAdapter;
    private WordsNavigation wordsNavigation;
    private TextView centerTipsTv;
    private Handler handler;
    private View headView;
    private UserInfo userInfo;
    String groupId = "";
    private int oldIndex, newIndex;
    private boolean isFirstScroll;
    private LinearLayout toolbarLl, groupLl;
    private ImageView addFriendIv;
    private LinearLayout verifyLl;
    private ImageView addIv;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_linkman;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        userInfo = AppSharePre.getPersonalInfo();
        toolbarLl = Utils.findViewsById(view, R.id.linkman_search_toolbar);
        addFriendIv = Utils.findViewsById(view, R.id.search_iv_add_friend);
        wordsNavigation = Utils.findViewsById(view, R.id.menu_wn_word);
        centerTipsTv = Utils.findViewsById(view, R.id.menu_tv_tips);
        addressBookLv = Utils.findViewsById(view, R.id.menu_lv_list);
        addIv = Utils.findViewsById(view, R.id.search_iv_add_friend);

        addIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReminder(addIv);
            }
        });
        getFriendList();
        initListView(view);
    }

    public void initListView(View view) {
        addressBookAdapter = new AddressBookAdapter(getActivity());
        addressBookLv.setAdapter(addressBookAdapter);
        headView = LayoutInflater.from(getActivity()).inflate(R.layout.address_list_headview, null);
        verifyLl = Utils.findViewsById(headView, R.id.headview_ll_add_friend);
        verifyLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到验证信息界面
                Intent verifyIntent = new Intent(getActivity(), VerifyInfoActivity.class);
                startActivityForResult(verifyIntent, 1);
            }
        });
        groupLl = Utils.findViewsById(headView, R.id.headview_ll_group);
        groupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到群列表
                Intent groupListIntent = new Intent(getActivity(), GroupListActivity.class);
                startActivity(groupListIntent);
            }
        });
        addressBookLv.addHeaderView(headView);
        addressBookLv.setOnScrollListener(this);
        addressBookLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击进入聊天界面
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("friendInfo", mList.get(position-1));
                startActivity(chatIntent);
            }
        });
        EventBus.getDefault().register(this);
    }

    //获取好友列表
    public void getFriendList() {
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
                            addressBookAdapter.setDatas(mList);

                        } else {
                            ToastUtils.getInstance().shortToast(message);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

//    //获取好友列表
//    public void getFriendList() {
//        OkGo.<String>get(Constants.TSHION_URL + Constants.getFriendList + userInfo.getUid())
//                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
//                .execute(new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
//                        String message = jsonObject.getString("message");
//                        int status = jsonObject.getIntValue("status");
//                        com.alibaba.fastjson.JSONObject dataJson = jsonObject.getJSONObject("data");
//                        if (status == 200) {
//                            List<Group> groups = JSON.parseArray(dataJson.getString("groups"), Group.class);
//                            if (groups.size() > 0) {
//                                groupId = groups.get(0).get_id();
//                                getGroupInfo();
//                            }
//                        } else {
//                            ToastUtils.getInstance().shortToast(message);
//                        }
//
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//    }
//
//    //获取组信息
//    public void getGroupInfo() {
//        OkGo.<String>get(Constants.TSHION_URL + Constants.getGroup + userInfo.getUid() + "/" + groupId)
//                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
//                .execute(new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
//                        String message = jsonObject.getString("message");
//                        int status = jsonObject.getIntValue("status");
//                        if (status == 200) {
//                            //获取好友列表
//                            mList = JSON.parseArray(jsonObject.getString("data"), FriendInfo.class);
//                            //对集合排序
//                            Collections.sort(mList, new Comparator<FriendInfo>() {
//                                @Override
//                                public int compare(FriendInfo lhs, FriendInfo rhs) {
//                                    //根据拼音进行排序
//                                    return lhs.getPinyin().compareTo(rhs.getPinyin().toUpperCase());
//                                }
//                            });
//                            //将列表存入数据库
//                            updateFriendList(mList);
//                            addressBookAdapter.setDatas(mList);
//
//                        } else {
//                            ToastUtils.getInstance().shortToast(message);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
//
//
//    }

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
//        updateRoomInfo(dataList);
    }

//    //创建房间信息
//    public void updateRoomInfo(List<FriendInfo> dataList) {
//        if (dataList == null)
//            return;
//        for (FriendInfo friendInfo : dataList) {
//            Room tempRoom = new Room();
//            tempRoom.setFriend(friendInfo);
//            tempRoom.setMemberid(friendInfo.getFriend_id());
//            tempRoom.setType("room");
//            tempRoom.setRoomid(friendInfo.getRoomid());
//            tempRoom.saveOrUpdate("roomid=?", friendInfo.getRoomid());
//        }
//
//    }

    @Override
    public void wordsChange(String words) {
        updateWord(words);
        updateListView(words);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_TOUCH_SCROLL:
                oldIndex = view.getLastVisiblePosition();
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //当滑动列表的时候，更新右侧字母列表的选中状态
        if (mList.size() > 0) {
            wordsNavigation.setTouchIndex(mList.get(firstVisibleItem).getHeaderWord());
            newIndex = view.getLastVisiblePosition();
            if (newIndex > oldIndex && firstVisibleItem > 0) {
                Log.e("TAG","滑动中");
                isFirstScroll = true;
                headView.setVisibility(View.GONE);
            } else if (firstVisibleItem == 0 && isFirstScroll == true) {
                //往上滑
                headView.setVisibility(View.VISIBLE);
            }
//            if (visibleItemCount + firstVisibleItem == totalItemCount) {
//                footView.setVisibility(View.VISIBLE);
//            } else {
//                footView.setVisibility(View.GONE);
//            }
        }
    }

    /**
     * @param words 首字母
     */
    private void updateListView(String words) {
        for (int i = 0; i < mList.size(); i++) {
            String headerWord = mList.get(i).getHeaderWord();
            //将手指按下的字母与列表中相同字母开头的项找出来
            if (words.equals(headerWord)) {
                addressBookLv.setSelection(i);
                //判断当前滑动距离 是否隐藏头部
                newIndex = addressBookLv.getLastVisiblePosition();
                if (newIndex > oldIndex && addressBookLv.getFirstVisiblePosition() > 0) {
                    isFirstScroll = true;
                    headView.setVisibility(View.GONE);
                } else if (addressBookLv.getFirstVisiblePosition() == 0 && isFirstScroll == true) {
                    //往上滑
                    headView.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }

    /**
     * 更新中央的字母提示
     *
     * @param words 首字母
     */
    private void updateWord(String words) {
        centerTipsTv.setText(words);
        centerTipsTv.setVisibility(View.VISIBLE);
        //清空之前的所有消息
        handler.removeCallbacksAndMessages(null);
        //1s后让tv隐藏
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                centerTipsTv.setVisibility(View.GONE);
            }
        }, 500);

    }


    private CommonPopupWindow popupWindow;

    public void showReminder(View view) {
        if (popupWindow != null && popupWindow.isShowing()) return;
        popupWindow = new CommonPopupWindow.Builder(getActivity())
                .setView(R.layout.popmenu_chat)
                .setWidthAndHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        View inflate = popupWindow.getContentView();
        View toChatGroup = Utils.findViewsById(inflate, R.id.menu_ll_to_group);
        View toAddFriend = Utils.findViewsById(inflate, R.id.menu_ll_to_add_friend);
        View toScan = Utils.findViewsById(inflate, R.id.menu_ll_to_scan);
        toChatGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发起群聊
                Intent groupIntent = new Intent(getActivity(), ChooseLinkmanActivity.class);
                groupIntent.putExtra("friendList", (Serializable) mList);
                startActivity(groupIntent);
            }
        });

        toAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加好友
                Intent addFriendIntent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(addFriendIntent);
                popupWindow.dismiss();
            }
        });
        toScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //扫一扫
            }
        });
        popupWindow.showAsDropDown(view, 0, 0);
    }


    /**
     * EventBus消息接收
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventReceiver(MessageEvent messageEvent) {
        String target = messageEvent.getTarget();
        String behavior = messageEvent.getBehavior();
        if (target.equals(Constants.TARGET_MAIN_ACTIVITY)) {
            if (behavior.equals(Constants.MESSAGE_EVENT_FRIEND_AGREE))
                getFriendList();

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1)
                getFriendList();
        }
    }
}
