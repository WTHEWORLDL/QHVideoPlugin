package com.wangli.qhvideoplugin.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;

public class CommonUtil {
    @SuppressLint("SimpleDateFormat")
    public static String getTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");// 初始化Formatter的转换格式。
        String ms = formatter.format(time);
        return ms;
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
