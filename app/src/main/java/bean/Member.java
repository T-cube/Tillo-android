package bean;

import java.io.Serializable;

/**
 * 成员实体
 */

public class Member implements Serializable {

    private String _id;//好友id
    private String name;//名称
    private String avatar;//头像

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
}
