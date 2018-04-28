package bean;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;


/**
 * 房间实体 -对应数据库 房间表
 */

public class Room extends DataSupport implements Serializable {
    @Column(unique = true, nullable = false)
    private String roomid; //房间Id

    private String memberid;//房间 好友id

    private String type;//房间类型: 房间 和群组
    private String groupid;//组id
    private FriendInfo friend;//好友信息

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getMemberid() {
        return memberid;
    }

    public void setMemberid(String memberid) {
        this.memberid = memberid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public FriendInfo getFriend() {
        return friend;
    }

    public void setFriend(FriendInfo friend) {
        this.friend = friend;
    }
//    public List<ChatMessage> getChatMessages() {
//        return chatMessages;
////        return DataSupport.where("roomid = ?", String.valueOf(roomid)).find(ChatMessage.class);
//    }
//
//    public void setChatMessages(List<ChatMessage> chatMessages) {
//        this.chatMessages = chatMessages;
//    }
}
