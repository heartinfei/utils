package com.wangqiang.libs.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by heartinfei on 15/3/29.
 */
public class FileManager {
    public final static String getDefaultDir(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * access Asset floder
     *
     */
    public static final String readFileFromAssets(Context context, String fileName) {
        StringBuffer res = new StringBuffer();
        try {
            InputStream is = context.getAssets().open(fileName);
            byte buffer[] = new byte[1024 * 4];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                res.append(new String(buffer, 0, length));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return res.toString();
        }
    }//end readFileFromAssets

    /**
     * save file
     *
     * @param fileName file name
     * @param content  content
     * @param isAppend true appent in end
     * @return true success
     */
    public final static boolean saveToFlie(String fileName, String content, boolean isAppend) {
        try {
            FileWriter writer = new FileWriter(fileName, isAppend);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public final static boolean saveFileToPrivateDir(Context context, String fileName, String content) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public final static String readFileFromPrivateDir(Context context, String fileName) {
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = context.openFileInput(fileName);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len));
            }
            fis.close();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            return sb.toString();
        }
    }


    public final static String readFromFile(String fileName) {
        StringBuffer sb = new StringBuffer();
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bfr = new BufferedReader(reader);
            String line;
            while ((line = bfr.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return sb.toString();
        }
    }//end readFromFile

    /**
     * deletefile
     *
     * @param fileName
     */
    public final static void deletFile(String fileName) {
        File file = new File(fileName);
        if (file.exists())
            file.delete();
    }
}
