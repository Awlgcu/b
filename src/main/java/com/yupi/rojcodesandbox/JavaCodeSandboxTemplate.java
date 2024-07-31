package com.yupi.rojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.rojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.rojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.rojcodesandbox.model.ExecuteMessage;
import com.yupi.rojcodesandbox.model.JudgeInfo;
import com.yupi.rojcodesandbox.utils.ProcessUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

//定义模版方法模式的模板
public abstract class JavaCodeSandboxTemplate implements CodeSandbox{
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 20000L;


    /**
     * 1.第一步,把代码保存为文件
     * @param code
     * @return
     */
    public File saveCodeToFile(String code){
        //1. 把用户的代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2.编译代码,得到class文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        //        2. 编译代码，得到 class 文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if(executeMessage.getExitValue()!=0){
                throw  new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    /**
     * 3.执行文件,获得执行结果列表
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList){
        //3.执行编译后的文件
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                //开启新线程去监控上面执行代码的线程,如果那个线程的在5s后都没执行完,那说明出错了,可以直接关闭线程了
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                //拿到执行运行代码的进程,使用封装的工具类去获取运行结果
                //第一种方式的接口
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
//                System.out.println("排错检查:"+executeMessage);
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException("程序执行异常", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4.整理输出结果集,将信息封装到executeCodeResponse返回类
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        //4.整理输出信息,将执行结果信息封装到executeCodeResponse里面
//        System.out.println("排错检查"+executeMessageList);
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime=0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                //如果执行出现错误了,返回设置状态填充错误信息并直接结束循环
                executeCodeResponse.setStatus(3);//3表示代码执行中出错了
                executeCodeResponse.setMessage(errorMessage);
                break;
            }
            Long time = executeMessage.getTime();
            //这里选择的是简便的方法,即只获取所有执行结果中最长的时间进行校验!
            if(time!=null){
                maxTime = Math.max(maxTime, time);
            }
            outputList.add(executeMessage.getMessage());
        }
        //判断每个样例是否都成功执行,设置状态码为1
        if(outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }

        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //TODO 待完成,如何查看程序执行内存
//        judgeInfo.setMemory(148L);
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
//        System.out.println("排错检查1:"+executeCodeResponse);
        return executeCodeResponse;
    }

    /**
     * 5.删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        if(userCodeFile.getParentFile()!=null){
            return  FileUtil.del(userCodeParentPath);
        }
        return true;
    }
    /**
     * 6.遇到异常返回异常反馈的方法
     * @param e
     * @return
     */
    public ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(Collections.emptyList());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);//2表示代码还没执行就出错,内部错误或者编译错误
        executeCodeResponse.setJudgeInfo(null);
        return executeCodeResponse;
    }

    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        //2. 编译代码，得到 class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        //3.执行编译后的文件
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        //4.整理输出信息,将执行结果信息封装到executeCodeResponse里面
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        //5.执行完一个样例后的文件夹及文件应该被清理
        boolean del = deleteFile(userCodeFile);
        System.out.println("删除"+(del ? "成功":"失败"));
//        System.out.println("排错检查2:"+executeCodeResponse);
        return executeCodeResponse;

        //6.如果遇见异常返回异常响应
    }


}
