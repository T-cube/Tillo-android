package bean;

import java.io.Serializable;

/**
 * 好友相关 实体
 */

public class FriendBean implements Serializable {

    private String _id;
    private String name;
    private String avatar;
    private String recevier;
    private String from;
    private String mark;
    private String create_at;
    /**
     * STATUS_FRIEND_REQUEST_PADDING // 待处理
     * STATUS_FRIEND_REQUEST_AGREE   // 已同意
     * STATUS_FRIEND_REQUEST_REJECT  // 已拒绝
     * STATUS_FRIEND_REQUEST_IGNORE  // 已忽略
     */
    private String status;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String getRecevier() {
        return recevier;
    }

    public void setRecevier(String recevier) {
        this.recevier = recevier;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
