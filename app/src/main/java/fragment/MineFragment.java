package fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.CameraSelectorActivity;
import com.yumeng.tillo.PersonInfoActivity;
import com.yumeng.tillo.R;
import com.yumeng.tillo.SettingActivity;
import com.yumeng.tillo.UpLoadImageActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.MyApplication;
import base.BaseFragment;
import bean.UserInfo;
import constants.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;
import utils.AppSharePre;
import utils.PhotoBitmapUtils;
import utils.Utils;
import utils.fileutil.FileUtils;

import static android.app.Activity.RESULT_OK;

/**
 * 我的
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {
    private UserInfo userInfo;
    private CircleImageView headCiv;
    private TextView nameTv;
    private String fileForderPath;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private String imagePah;
    private Dialog progressDialog;
    private RxPermissions rxPermissions;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private LinearLayout settingLl;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        userInfo = AppSharePre.getPersonalInfo();
        rxPermissions = new RxPermissions(getActivity());
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getUid();
        boolean makeFolderState = FileUtils.makeFolders(fileForderPath);
        imagePah = fileForderPath + File.separator + userInfo.getUid() + ".jpg";
        headCiv = myFindViewsById(R.id.mine_civ_head);
        headCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择头像
                Intent personInfoIntent = new Intent(getActivity(), PersonInfoActivity.class);
                MineFragment.this.startActivityForResult(personInfoIntent, 1);
            }
        });
        nameTv = myFindViewsById(R.id.mine_tv_name);
        settingLl = myFindViewsById(R.id.mine_ll_setting);
        settingLl.setOnClickListener(this);
        nameTv.setText(userInfo.getName());
        initLoadingDialog();
        if (userInfo.getAvatar() != null) {
            //如果用户头像不为空，下载用户头像
            rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                downloadHeadImage(userInfo.getAvatar(), userInfo.getUid(), headCiv);
                            }
                        }
                    });

        }
    }

    /**
     * 发送图片相关
     */

    //拍照-相册选取 对话框
    public void initPickPhotoDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.AlertDialogStyle);
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_photo, null);
//        //初始化控件
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体从中间弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);
        dialog.show();//显示对话框
        RelativeLayout albumPickRl = Utils.findViewsById(inflate, R.id.choose_photo_rl_choose_photo);
        RelativeLayout takePhotoRl = Utils.findViewsById(inflate, R.id.choose_photo_rl_take_photo);
        RelativeLayout cancelRl = Utils.findViewsById(inflate, R.id.choose_photo_rl_cancel);
        albumPickRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //相册选取
                rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                //权限已经开启   enableCrop:是否裁剪
                                if (aBoolean) {
                                    new Matisse(getActivity(), MineFragment.this)
                                            .choose(MimeType.ofImage())
                                            .countable(false)
                                            .theme(R.style.Matisse_Dracula)
                                            .maxSelectable(1)
                                            .imageEngine(new GlideEngine())
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    dialog.dismiss();
                                } else {
                                    //未开启权限，弹出提示框

                                }


                            }
                        });
            }
        });
        takePhotoRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拍照
                rxPermissions.request(android.Manifest.permission.CAMERA)
                        .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                //权限已经开启   enableCrop:是否裁剪
                                if (aBoolean) {
                                    CameraSelectorActivity.start(getActivity(), false);
                                    dialog.dismiss();
                                } else {
                                    //未开启权限，弹出提示框

                                }


                            }
                        });
                dialog.dismiss();

            }
        });
        cancelRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消
                dialog.dismiss();
            }
        });


    }

    //下载头像
    public void downloadHeadImage(String tempAvatar, String targetId, CircleImageView imageView) {
        if (TextUtils.isEmpty(tempAvatar))
            return;
        progressDialog.show();
        String fileForderPath = utils.fileutil.FileUtils.getSDPath() + File.separator + targetId;
        boolean makeFolderState = utils.fileutil.FileUtils.makeFolders(fileForderPath);
        OkGo.<File>get(tempAvatar)
                .execute(new FileCallback(fileForderPath, targetId + ".jpg") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        progressDialog.dismiss();
                        File file = response.body();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //加载
                                Glide.with(MyApplication.getContext())
                                        .load(file.getAbsolutePath())
                                        .error(R.drawable.head)
                                        .into(imageView);
                            }
                        }, 200);
                    }

                    @Override
                    public void onError(Response<File> response) {
                        progressDialog.dismiss();
                    }
                });
    }

    public boolean headImageIsExist() {
        File headImage = new File(fileForderPath, userInfo.getUid() + ".jpg");
        if (headImage.exists())
            return true;
        else
            return false;
    }

    public void initLoadingDialog() {
        progressDialog = new Dialog(getActivity(), R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog_loading);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.dialog_tv_tips);
        msg.setText("信息加载中");
    }

    //上传图片
    public void uploadHeadImage(String imagePath) {
        progressDialog.show();
        UserInfo userInfo = AppSharePre.getPersonalInfo();
        final File file = new File(imagePath);
        OkGo.<String>put(Constants.BaseUrl + Constants.uploadHead)
                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                .params("avatar", file)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        progressDialog.dismiss();
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                        com.alibaba.fastjson.JSONObject dataObject = JSON.parseObject(jsonObject.getString("data"));
                        String avatar = dataObject.getString("avatar");
                        userInfo.setAvatar(avatar);
                        AppSharePre.setPersonalInfo(userInfo);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(MyApplication.getContext()).load(imagePath)
                                        .error(R.drawable.head)
                                        .into(headCiv);
                            }
                        }, 200);

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        progressDialog.dismiss();
                    }
                });
    }

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        String imagePath = "";
//        //相册选取回调
//        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
//            List<Uri> mSelected = Matisse.obtainResult(data);
//            imagePath = utils.FileUtils.getRealFilePath(getActivity(), mSelected.get(0));
//            //上传头像
//            uploadHeadImage(imagePath);
//        } else if (resultCode == RESULT_OK && requestCode == CameraSelectorActivity.REQUEST_IMAGE) {
//            //拍照完成回调
//            ArrayList<String> images = (ArrayList<String>) data.getSerializableExtra(CameraSelectorActivity.REQUEST_OUTPUT);
//            imagePath = PhotoBitmapUtils.amendRotatePhoto(images.get(0), getActivity(), 2);
//            //上传头像
//            uploadHeadImage(imagePath);
//        }
//    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mine_ll_setting:
                //个人设置
                Intent settingIntent = new Intent(getActivity(), SettingActivity.class);
                startActivity(settingIntent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String tempavatar = data.getStringExtra("avatar");
            //如果用户头像不为空，下载用户头像
            if (!TextUtils.isEmpty(tempavatar))
                Glide.with(MyApplication.getContext()).load(tempavatar)
                        .error(R.drawable.head)
                        .into(headCiv);
            userInfo = AppSharePre.getPersonalInfo();
            nameTv.setText(userInfo.getName());
        }

    }

}
