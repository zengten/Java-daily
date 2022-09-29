package com.zt.vavr;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static io.vavr.API.*;

/**
 * vavr match test
 *
 * @author ZT
 * @version 1.0
 * @date 2022/8/25 19:58
 */
@Slf4j
public class MatchTest {

    /**
     *  Match(cur).of 未匹配成功抛出error
     * @throws io.vavr.MatchError
     */
    @SuppressWarnings("all")
    @Test
    public void testMatchCaseWithError() {
        String cur = "test1";
        String answer = Match(cur).of(
                Case($("1"), "one"),
                Case($("2"), "two")
        );
        System.out.println(answer);
    }


    /**
     * Case(Pattern0<T> pattern, R retVal)
     * Match(cur).option 返回包装option
     */
    @SuppressWarnings("all")
    @Test
    public void testMatchCaseOption() {
        String cur = "test1";
        String answer = Match(cur).option(
                Case($("1"), "one"),
                Case($("2"), "two")
        ).getOrNull();
        System.out.println(answer);
    }

    /**
     * Case(Pattern0<T> pattern, Supplier<? extends R> supplier)
     * 只有 success Case 才会执行之后语句
     */
    @SuppressWarnings("all")
    @Test
    public void testMatchCase() {
        String str1 = "test1", str2 = "test2", str3 = "test3", cur = "test3";
        String answer = Match(cur).option(
                Case($(str1), () -> {
                    log.info("return one execute......");
                    return "one";
                }),
                Case($(str2), () -> {
                    log.info("return two execute......");
                    return "two";
                }),
                Case($(str3), () -> {
                    log.info("return three execute......");
                    return "three";
                })
        ).getOrNull();
        System.out.println(answer);
    }
}
