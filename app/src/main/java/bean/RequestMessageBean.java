package bean;

import java.io.Serializable;

/**
 * Created by yumeng on 2018/4/8.
 */

public class RequestMessageBean implements Serializable {
    private String token;
    private String uid;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
