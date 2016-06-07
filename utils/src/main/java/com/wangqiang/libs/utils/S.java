package com.wangqiang.libs.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author wangqiang
 */
public class S {
    public static final String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/WQ/log/";
    public static final String tag = "smart_wq_log";
    //true 输出log
    private static final boolean logSwitch = true;

    public static final void i(Object s) {
        if (logSwitch) {
            StackTraceElement stack = Thread.currentThread().getStackTrace()[3];
            String logStr;
            if (s == null)
                logStr = "null";
            else if (s instanceof String)
                logStr = (String) s;
            else
                logStr = String.valueOf(s);
            Log.i(tag, stack.getFileName() + "-" + stack.getMethodName() + "-" + stack.getLineNumber() + "-->" + logStr);
        }
    }// end i

    /**
     * check sd card
     *
     * @return SD exist true
     */
    public static final boolean checkSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static final boolean writeLogToSD(String msg, String savePath, String fileName) {
        if (!logSwitch || !checkSD()) {
            return false;
        }
        String path = (savePath == null ? defaultPath : savePath);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (fileName == null) {
            fileName = "Log-" + System.currentTimeMillis() + ".txt";
        }
        try {
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(msg.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }// end writeLogToSD
}
