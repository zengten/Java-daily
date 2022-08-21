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
}
