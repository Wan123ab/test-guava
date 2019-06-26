package com.wq.testguava;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.junit.Test;

/**
 * @author 万强
 * @date 2019/6/26 11:55
 * @desc
 */
public class TestValidate {

    @Test
    public void test1() {
        System.out.println(Strings.isNullOrEmpty(""));//true
    }

    @Test
    public void test2() {
        int num = 1;
        //java.lang.IllegalArgumentException: num不能大于2，当前num=1
        Preconditions.checkArgument(num > 2, "num不能大于2，当前num = %s", num);
    }

}
