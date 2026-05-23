package com.lifeselection.utils;

public interface ILock {

    /**
     * 灏濊瘯鑾峰彇閿?
     * @param timeoutSec 閿佹寔鏈夌殑瓒呮椂鏃堕棿锛岃繃鏈熷悗鑷姩閲婃斁
     * @return true浠ｈ〃鑾峰彇閿佹垚鍔? false浠ｈ〃鑾峰彇閿佸け璐?
     */
    boolean tryLock(long timeoutSec);

    /**
     * 閲婃斁閿?
     */
    void unlock();
}
