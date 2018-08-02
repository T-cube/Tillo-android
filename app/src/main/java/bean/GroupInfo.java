package bean;

import com.alibaba.fastjson.annotation.JSONField;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * 群信息
 */

public class GroupInfo extends DataSupport implements Serializable {
    @JSONField(name = "_id")
    private String group_id;
    private String name;//群名称
    private String creator;//创建者
    private String owner;//群主
    @Column(unique = true)
    private String roomid;//房间号
    private String create_at;//创建时间
    @Column(ignore = true)
    private List<GroupMember> groupMembers;
    @Column(ignore = true)
    private ChatMessage message;
    private String content;//消息内容
    @JSONField(name = "_offline_count")
    private int offline_count;//未读消息
    private int onlineMessage;//在线未读消息数
    private String avatar;

    public List<GroupMember> getGroupMembers() {
        return DataSupport.where("group_id=?", group_id).find(GroupMember.class);
    }

    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public int getOffline_count() {
        return offline_count;
    }

    public void setOffline_count(int offline_count) {
        this.offline_count = offline_count;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getOnlineMessage() {
        return onlineMessage;
    }

    public void setOnlineMessage(int onlineMessage) {
        this.onlineMessage = onlineMessage;
    }
}
