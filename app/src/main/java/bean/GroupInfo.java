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
    private String name;//群名称
    private String creator;//创建者
    private String owner;//群主
    @Column(unique = true)
    private String roomId;//房间号
    @Column(ignore = true)
    private List<GroupMember> groupMembers;
    @Column(ignore = true)
    private ChatMessage message;
//    private String content;//消息内容
//    @JSONField(name = "_offline_count")
//    private int offline_count;//未读消息
//    private int onlineMessage;//在线未读消息数
    private String avatar;
    private String isTop;//是否置顶
    @Column(ignore = true)
    private Setting settings;
    private String block;//0、不屏蔽 1、屏蔽
    private String not_disturb;//0、正常接收 1、免打扰

    public List<GroupMember> getGroupMembers() {
        return DataSupport.where("roomid=?", roomId).find(GroupMember.class);
    }

    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public Setting getSettings() {
        return settings;
    }

    public void setSettings(Setting settings) {
        this.settings = settings;
    }

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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
