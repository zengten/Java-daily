package com.zt.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.LoginFormDTO;
import com.zt.dto.Result;
import com.zt.dto.UserSignDetail;
import com.zt.entity.User;
import com.zt.mapper.UserMapper;
import com.zt.service.IUserService;
import com.zt.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author ZT
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final RedisUtils redisUtils;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisScriptUtil redisScriptUtil;

    @Override
    public Result sendCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        redisUtils.set(RedisConstants.LOGIN_CODE_KEY + phone, code, 120);
        return Result.ok();
    }

    @Override
    public Result loginByPassword(LoginFormDTO loginForm) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, loginForm.getPhone()));
        if(Objects.isNull(user)) {
            return Result.fail("账号未注册！");
        }
        if(!DigestUtil.bcryptCheck(loginForm.getPassword(), user.getPassword())) {
            return Result.fail("账号或密码错误！");
        }
        String token = IdUtil.fastSimpleUUID();
        redisUtils.set(RedisConstants.LOGIN_USER_KEY + token, user, 36000);
        return Result.ok(token);
    }

    @Override
    public Result loginByMobile(LoginFormDTO loginForm) {
        String code = redisUtils.get(RedisConstants.LOGIN_CODE_KEY + loginForm.getPhone());
        if(StrUtil.isBlank(code) || !code.equals(loginForm.getCode())) {
            return Result.fail("验证码有误！");
        }
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, loginForm.getPhone()));
        if(Objects.isNull(user)) {
            user = User.builder()
                    .phone(loginForm.getPhone())
                    .nickName("user_" + IdUtil.fastSimpleUUID().substring(0, 6))
                    .password(DigestUtil.bcrypt("Aa112233"))
                    .build();
            save(user);
        }
        String token = IdUtil.fastSimpleUUID();
        redisUtils.set(RedisConstants.LOGIN_USER_KEY + token, user, 36000);
        return Result.ok(token);
    }

    @Override
    public Result logout() {
        String key = RequestUtil.getRequest().getHeader("authorization");
        if(StrUtil.isBlank(key)) {
            return Result.fail("未登录！");
        }
        redisUtils.delete(RedisConstants.LOGIN_USER_KEY + key);
        return Result.ok();
    }

    @Override
    public Result sign() {
        Long userId = UserHolder.getUser().getId();
        LocalDate today = LocalDate.now();
        String signKeyPrefix = today.format(DateTimeFormatter.ofPattern("yyyyMM:"));
        // 签到key格式  sign:202211:userId  方便按时间统计签到人数
        String signKey = RedisConstants.USER_SIGN_KEY + signKeyPrefix + userId;
        int dayIndex = today.getDayOfMonth();
        // 签到自增key
        String formatDay = today.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String signIncrementKey = RedisConstants.SIGN_INDEX_KEY + formatDay;
        RedisScript<Long> script = redisScriptUtil.getRedisScript(RedisScriptUtil.ScriptEnum.SIGN);
        Long result = stringRedisTemplate.execute(script,
                ListUtil.of(signKey, signIncrementKey),
                // 1号签到在第0个bit位置
                String.valueOf(dayIndex - 1), String.valueOf(1)
                );
        if(result == null) {
            return Result.fail("系统繁忙！");
        }
        if(result <= 0) {
            return Result.fail("今天已经签到过了！");
        }
        return Result.ok(StrUtil.format("签到成功，今天第{}个签到！", result));
    }

    @Override
    public Result signDay() {
        List<Long> result = getSignValue();
        // 最多31天，签到在0-30个bit位置
        if(CollectionUtil.isEmpty(result)) {
            // 当月连续签到0天
            return Result.ok(0);
        }
        long signValue = result.get(0);
        int ans = 0;
        while((signValue & 1) == 1) {
            signValue >>>= 1;
            ans++;
        }
        return Result.ok(StrUtil.format("连续签到{}天", ans));
    }

    /**
     * 获取实际签到数据
     */
    private List<Long> getSignValue() {
        LocalDate today = LocalDate.now();
        String keyPrefix = today.format(DateTimeFormatter.ofPattern("yyyyMM:"));
        String key = RedisConstants.USER_SIGN_KEY + keyPrefix + UserHolder.getUser().getId();
        int dayIndex = today.getDayOfMonth();
        // bitField key operate u5 0
        return stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().get(BitFieldSubCommands
                        // 注意这里不是(dayIndex - 1)，dayIndex表示从0开始多少位
                        .BitFieldType.unsigned(dayIndex)).valueAt(0)
        );
    }

    @Override
    public Result signOfMonth() {
        List<Long> signValueList = getSignValue();
        List<UserSignDetail> signEmptyMsg = buildCurMonthSignEmptyMessage();
        if(CollectionUtil.isEmpty(signValueList)) {
            return Result.ok(signEmptyMsg);
        }
        // 填充签到数据
        long signValue = signValueList.get(0);
        // 当月最多是今天已经签到，不可能明天签到了
        int start = LocalDate.now().getDayOfMonth();
        for (int i = start - 1; i >= 0; i--) {
            if((signValue & 1) == 1) {
                // 当天有签到
                signEmptyMsg.get(i).setHasSign(true);
            }
            signValue >>>= 1;
        }
        return Result.ok(signEmptyMsg);
    }

    /**
     * 构建当月签到模板数据
     */
    private List<UserSignDetail> buildCurMonthSignEmptyMessage() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonth().getValue();
        int firstDay = 1;
        int lastDay = today.lengthOfMonth();
        List<UserSignDetail> ans = new ArrayList<>();
        while (firstDay <= lastDay) {
            String day = StrUtil.format("{}-{}-{}", year, month, firstDay);
            UserSignDetail detail = new UserSignDetail(day, false);
            ans.add(detail);
            firstDay++;
        }
        return ans;
    }

}
