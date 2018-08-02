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

    @JSONField(name = "_id")
    private String other_id;
    private String uid;
    private String type;
    private String status;
    private String name;
    private String avatar;
    private String groupid;
    //拼音
    private String pinyin;
    //拼音首字母
    private String headerWord;

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
    public String getOther_id() {
        return other_id;
    }

    public void setOther_id(String other_id) {
        this.other_id = other_id;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
