
package com.example.texteditortest.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class LogMgr {

    public static boolean DEBUG = true;

    public static final String TAG = "Jeet";

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void writeLog(String str) {

        String str_Path_Full = Environment.getExternalStorageDirectory().toString()
                + "/download/logdata.txt";

        File file = new File(str_Path_Full);
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
        } else {
            try {
                BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full,true));
                bfw.write(str);
                bfw.write("\n");
                bfw.flush();
                bfw.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    }


}
