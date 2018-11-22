package bean;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * 会话表
 */

public class Conversation extends DataSupport implements Serializable {

    @Column(unique = true, nullable = false)
    private String roomId;
    private String content;//最后一条消息内容
    private long timestamp;//最后一条消息时间
    private int msgNum;//离线未读消息数
    private String avatar;
    private String name;
    private String sender;
    private String chatType;
    private String type;
    private int onlineMessage;//在线未读消息数
    @Column(ignore = true)
    private List<FriendInfo> friendInfo;
    private List<GroupInfo> groupInfo;
    private String owner;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<FriendInfo> getFriendInfo() {
        Log.e("TAG_roomid",roomId);
        return DataSupport.where("roomid=?", roomId).find(FriendInfo.class);
    }

    public void setFriendInfo(List<FriendInfo> friendInfo) {
        this.friendInfo = friendInfo;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOnlineMessage() {
        return onlineMessage;
    }

    public void setOnlineMessage(int onlineMessage) {
        this.onlineMessage = onlineMessage;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<GroupInfo> getGroupInfo() {
        return DataSupport.where("roomId=?", roomId).find(GroupInfo.class);
    }

    public void setGroupInfo(List<GroupInfo> groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}
