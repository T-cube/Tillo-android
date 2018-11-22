package bean;


import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

import utils.PinYinUtils;

/**
 * 好友信息 - 数据库 好友表
 */

public class FriendInfo extends DataSupport implements Serializable {
    //拼音
    private String pinyin;
    //拼音首字母
    private String headerWord;
    @JSONField(name = "id")
    private String friend_id;
    private String email;
    private String mobile;
    private String name;
    private String avatar;
    private String nickname;
    private String user;
    private String showName;
    @Column(unique = true)
    private String roomId;
    private String istop;//0、不置顶 1、置顶
    private String isshield;//0、不屏蔽 1、屏蔽
    @Column(ignore = true)
    private Setting settings;
    private String block;
    private String not_disturb;

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getNot_disturb() {
        return not_disturb;
    }

    public void setNot_disturb(String not_disturb) {
        this.not_disturb = not_disturb;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeaderWord() {
        if (TextUtils.isEmpty(pinyin)) {
            this.pinyin = PinYinUtils.getPinyin(showName);
        }
        headerWord = pinyin.substring(0, 1).toUpperCase();
        return headerWord;
    }

    public String getPinyin() {
        this.pinyin = PinYinUtils.getPinyin(showName);
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void setHeaderWord(String headerWord) {
        this.headerWord = headerWord;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getShowname() {
        return showName;
    }

    public void setShowname(String showname) {
        this.showName = showname;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getIstop() {
        return istop;
    }

    public void setIstop(String istop) {
        this.istop = istop;
    }

    public String getIsshield() {
        return isshield;
    }

    public void setIsshield(String isshield) {
        this.isshield = isshield;
    }

    public Setting getSettings() {
        return settings;
    }

    public void setSettings(Setting settings) {
        this.settings = settings;
    }

    public void friendToString() {
        Log.e("TAG_friendToString", "friend_id" + friend_id + "showname" + showName + "mobile" + mobile+"roomid"+roomId);
    }
}
