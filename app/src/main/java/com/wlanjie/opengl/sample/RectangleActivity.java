package com.wlanjie.opengl.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wlanjie on 2017/3/18.
 */
public class RectangleActivity extends AppCompatActivity {

    GL2JNIView mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new GL2JNIView(getApplication(), GL2JNIView.DrawType.Rectangle);
        setContentView(mView);
    }

    @Override protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GL2JNILib.release();
    }
}
