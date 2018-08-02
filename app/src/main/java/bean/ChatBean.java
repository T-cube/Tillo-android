package bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 消息实体
 */

public class ChatBean implements Serializable {
    private Member member;
    private ChatMessage message;
    @JSONField(name = "_offline_count")
    private int unread;//未读消息数

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }
}
