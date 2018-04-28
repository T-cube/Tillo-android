package utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * IO流 工具类<br>
 * 很简单,仅支持文本操作
 */
public class IOUtils {

    /**
     * 文本的写入操作
     *
     * @param filePath 文件路径。一定要加上文件名字 <br>
     *         例如：../a/a.txt
     * @param content 写入内容
     * @param append  是否追加
     */
    public static void write(String filePath, String content, boolean append) {
        BufferedWriter bufw = null;
        try {
            bufw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath, append)));
            bufw.write(content);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (bufw != null) {
                try {
                    bufw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文本的读取操作
     *
     * @param path 文件路径,一定要加上文件名字<br>
     *       例如：../a/a.txt
     * @return
     */
    public static String read(String path) {
        BufferedReader bufr = null;
        try {
            bufr = new BufferedReader(new InputStreamReader(
                    new FileInputStream(path)));
            StringBuffer sb = new StringBuffer();
            String str = null;
            while ((str = bufr.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 文本的读取操作
     *
     * @param is 输入流
     * @return
     */
    public static String read(InputStream is) {
        BufferedReader bufr = null;
        try {
            bufr = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String str = null;
            while ((str = bufr.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * @param context 上下文
     * @param fileName 文件名
     * @return 字节数组
     */
    public static byte[] readBytes(Context context, String fileName) {
        FileInputStream fin = null;
        byte[] buffer = null;
        try {
            fin = context.openFileInput(fileName);
            int length = fin.available();
            buffer = new byte[length];
            fin.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                    fin = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    /**
     * 快速读取程序应用包下的文件内容
     *
     * @param context 上下文
     * @param filename 文件名称
     * @return 文件内容
     * @throws IOException
     */
    public static String read(Context context, String filename)
            throws IOException {
        FileInputStream inStream = context.openFileInput(filename);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        return new String(data);
    }


}
