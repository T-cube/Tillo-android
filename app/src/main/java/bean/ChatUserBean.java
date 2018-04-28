package bean;

import java.io.Serializable;

/**
 * 用户 id 、房间id
 */

public class ChatUserBean implements Serializable {
    private String userId;
    private String roomId;

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
}
