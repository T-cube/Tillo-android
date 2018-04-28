package bean;

import java.io.Serializable;
import java.util.List;

/**
 * 房间信息
 */

public class RoomBean implements Serializable {
    private String _id;
    private String roomid;//房间id
    private List<String> members;//房间成员
    private String createAt; //创建时间

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}
