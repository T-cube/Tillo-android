package bean;

import java.io.Serializable;

/**
 * Created by yumeng on 2018/4/16.
 */

public class Group implements Serializable {
    private String _id;
    private String type;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
