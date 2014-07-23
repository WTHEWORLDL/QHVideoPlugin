package com.wangli.qhvideoplugin;

import com.qihoo.qplayer.QHPlayerSDK;

import android.app.Application;

public class VideoApplication extends Application {
    private final static String key = "4HcPn7xQm6Uhn/KGr+eqbQ==";
    @Override
    public void onCreate() {
        super.onCreate();
        QHPlayerSDK.getInstance().init(this, key);
    }

}
