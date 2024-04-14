package com.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WindowsCmdDemo {
    // 1.一般的执行方法，有时执行 exe 会卡在那    stmt要执行的命令
    public static void executive(String stmt) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();  //获取Runtime实例
        //执行命令
        try {
            String[] command = {"cmd", "/c", stmt};
            Process process = runtime.exec(command);
            // 标准输入流（必须写在 waitFor 之前）
            String inStr = consumeInputStream(process.getInputStream());
            // 标准错误流（必须写在 waitFor 之前）
            String errStr = consumeInputStream(process.getErrorStream()); //若有错误信息则输出
            int proc = process.waitFor();
            if (proc == 0) {
                System.out.println("执行成功");
            } else {
                System.out.println("执行失败" + errStr);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消费 input stream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }

    // 2.第二种方法是先生成一个缓存文件，用来缓存执行过程中输出的信息，这样在执行命令的时候不会卡。代码如下：
    //这个方法比第一个好，执行时不会卡  stmt要执行的命令
    public static void exeCmd(String stam) {
        BufferedReader br = null;
        try {
            File file = new File("D:\\daemonTmp");
            File tmpFile = new File("D:\\daemonTmp\\temp.tmp");//新建一个用来存储结果的缓存文件
            if (!file.exists()) {
                file.mkdirs();
            }
            if (!tmpFile.exists()) {
                tmpFile.createNewFile();
            }
            ProcessBuilder pb = new ProcessBuilder().command("cmd.exe", "/c", stam).inheritIO();
            pb.redirectErrorStream(true);//这里是把控制台中的红字变成了黑字，用通常的方法其实获取不到，控制台的结果是pb.start()方法内部输出的。
            pb.redirectOutput(tmpFile);//把执行结果输出。
            pb.start().waitFor();//等待语句执行完成，否则可能会读不到结果。
            InputStream in = new FileInputStream(tmpFile);
            br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            br = null;
            tmpFile.delete();//卸磨杀驴。
            System.out.println("执行完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
