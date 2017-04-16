package com.cdd.autoupdatemodel;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void CheckUpdate(View view) {
        if (isOpenNetwork()) {
            UpdateManager manager = new UpdateManager(this);
            // 检查软件更新
            manager.checkUpdate();

        }
    }

    /**
     * 判断是否有网络
     *
     * @return
     */
    private boolean isOpenNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            // 获取当前网络连接的类型信息
            int networkType = networkInfo.getType();
            if (ConnectivityManager.TYPE_WIFI == networkType) {// 当前为wifi网络

                ToastUtils.show(this, "当前为wifi网络");
            } else if (ConnectivityManager.TYPE_MOBILE == networkType) {// 当前为mobile网络

                ToastUtils.show(this, "当前为mobile网络");
            }
            return connManager.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }
}
