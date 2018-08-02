package constants;

import java.util.List;

/**
 * Created by Administrator on 2018/3/21.
 */

public class Constants {
    public static final boolean Debug = false;//false为线上
    public static final String APP_FIRST_IN = "app_first_inf";
    public static final String APP_USER_INFO = "app_user_info";
    public static final String APP_IMAGE_TOKEN = "app_image_token";
    public static final String APP_USER_CONFIG = "app_user_config";
    public static final String APPNAME = "tshion";
    public static final String BaseUrl = Debug ? "http://192.168.100.42:3333" : "http://119.28.43.121:3333";
    //    http://180.178.124.2:3333
    public static final String TSHION_URL = Debug ? "http://192.168.100.42:9999/api" : "http://119.28.43.121:9999/api";
    public static final String ROOM_ID_LIST = "chatUserBean";
    public static final String FileProviderAuthorities = "com.yumeng.tillo.fileprovider";//类名+fileprovider
    //ip
    public static final String SOCKET_IP = Debug ? "192.168.100.42" : "119.28.43.121";
    //端口号
    public static final int SOCKET_PORT = 3014;
    //EventBus 数据分发 标识
    public static final String MESSAGE_EVENT_HAS_MESSAGE = "updateMessageList";//收到新消息
    public static final String MESSAGE_EVENT_GROUP_HAS_MESSAGE = "updateMessageList";//群收到新消息
    public static final String MESSAGE_EVENT_FRIEND_NEW = "newFriend";//更新验证信息列表
    public static final String MESSAGE_EVENT_FRIEND_AGREE = "agreeWithYou";//更新好友列表
    public static final String MESSAGE_EVENT_UPDATE_GROUP = "updateGroupList";//更新群列表
    public static final String MESSAGE_EVENT_UPDATE_GROUP_SESSION = "updateGroupSession";//更新群会话列表
    public static final String MESSAGE_EVENT_UPDATE_AUDIO = "updateAudio";//更新语音消息
    public static final String MESSAGE_EVENT_UPDATE_GROUP_AUDIO = "updateGroupAudio";//更新群语音消息
    public static final String MESSAGE_EVENT_INIT_CONVERSATION = "updateConversationList";//更新会话列表
    public static final String MESSAGE_EVENT_UPDATE_OFFLINE_MESSAGE = "updateOfflineMessage";//更新离线消息
    public static final String MESSAGE_EVENT_UPDATE_GROUP_OFFLINE_MESSAGE = "updateGroupOfflineMessage";//更新群离线消息
    public static final String MESSAGE_EVENT_ALERT_GROUP_NAME = "alertGroupName";
    public static final String MESSAGE_EVENT_CLEAR_OFFLINE = "clearOffline";//清除未读消息数
    public static final String MESSAGE_EVENT_CLEAR_GROUP_OFFLINE = "clearOffline";//清除未读消息数
    public static final String MESSAGE_INIT_CLIENT = "initClient";//重连client
    public static final String TARGET_CHAT_FRAGMENT = "chatFragment";
    public static final String TARGET_GROUP_FRAGMENT = "groupFragment";
    public static final String TARGET_CHAT_ACTIVITY = "chatActivity";
    public static final String TARGET_GROUP_CHAT_ACTIVITY = "GroupChatActivity";
    public static final String TARGET_MAIN_ACTIVITY = "mainActivity";
    public static final String TARGET_SERVICE = "PomeloService";

    /**
     * 接口地址
     */
    //获取短信验证码
    public static final String getSMS = "/api/account/send-sms";
    //注册
    public static final String register = "/api/account/register";
    //登陆
    public static final String login = "/oauth/token";
    //获取用户信息
    public static final String getUserInfo = "/api/user/info";
    //修改用户信息
    public static final String alertUserInfo = "/api/user/info";
    //上传头像
    public static final String uploadImage = "/api/user/avatar/upload";


    /**
     * 聊天接口
     */
    public static final String searchUser = "/user";
    //发送添加好友请求
    public static final String addFriendRequest = "/user/friend-request";
    //拉取好友请求列表
    public static final String getFriendRequest = "/user/friend-request/receiver/";
    //操作好友请求
    public static final String operationRequest = "/user/friend-request/";
    //获取好友列表
    public static final String getFriendList = "/friend/list";
    //获取组的好友列表
    public static final String getGroup = "/user/friends/info/";
    //获取聊天记录
    public static final String getHistory = "/message/";
    //上传文件
    public static final String uploadFile = "/file";
    //下载音频文件
    public static final String downloadFile = "/file/audio/";
    //获取缩略图
    public static final String getThumbnail = "/file/image/thumbnail";
    //获取原图
    public static final String getImage = "/file/image/view";
    //上传头像
    public static final String uploadHead = "/api/user/avatar/upload";
    //修改个人资料
    public static final String alertPersonInfo = "/api/user/info";
    //获取好友信息
    public static final String getFriendInfo = "/user/";
    //修改好友信息
    public static final String alertFriendInfo = "/user/friend/info/";
    //创建群
    public static final String createGroup = "/group";
    //获取成员列表
    public static final String getMemberList = "/group/members/";
    //添加群成员
    public static final String addGroupMember = "/group/member/add/";
    //修改群名称
    public static final String alertGroupName = "/group/modify/";
    //获取群会话列表
    public static final String getGroupSessionList = "/group/session/all";
    //拉取会话列表
    public static final String getChatSessionList = "/chat/session";
    //获取离线消息
    public static final String getOfflineMessageList = "/message/offline/history/";

}
