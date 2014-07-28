
package com.wangli.qhvideoplugin.activity;

import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.view.QHVideoController;

import android.os.Bundle;

public class VideoPlayerActivity extends BaseActivity {

    private QHVideoController controller;
    private String website = "youku";
    private String url = "http://v.youku.com/v_show/id_XNzQ2NDE0NzA4.html";// http%3A%2F%2Fv.youku.com%2Fv_show%2Fid_XNzMzNjkwMjQ4.html
    private String title = "狗血的山姆";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        controller = (QHVideoController) findViewById(R.id.qhvc_controller);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.init(title, website, url, "http://img.nr99.com/attachment/forum/threadcover/04/eb/123658.jpg", 1000);
    }

    @Override
    public void finish() {
        super.finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(controller!=null){
            controller.release();
        }
    }
}
