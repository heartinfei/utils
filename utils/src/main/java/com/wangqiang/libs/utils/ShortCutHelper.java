package com.wangqiang.libs.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

/**
 * Created by heartinfei on 2015/8/14.
 */
public class ShortCutHelper {
    // TODO: 2015/8/21
    public static final String TARGET_COMPONENT_NAME = "TARGET_COMPONENT_NAME";
    public static final String IS_FROM_SHORT_CUT = "IS_FROM_SHORT_CUT";
    private String mTitle = "";
    private int mImgResId = 0;
    private Bitmap mBmp = null;
    private Context mContext;
    private String mLauncherClassName;
    private String mTargetClassName;
    private String mUrl;

    private ShortCutHelper(Builder builder) {
        mBmp = builder.mBmp;
        mContext = builder.context;
        mTitle = builder.title;
        mImgResId = builder.imgResId;
        mLauncherClassName = builder.launcherClassName;
        mTargetClassName = builder.targetClassName;
        mUrl = builder.url;
    }

    /**
     * create shotcut on desktop
     */

    public void createShortCut() {
        //创建快捷方式的Intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        //需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mTitle);
        //快捷图片
        if (mBmp != null) {
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON, mBmp);
        } else {
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(mContext, mImgResId));
        }
        Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
        ComponentName componentName = new ComponentName(mContext.getPackageName(), mLauncherClassName);
        //这里必须使用这个方法
        intentLauncher.setComponent(componentName);
        intentLauncher.putExtra(IS_FROM_SHORT_CUT, true);
        intentLauncher.putExtra("actionBarTitle", mTitle);
        intentLauncher.putExtra("dataUri", mUrl);
        intentLauncher.putExtra(TARGET_COMPONENT_NAME, mTargetClassName);
        //点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentLauncher);
        mContext.sendBroadcast(shortcutintent);
        Toast.makeText(mContext, "该功能已添加到桌面", Toast.LENGTH_LONG).show();
    }//end createShortCut

    public static class Builder {
        private Context context;
        private String title;
        private int imgResId;
        private Bitmap mBmp = null;
        private String url;
        private String launcherClassName;
        private String targetClassName;

        public Builder(Context ctx, String launClassName, String tarClassName) {
            this.context = ctx;
            this.launcherClassName = launClassName;
            this.targetClassName = tarClassName;
        }

        public Builder setTitle(String t) {
            this.title = t;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setImgResId(int resId) {
            imgResId = resId;
            return this;
        }

        public Builder setImageBitmap(Bitmap bmp) {
            mBmp = bmp;
            return this;
        }

        public ShortCutHelper create() {
            return new ShortCutHelper(this);
        }
    }//end Builder
}
