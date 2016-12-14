package com.miki.flashsetting;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;

public class UtilsSDCard {

        public static boolean SaveUserInfo(int currentNum,String spendTime,
                        String counts, String secondTime, String fileSize, String Velocity,
                        String completeNumber) {

                try {
                        File SDCardFile = Environment.getExternalStorageDirectory();
                        File file = new File(SDCardFile, "data.txt");
                        FileOutputStream fos;
                        fos = new FileOutputStream(file,true);

                        String data = ("***********************" + "\n" +
                                        "Num:"+ currentNum + "    " + "\n"
                                +"Spend time:" + spendTime + "    " + "\n"
                                        + "Time:" + secondTime + "s" + "    " + "\n"
                                        + "Auto Copy Count:" + counts + "" + "    " + "\n"
                                        + "File Complete Number:" + completeNumber + "    " + "\n"
                                        + "File Size:" + fileSize + "    " + "\n"
                                        + "Velocity:" + Velocity + "    " + "\n\n");

                        fos.write(data.getBytes());
                        fos.flush();
                        fos.close();
                        return true;
                } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Write fail.." + e);
                }
                return false;
        }
}