package com.zt.utils;

/**
 * @author ZT
 */
public interface ILock {

    boolean tryLock(long expireTime);

    void unlock();
}
