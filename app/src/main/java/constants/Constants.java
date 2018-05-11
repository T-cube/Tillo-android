package constants;

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
    public static final String BaseUrl = Debug ? "http://180.178.124.2:3333" : "http://192.168.1.237:3333";
    public static final String TSHION_URL = Debug ? "http://180.178.124.2:9999/api" : "http://192.168.1.237:9999/api";
    public static final String ROOM_ID_LIST = "chatUserBean";
    public static final String FileProviderAuthorities = "com.qihuo.tshion.fileprovider";//类名+fileprovider
    //ip
    public static final String SOCKET_IP = Debug ? "180.178.124.2" : "192.168.1.237";
    //端口号
    public static final int SOCKET_PORT = 3014;
    //EventBus 数据分发 标识
    public static final String MESSAGE_EVENT_HAS_MESSAGE = "updateMessageList";//收到新消息
    public static final String MESSAGE_EVENT_FRIEND_NEW = "newFriend";//更新验证信息列表
    public static final String MESSAGE_EVENT_FRIEND_AGREE = "agreeWithYou";//更新好友列表
    public static final String MESSAGE_EVENT_INIT_CONVERSATION = "updateConversationList";//更新会话列表
    public static final String TARGET_CHAT_FRAGMENT = "chatFragment";
    public static final String TARGET_CHAT_ACTIVITY = "chatActivity";
    public static final String TARGET_MAIN_ACTIVITY = "mainActivity";

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
    public static final String getFriendList = "/user/friends/";
    //获取组的好友列表
    public static final String getGroup = "/user/friends/info/";
    //获取聊天记录
    public static final String getHistory = "/message/";
    //上传文件
    public static final String uploadFile = "/file";
    //下载音频文件
    public static final String downloadFile = "/file/audio/";
}
