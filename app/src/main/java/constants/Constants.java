package constants;

import java.util.List;

/**
 * Created by Administrator on 2018/3/21.
 */

public class Constants {
    public static final boolean Debug = true;//false为线上
    public static final String APP_FIRST_IN = "app_first_inf";
    public static final String APP_USER_INFO = "app_user_info";
    public static final String APP_IMAGE_TOKEN = "app_image_token";
    public static final String APP_USER_CONFIG = "app_user_config";
    public static final String APPNAME = "tshion";
    public static final String BaseIP = Debug ? "http://192.168.1.93:" : "http://192.168.1.177:";
    public static final String BaseUrl = BaseIP + "8001";
    public static final String FriendUrl = BaseIP + "8003";
    public static final String ChatUrl = BaseIP + "8002";
    public static final String GroupUrl = BaseIP + "8006";
    public static final String FileUrl = BaseIP + "8081";
    public static final String RTCUrl = "http://192.168.1.64:8009/push";
    public static final String APPLICATION_ID="com.yumeng.tillo";
    //    http://180.178.124.2:3333
    public static final String TSHION_URL = Debug ? "http://193.112.28.95:9999/api" : "http://119.28.43.121:9999/api";
    public static final String ROOM_ID_LIST = "chatUserBean";
    public static final String FileProviderAuthorities = "com.yumeng.tillo.fileprovider";//类名+fileprovider
    //ip
    public static final String SOCKET_IP = Debug ? "193.112.28.95" : "119.28.43.121";
    //端口号
    public static final int SOCKET_PORT = 3014;
    //EventBus 数据分发 标识
    public static final String MESSAGE_EVENT_HAS_MESSAGE = "updateMessageList";//收到新消息
    public static final String MESSAGE_EVENT_GROUP_HAS_MESSAGE = "updateMessageList";//群收到新消息
    public static final String MESSAGE_EVENT_FRIEND_NEW = "newFriend";//更新验证信息列表
    public static final String MESSAGE_EVENT_FRIEND_AGREE = "agreeWithYou";//更新好友列表
    public static final String MESSAGE_EVENT_NEW_FRIEND_REQUEST = "newFriendRequest";//新的好友请求
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
    public static final String MESSAGE_EVENT_CLEAR_RECODER = "clearRecoder";//清除聊天记录
    public static final String MESSAGE_EVENT_CLEAR_GROUP_RECODER = "clearGroupRecoder";//清除群记录
    public static final String MESSAGE_EVENT_JOIN_VOICE = "joinVoice";//对方同意加入房间
    public static final String MESSAGE_EVENT_FINISH_VOICE = "finishVoice";//对方拒绝加入房间
    public static final String MESSAGE_CHAT_DOT = "addDot";//添加消息红点
    public static final String MESSAGE_GROUP_CHAT_DOT = "addGroupDot";//添加消息红点
    public static final String MESSAGE_INIT_CLIENT = "initClient";//重连client
    public static final String MESSAGE_DISCONNECT_ERROR = "disconnectError";//未连接到socket
    public static final String MESSAGE_CONNECT_SUCCESS = "connectSuccess";//连接成功
    public static final String MESSAGE_EVENT_GROUP_INVITE = "groupInvite";//邀请入群
    public static final String TARGET_CHAT_FRAGMENT = "chatFragment";
    public static final String TARGET_GROUP_FRAGMENT = "groupFragment";
    public static final String TARGET_CHAT_ACTIVITY = "chatActivity";
    public static final String TARGET_VOICE_ACTIVITY = "voiceActivity";
    public static final String TARGET_VIDEO_ACTIVITY = "videoActivity";
    public static final String TARGET_GROUP_CHAT_ACTIVITY = "GroupChatActivity";
    public static final String TARGET_MAIN_ACTIVITY = "mainActivity";
    public static final String TARGET_VOICECALL_ACTIVITY = "voiceCallActivity";
    public static final String TARGET_VIDEOCALL_ACTIVITY = "videoCallActivity";
    public static final String TARGET_SERVICE = "PomeloService";

    /**
     * 接口地址
     */
    //获取短信验证码
    public static final String getSMS = "/getSmsCode";
    //注册
    public static final String register = "/register";
    //登陆
    public static final String login = "/login";
    //获取用户信息
    public static final String getUserInfo = "/getUserInfo";
    //修改用户信息
    public static final String alertUserInfo = "/api/user/info";
    //上传头像
    public static final String uploadImage = "/api/user/avatar/upload";
    //忘记密码
    public static final String forgetPassword = "/forget";

    /**
     * 发送聊天消息
     */


    /**
     * 聊天接口
     */
    public static final String searchUser = "/searchFriend?";
    //发送添加好友请求
    public static final String addFriendRequest = "/addFriend";
    //拉取好友请求列表
    public static final String getFriendRequest = "/searchRequest?";
    //操作好友请求
    public static final String passFriendRequest = "/passFriend";
    //获取好友列表
    public static final String getFriendList = "/friends";
    //修改好友备注
    public static final String alertRemark = "/updateFriend";
    //获取组的好友列表
    public static final String getGroup = "/user/friends/info/";
    //获取聊天记录
    public static final String getHistory = "/message/";
    //上传文件
    public static final String uploadFile = "/file";
    //下载音频文件
    public static final String downloadFile = "/getFile";
    //获取缩略图
    public static final String getThumbnail = "/file/image/thumbnail";
    //获取原图
    public static final String getImage = "/file/image/view";
    //上传头像
//    public static final String uploadHead = "/api/user/avatar/upload";
    //修改个人资料
    public static final String alertPersonInfo = "/updateInfo";
    //获取好友信息
    public static final String getFriendInfo = "/user/";
    //修改好友信息
    public static final String alertFriendInfo = "/user/friend/info/";
    //创建群
    public static final String createGroup = "/createGroup";
    //获取成员列表
    public static final String getMemberList = "/getGroupMember";
    //添加群成员
    public static final String addGroupMember = "/addGroupMember";
    //修改群名称
    public static final String alertGroupName = "/updateGroupName";
    //获取群会话列表
    public static final String getGroupSessionList = "/group/session/all";
    //拉取会话列表
    public static final String getChatSessionList = "/getOfflineSession";
    //获取离线消息
    public static final String getOfflineMessageList = "/getOfflineMsg";
    //获取群离线消息
    public static final String getGroupOfflineMessageList = "/pullGroupMsg";
    //屏蔽好友
    public static final String blockFriend = "/user/friend/block/";
    //群免打扰
    public static final String groupDisturb = "/group/distub/";
    //删除联系人
    public static final String deleteLinkman = "/deleteFriend";
    //删除群成员
    public static final String deleteGroupMember = "/delGroupMember";
    //退群
    public static final String quitGroup = "/exitGroup";
    //发起加入房间邀请
    public static final String sendRoomInvite = "/user/room-request";
    //同意加入房间
    public static final String agreeRoomInvite = "/user/room-request-feedback";
    //保存设备token
    public static final String saveDeviceToken = "/saveDeviceToken";
    //获取群列表
    public static final String getGroupList = "/getGroupList";

    /**
     * RTC相关
     */
    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "org.appspot.apprtc.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    //对等连接统计数据回调周期。
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};
}
