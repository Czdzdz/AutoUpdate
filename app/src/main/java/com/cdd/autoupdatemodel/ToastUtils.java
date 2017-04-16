package com.cdd.autoupdatemodel;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/16 0016 01:58.
 * <p>
 * From url:
 */

public class ToastUtils {

    public static void show(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
}
