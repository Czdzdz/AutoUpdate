package com.cdd.autoupdatemodel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2017/4/15 0015 23:41.
 * <p>
 * From url:
 */

public class UpdateManager {
    private static final int DOWNLOAD = 1;  //下载中
    private static final int DOWNLOAD_FINISH = 2;   //下载完成
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    private String fileName = "filename.apk";
    //获取存储卡的路径
    String savePath = SDUtils.getRootDirectory() + "/download";
    //获取服务端最新版本的版本号
    private String url = "";
    //服务端Apk文件地址
    String baseUrl = "http://180.153.105.143/imtt.dd.qq.com/16891/433949400FC6E29FDE9E209099BFE5BC.apk?mkey=58f27a0e81a5a642&f=4250&c=0&fsname=com.tencent.mobileqq_6.7.1_500.apk&csr=1bbd&p=.apk";

    private boolean cancleUpdate;   //标记更新状态
    private ProgressBar pbUpdate;
    private AlertDialog mDownloadDialog;
    private HttpURLConnection conn;
    private int progress=0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:

                    // 设置进度条位置
                    pbUpdate.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:

                    //断开连接
                    conn.disconnect();

                    //取消下载对话框显示
                    if (mDownloadDialog.isShowing())
                        mDownloadDialog.dismiss();
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }

    };


    /**
     * 安装到手机
     */
    private void installApk() {
        File apkfile = new File(savePath, fileName);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    public UpdateManager(Context context) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    //检测更新
    public void checkUpdate() {
        if (isUpdate() == true) {
            showUpdateDialog();
        } else {
            ToastUtils.show(mContext, "当前版本已为最新");
        }
    }

    //显示软件更新的提示框
    private void showUpdateDialog() {
        String string = download(url);//xml的下载地址

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.update)
                .setMessage(string)
                .setPositiveButton(R.string.updateNow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //显示下载对话框
                        showDownLoadDialog();
                    }
                })
                .setNegativeButton(R.string.updateLater, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }

    /**
     * 通过URl下载更新内容
     *
     * @param urlString
     * @return
     */
    private String download(String urlString) {
        StringBuffer sbBuffer = new StringBuffer();
        String line = null;
        BufferedReader buffer = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = buffer.readLine()) != null) {
                sbBuffer.append(line);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        return sbBuffer.toString();
    }


    /**
     * 显示下载进度对话框
     */
    private void showDownLoadDialog() {

        View updateProgressView = mLayoutInflater.inflate(R.layout.update, null);
        pbUpdate = (ProgressBar) updateProgressView.findViewById(R.id.pb_update);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.loadTitle);
        builder.setView(updateProgressView, 20, 20, 20, 20);
        builder.setNegativeButton(R.string.cancleUpdate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //设置取消状态
                cancleUpdate = true;
            }
        });
//        builder.setCancelable(true);
        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        //从服务端下载最新文件
        downLoadApk();

    }

    /**
     * 开启一条新线程下载Apk
     */
    private void downLoadApk() {
        new downLoadApkThread().start();
    }

    /**
     * 检测软件是否有更新版本
     *
     * @return
     */
    private boolean isUpdate() {
//        double versionCode = getVersionCode(mContext);
//
//        //服务器端最新的App版本号
//        double serviceVersionCode = 1.0;
//
//        if (serviceVersionCode > versionCode) {
//            return true;
//        }
//        return false;
        return true;
    }

    private double getVersionCode(Context mContext) {

        double versionCode = 0.0;
        String packName = mContext.getPackageName();
        try {
            versionCode = mContext.getPackageManager().getPackageInfo(packName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    class downLoadApkThread extends Thread {

        @Override
        public void run() {

            if (SDUtils.isAvailable()) {

                try {
                    URL url = new URL(baseUrl);

                    //创建连接
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    //获取文件大小
                    int length = conn.getContentLength();

                    Log.d("tag", "文件大小： " + length);

                    //创建输入流
                    InputStream inputStream = conn.getInputStream();

                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }

                    File apkFile = new File(savePath, fileName);
                    FileOutputStream fos = new FileOutputStream(apkFile);

                    int count = 0;
                    //缓存
                    byte[] buff = new byte[1024];
                    //读写文件
                    do {

                        int numRead = inputStream.read(buff);
                        count += numRead;
                        Log.d("tag", "count文件大小： " + count);

                        //计算进度条位置
                        progress = (int) (((float) count / length) * 100);

                        //更新UI进度
                        mHandler.sendEmptyMessage(DOWNLOAD);

                        if (numRead <= 0) {
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        fos.write(buff, 0, numRead);
                    } while (!cancleUpdate);

                    //读写完毕，关闭输入输出流
                    fos.close();
                    inputStream.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                ToastUtils.show(mContext, "SD卡不可用");
            }
        }
    }

}
