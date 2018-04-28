package bean;

/**
 * Created by Administrator on 2018/3/27.
 */

public class Recorder {
    float time;//时间长度
    String filePath;//文件路径

    public Recorder(float time, String filePath) {
        super();
        this.time = time;
        this.filePath = filePath;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
