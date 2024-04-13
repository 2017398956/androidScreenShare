package com.wanjian.puppet;

import static com.wanjian.puppet.ScreenUtils.screenshot;
import static com.wanjian.puppet.TouchUtils.back;
import static com.wanjian.puppet.TouchUtils.home;
import static com.wanjian.puppet.TouchUtils.menu;
import static com.wanjian.puppet.TouchUtils.touchDown;
import static com.wanjian.puppet.TouchUtils.touchMove;
import static com.wanjian.puppet.TouchUtils.touchUp;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by wanjian on 2017/4/4.
 */

public class Main {

    private static float scale = 1;

    public static void main(String[] args) throws Exception {
        System.out.println(">>>>>start>>>>>");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.out));
        prepareLocalServer();
    }

    private static void prepareLocalServer() throws IOException {
        LocalServerSocket serverSocket = new LocalServerSocket("puppet-ver1");
        LocalSocket socket = null;
        while (true) {
            System.out.println(">>>>> listen");
            try {
                socket = serverSocket.accept();
                System.out.println(">>>>> accepted");
                read(socket);
                write(socket);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                prepareLocalServer();
            }
        }
    }


    private static WriteThread writeThread;

    public static class WriteThread extends Thread {
        public LocalSocket socket;

        @Override
        public void run() {
            super.run();
            try {
                final int VERSION = 2;
                BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                while (true) {
                    Bitmap bitmap = screenshot();
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (ScreenUtils.getDisplaySize().x * scale),
                            (int) (ScreenUtils.getDisplaySize().y * scale), true);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);

                    outputStream.write(VERSION);
                    writeInt(outputStream, byteArrayOutputStream.size());
                    outputStream.write(byteArrayOutputStream.toByteArray());
                    outputStream.flush();
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void write(final LocalSocket socket) {
        if (writeThread == null) {
            writeThread = new WriteThread();
            writeThread.socket = socket;
            writeThread.start();
        } else {
            writeThread.socket = socket;
        }
    }

    private static ReadThread readThread;

    public static class ReadThread extends Thread {

        private String DOWN = "DOWN";
        private String MOVE = "MOVE";
        private String UP = "UP";
        private String MENU = "MENU";
        private String HOME = "HOME";
        private String BACK = "BACK";
        private String DEGREE = "DEGREE";
        public LocalSocket socket;

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        System.out.println("socket closed....");
                        return;
                    }
                    if (line.startsWith(DOWN)) {
                        handlerDown(line.substring(DOWN.length()));
                    } else if (line.startsWith(MOVE)) {
                        handlerMove(line.substring(MOVE.length()));
                    } else if (line.startsWith(UP)) {
                        handlerUp(line.substring(UP.length()));
                    } else if (line.startsWith(MENU)) {
                        menu();
                    } else if (line.startsWith(HOME)) {
                        home();
                    } else if (line.startsWith(BACK)) {
                        back();
                    } else if (line.startsWith(DEGREE)) {
                        scale = Float.parseFloat(line.substring(DEGREE.length())) / 100;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private static void read(final LocalSocket socket) {
        if (readThread == null) {
            readThread = new ReadThread();
            readThread.socket = socket;
            readThread.start();
        } else {
            readThread.socket = socket;
        }
    }

    private static void handlerUp(String line) {
        Point point = getXY(line);
        if (point != null) {
            touchUp(point.x, point.y);
        }
    }

    private static void handlerMove(String line) {
        Point point = getXY(line);
        if (point != null) {
            touchMove(point.x, point.y);
        }
    }

    private static void handlerDown(String line) {
        Point point = getXY(line);
        if (point != null) {
            touchDown(point.x, point.y);
        }
    }


    private static Point getXY(String nums) {
        try {
            Point point = ScreenUtils.getDisplaySize();
            String[] s = nums.split("#");
            float scaleX = Float.parseFloat(s[0]);
            float scaleY = Float.parseFloat(s[1]);
            point.x = (int) (point.x * scaleX);
            point.y = (int) (point.y * scaleY);
            return point;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return null;
    }


    private static void writeInt(OutputStream outputStream, int v) throws IOException {
        outputStream.write(v >> 24);
        outputStream.write(v >> 16);
        outputStream.write(v >> 8);
        outputStream.write(v);
    }


}
