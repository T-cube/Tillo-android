package bean;


import android.text.TextUtils;

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
    @JSONField(name = "_id")
    private String friend_id;
    private String email;
    private String mobile;
    private String name;
    private String avatar;
    private String nickname;
    private String user;
    private String showname;
    @Column(unique = true)
    private String roomid;
    //    public AddressBookBean() {
//        this.pinyin = PinYinUtils.getPinyin(showname);
//        headerWord = pinyin.substring(0, 1);
//    }

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
            this.pinyin = PinYinUtils.getPinyin(showname);
        }
        headerWord = pinyin.substring(0, 1).toUpperCase();
        return headerWord;
    }
    public String getPinyin() {
        this.pinyin = PinYinUtils.getPinyin(showname);
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
        return showname;
    }

    public void setShowname(String showname) {
        this.showname = showname;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }
}
