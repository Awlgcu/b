package com.yupi.roj.judge.codesandbox;

import com.yupi.roj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.yupi.roj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.yupi.roj.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂,根据传入的字符串生成对应的代码沙箱
 * 此处是静态工厂,只写一个静态方法来创建对应的代码沙箱
 */
public class CodeSandboxFactory {
    public static CodeSandbox newInstance(String type){
        switch (type){
            case "example":
                System.out.println("ExampleCodeSandbox success!");
                return new ExampleCodeSandbox();
            case "remote":
                System.out.println("RemoteCodeSandbox success!");
                return new RemoteCodeSandbox();
            case "thirdParty":
                System.out.println("thirdParty success!");
                return new ThirdPartyCodeSandbox();
            default:
                System.out.println("ExampleCodeSandbox success!");
                return new ExampleCodeSandbox();
        }
    }

}
