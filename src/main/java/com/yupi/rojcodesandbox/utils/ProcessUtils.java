package com.yupi.rojcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.yupi.rojcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 执行进程并获取信息的工具类
 */
public class ProcessUtils {
    /**
     * 执行代码并获取信息的静态方法
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            //2.2拿到程序执行结果,包括编译后输出的信息以及编译出错信息
            if (exitValue == 0) {
                System.out.println(opName+"成功");
                //分批获取输出信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //逐行打印信息
                List<String> outputList = new ArrayList<>();
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputList, "\n"));
            } else {
                System.out.println(opName+"失败,错误码:" + exitValue);
                //分批获取输出信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //逐行打印信息
                List<String> outputList = new ArrayList<>();
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputList, "\n"));

                //分批获取错误输出信息
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                //逐行打印错误信息
                List<String> errorOutputList = new ArrayList<>();
                String errorCompileErrorOutputLine;
                while ((errorCompileErrorOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputList.add(errorCompileErrorOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputList, "\n"));
            }
            stopWatch.stop();
            long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
//            System.out.println(lastTaskTimeMillis+"ms");
            executeMessage.setTime(lastTaskTimeMillis);
            runProcess.destroy();
        }catch(Exception e){
            e.printStackTrace();
        }

        return executeMessage;
    }

    /**
     * 在控制台有交互的执行代码并获取信息的静态方法
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            //1.向控制台输入
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");
            String newArgs = StrUtil.join("\n", s) + "\n";
            outputStreamWriter.write(newArgs);
            outputStreamWriter.flush();
            //这个flush相当于按了一个回车!

            //2.分批获取输出信息
            InputStream inputStream = runProcess.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            //3.逐行打印信息
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            //4.资源回收
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        }catch(Exception e){
            e.printStackTrace();
        }
        return executeMessage;
    }
}