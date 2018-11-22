package bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * block 屏蔽 not_disturb 免打扰
 */

public class Setting extends DataSupport implements Serializable {
    private int block;//0、不屏蔽 1、屏蔽
    private int not_disturb;//0、正常接收1、免打扰

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getNot_disturb() {
        return not_disturb;
    }

    public void setNot_disturb(int not_disturb) {
        this.not_disturb = not_disturb;
    }
}
