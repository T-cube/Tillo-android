package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;



import org.json.JSONObject;

import bean.UserInfo;
import pomelo.DataCallBack;
import pomelo.DataEvent;
import pomelo.DataListener;
import pomelo.PomeloClient;
import utils.AppSharePre;

/**
 * Socket 服务
 */

public class PomeloService extends Service {

    private PomeloServiceBinder pomeloServiceBinder = new PomeloServiceBinder();
    //ip
    private String ip = "192.168.1.27";
    //端口号
    private int port = 3014;
    //pomeloClient 客户端
    private PomeloClient pomeloClient;
    /*默认重连*/
    private boolean isReConnect = true;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initClient();
        return pomeloServiceBinder;
    }


    public class PomeloServiceBinder extends Binder {

        /**
         * 返回当前服务
         */
        public PomeloService getService() {
            return PomeloService.this;
        }
    }

    //启动服务


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //初始化pomeloClient
    public void initClient() {
        pomeloClient = new PomeloClient(ip, port);
        pomeloClient.init();
        pomeloClientRequest();
        pomeloClient.on("onChat", new DataListener() {
            @Override
            public void receiveData(DataEvent dataEvent) {


            }
        });

    }

    //建立连接
    public void pomeloClientRequest() {
        Log.e("TAG", "建立连接");
        UserInfo userInfo = AppSharePre.getPersonalInfo();
        try {
            JSONObject data = new JSONObject();
            data.put("token", userInfo.getAccess_token());
            data.put("uid", userInfo.getUid());
            pomeloClient.request("gate.gateHandler.queryEntry", data, new DataCallBack() {
                @Override
                public void responseData(JSONObject jsonObject) {
                    Log.e("TAG", "123");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    //关闭pomeloClient
    public void closeClient() {
        pomeloClient.disconnect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        closeClient();
    }


}
