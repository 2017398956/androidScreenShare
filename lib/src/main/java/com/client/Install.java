package com.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import sun.awt.OSInfo;

/**
 * Created by wanjian on 2017/4/5.
 */

public class Install {

    private static String adbPath = "./adb ";
    private static String adbShell;

    public static void main(String[] args) {
        adbPath = "adb ";
        adbShell = adbPath + "shell ";
        String dexPath = "Main.dex";
        File file = new File("../../../../../shareandcontrollib", "build/intermediates/dex/debugAndroidTest/mergeDexDebugAndroidTest/classes.dex");
        System.out.println("currentPath:" + file.getAbsolutePath() + " and exist?:" + file.exists());
//        install(file.getAbsolutePath());
        shellCommand(new String[]{"cat /sdcard/did"});
    }

    public static void install(String dexFilePath) {
        adbCommand("push " + dexFilePath + " /sdcard/Main.dex");
        String path = "export CLASSPATH=/sdcard/Main.dex";
        String app = "exec app_process /sdcard com.wanjian.puppet.Main";
        shellCommand(new String[]{path, app});
    }

    private static void adbCommand(String com) {
        System.out.println("adbCommand...." + com);
        if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            command("cmd", adbPath + com);
        } else {
            command("sh", adbPath + com);
        }
    }

    private static void shellCommand(String[] com) {
        System.out.println("shell command: " + adbShell + "  " + Arrays.toString(com));
        BufferedWriter outputStream = null;
        try {
            Process process = Runtime.getRuntime().exec(adbShell); // adb
            // shell
            outputStream = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            for (String s : com) {
                outputStream.write(s);
                outputStream.write("\n");
            }
            outputStream.flush();
            System.out.println("shell write finished...");
//            readError(process.getErrorStream());
//            adbCommand("forward tcp:8888 localabstract:puppet-ver1");
            readResult(process.getInputStream());

//            int result = process.waitFor();
//            System.out.println("waitFor result : " + result);

//            outputStream.write("cat /sdcard/did");
//            outputStream.flush();
//            String str = readResult(process.getInputStream());
//            System.out.println("cat result:" + str);
//            if (str != null) {
//                outputStream.write("exit\n");
//
//            }

            boolean canExit = false;
            while (!canExit) {
                // FIXME:添加终止操作
                Thread.sleep(10 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        if (outputStream != null) {
            try {
//                outputStream.write("exit\n");
//                outputStream.flush();
//                outputStream.close();
//                System.out.println("shell command exit.");
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private static void readError(final InputStream errorStream) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                readResult(errorStream);
            }
        }.start();
    }

    private static void command(String c, String com) {
        System.out.println("---> " + c + com);
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec(c); // adb
            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));


            outputStream.write(com);
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            int i = process.waitFor();
            readResult(process.getInputStream());


            System.out.println("------END-------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readResult(final InputStream stream) {
        System.out.println("read result.....");
        try {
            String line;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            int i = 0;
            while ((line = reader.readLine()) != null) {
                i++;
                System.out.println(i + ": size:" + line.length() + " content:" + line);
            }
            System.out.println("-------END------");
        } catch (IOException e) {
            System.out.println("exception");
            e.printStackTrace(System.out);
            try {
                stream.close();
            } catch (Exception e1) {
                e1.printStackTrace(System.out);
            }
        }
//        return null;
    }


}
