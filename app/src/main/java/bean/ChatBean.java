package bean;

import java.io.Serializable;

/**
 * 消息实体
 */

public class ChatBean implements Serializable {
    private RoomBean room;
    private ChatMessage message;

    public RoomBean getRoom() {
        return room;
    }

    public void setRoom(RoomBean room) {
        this.room = room;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }
}
