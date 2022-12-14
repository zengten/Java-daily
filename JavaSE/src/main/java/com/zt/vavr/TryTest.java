package com.zt.vavr;

import io.vavr.Function1;
import io.vavr.control.Try;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author ZT
 * @version 1.0
 * @description: vavr util Try测试类
 * @date 2022/8/21 19:26
 */
public class TryTest {

    @SuppressWarnings("all")
    @Test
    public void testTryWithNumericOverflowException() {
        Integer answer = Try.of(() -> 12 / 0).getOrElse(-1);
        assertThat(answer).isEqualTo(-1);
    }

    @SuppressWarnings("all")
    @Test
    public void testTryWithThreadSleep() {
        Function1<Integer, Try<Integer>> f1 = x -> Try.of(() -> {
            Thread.sleep(5000);
            return x + 1000;
        });
        Integer answer = f1.apply(7).getOrElse(-1);
        assertThat(answer).isEqualTo(1007);
    }

    @Test
    public void testTryRunWithException() {
        boolean success = Try.run(() -> Integer.parseInt("11"))
                .andThenTry(() -> Integer.parseInt("22"))
                .andThenTry(() -> Integer.parseInt("33"))
                .andFinally(() -> Integer.parseInt("a"))
                .isSuccess();
        assertThat(success).isFalse();
    }

    /**
     * 看上去 Supplier  Callable 方法差不多！
     */
    @Test
    public void testTryOfSupplierOrCallable() {
        Integer num2 = Try.ofSupplier(() -> Integer.parseInt("1a"))
                .getOrElse(222);
        System.out.println("num2 = " + num2);
        Integer num3 = Try.ofCallable(() -> Integer.parseInt("1c"))
                .getOrElse(333);
        System.out.println("num3 = " + num3);
    }

    /**
     * try fold 根据执行方法是否异常，继续执行另外方法获得结果
     * 异常返回a，无异常返回b
     */
    @Test
    public void testTryFold() {
        String s = Try.of(() -> Integer.parseInt("123a"))
                .fold(e -> "a", str -> "b");
        System.out.println("s = " + s);
    }

    /**
     * 使用try执行parseInt方法，
     * 根据异常情况执行consumer A，非异常情况执行consumer B
     * 再map获得最后结果
     */
    @Test
    public void testTryPeekAndMap() {
        String s = Try.of(() -> Integer.parseInt("1234a"))
                .peek(e -> {
                    throw new RuntimeException("aa");
                }, value -> {
                    System.out.println("value = " + value);
                    if(123 == value) {
                        throw new RuntimeException("bb");
                    }
                }).map(String::valueOf)
                .getOrElseThrow(() -> new RuntimeException("cc"));
        System.out.println("s = " + s);
    }
}
