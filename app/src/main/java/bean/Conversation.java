package bean;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 会话表
 */

public class Conversation extends DataSupport {

    @Column(unique = true, nullable = false)
    private String roomid;
    private String content;//最后一条消息内容
    private long timestamp;//最后一条消息时间
    private int unread;//未读消息数
    @Column(ignore = true)
    private List<FriendInfo> friendInfo;

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

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

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public List<FriendInfo> getFriendInfo() {
        return DataSupport.where("roomid=?", roomid).find(FriendInfo.class);
    }

    public void setFriendInfo(List<FriendInfo> friendInfo) {
        this.friendInfo = friendInfo;
    }
}
