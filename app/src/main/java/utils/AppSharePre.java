package utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

import application.MyApplication;
import bean.ChatUserBean;
import bean.UserInfo;
import constants.Constants;


/**
 * 用户登陆时获取3中类型
 * 1.是否第一次登陆
 * 2.用户账户信息
 * 3.用户配置信息
 */
public class AppSharePre {

    /**
     * 获取Token  【特殊】
     */
    public static String getToken() {
        UserInfo personalInfo = getPersonalInfo();
        return personalInfo.getToken();
    }

    /**
     * 注销   ★删除所有用户数据★
     */
    public static void clearAllUserData() {
        clearFirstlaunch();
        clearPersonalInfo();
    }

    /**
     * 第一次进入
     */
    public static void setFirstLaunch() {
        SharedPreferences sp = getSharePreferencesInstance();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.APP_FIRST_IN, true);
        editor.commit();
    }

    public static Boolean getFirstLaunch() {
        SharedPreferences sp = getSharePreferencesInstance();
        return sp.getBoolean(Constants.APP_FIRST_IN, false);
    }

    private static void clearFirstlaunch() {
        SharedPreferences sp = getSharePreferencesInstance();
        sp.edit().remove(Constants.APP_FIRST_IN).commit();
    }

    /**
     * 判断是不是第一次启动
     */
    public static boolean isFirstLaucher() {//
        SharedPreferences sp = getSharePreferencesInstance(); // 获得Preferences
        boolean isfirst = sp.getBoolean("isfirstlaucher", true);
        return isfirst;
    }

    /**
     * 更新第一次启动
     */
    public static void updateIsFirstLaucher(Boolean isfirst) {//
        SharedPreferences sp = getSharePreferencesInstance(); // 获得Preferences
        SharedPreferences.Editor editor = sp.edit(); // 获得Editor
        editor.putBoolean("isfirstlaucher", isfirst); // 将密码存入Preferences
        editor.commit();
    }

    /**
     * 账户信息
     */
    public static void setPersonalInfo(UserInfo info) {
        SharedPreferences sp = getSharePreferencesInstance();
        sp.edit().putString(Constants.APP_USER_INFO, JSON.toJSONString(info)).commit();
    }


//    /**
//     * 图片上传token
//     */
//    public static void setImageToken(UpLoadTokenInfo info) {
//        SharedPreferences sp = getSharePreferencesInstance();
//        sp.edit().putString(Constants.APP_IMAGE_TOKEN, JSON.toJSONString(info)).commit();
//    }
//
//    public static UpLoadTokenInfo getImageToken() {
//        SharedPreferences sp = getSharePreferencesInstance();
//        String infoString = sp.getString(Constants.APP_IMAGE_TOKEN, "");
//        if (TextUtils.isEmpty(infoString)) {
//            return null;
//        }
//        return JSON.parseObject(infoString, UpLoadTokenInfo.class);
//    }

    private static void clearPersonalInfo() {
        SharedPreferences sp = getSharePreferencesInstance();
        sp.edit().remove(Constants.APP_USER_INFO).commit();
    }


    public static UserInfo getPersonalInfo() {
        SharedPreferences sp = getSharePreferencesInstance();
        String infoString = sp.getString(Constants.APP_USER_INFO, "");
        if (TextUtils.isEmpty(infoString)) {
            return null;
        }
        return JSON.parseObject(infoString, UserInfo.class);
    }

    public static List<ChatUserBean> getRoomIdList() {
        SharedPreferences sp = getSharePreferencesInstance();
        String infoString = sp.getString(Constants.ROOM_ID_LIST, "");
        if (TextUtils.isEmpty(infoString)) {
            return null;
        }
        return JSON.parseArray(infoString, ChatUserBean.class);
    }

    public static void setRoomIdList(List<ChatUserBean> roomIdList) {
        SharedPreferences sp = getSharePreferencesInstance();
        sp.edit().putString(Constants.ROOM_ID_LIST, JSON.toJSONString(roomIdList)).commit();
    }

    public static void clearRoomIdList() {
        SharedPreferences sp = getSharePreferencesInstance();
        sp.edit().remove(Constants.ROOM_ID_LIST).commit();
    }

    /**
     * 获取sharePreference的对象
     */
    private static SharedPreferences getSharePreferencesInstance() {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(Constants.APPNAME, Activity.MODE_PRIVATE);
        return sp;
    }
}
