package bean;

import java.io.Serializable;

/**
 * Created by yumeng on 2018/6/29.
 */

public class Address implements Serializable {
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
