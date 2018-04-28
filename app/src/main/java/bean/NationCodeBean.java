package bean;

import java.io.Serializable;

/**
 * Created by yumeng on 2018/4/27.
 */

public class NationCodeBean implements Serializable {
    private String name;
    private String cn_name;
    private String dial_code;
    private String code;
    private String headWord;
    private String price;

    public String getCn_name() {
        return cn_name;
    }

    public void setCn_name(String cn_name) {
        this.cn_name = cn_name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDial_code() {
        return dial_code;
    }

    public void setDial_code(String dial_code) {
        this.dial_code = dial_code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHeadWord() {
        return name.substring(0, 1).toUpperCase();
    }

    public void setHeadWord(String headWord) {
        this.headWord = headWord;
    }

}
