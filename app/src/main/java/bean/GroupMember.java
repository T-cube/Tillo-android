package bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

import utils.PinYinUtils;

/**
 * 群成员列表
 */

public class GroupMember extends DataSupport implements Serializable {

    private String userId;
    private String name;
    private String avatar;
    private boolean delFlag;
    //拼音
    private String pinyin;
    //拼音首字母
    private String headerWord;
    //房间号
    private String roomId;
    public String getHeaderWord() {
        if (TextUtils.isEmpty(pinyin)) {
            this.pinyin = PinYinUtils.getPinyin(name);
        }
        headerWord = pinyin.substring(0, 1).toUpperCase();
        return headerWord;
    }

    public String getPinyin() {
        this.pinyin = PinYinUtils.getPinyin(name);
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void setHeaderWord(String headerWord) {
        this.headerWord = headerWord;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isDelFlag() {
        return delFlag;
    }

    public void setDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }
}
