package com.yupi.roj;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 主类测试
 *
 */
@SpringBootTest
class MainApplicationTests {

    @Test
    void contextLoads() {
        Map<Integer, Integer> map = new HashMap<>();
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        System.out.println(s);
        int i = sc.nextInt();
        System.out.print(i);
        System.out.println("end");
    }

}
