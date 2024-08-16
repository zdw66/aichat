package com.aichat.utils;


import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Slf4j
public class SHA1 {

    /**
     * 用SHA1生成安全签名
     * @param token
     * @param timestamp
     * @param nonce
     * @param encrypt
     * @return
     */
    public static String getSHA1(String token,String timestamp,String nonce,String encrypt){
        try {
            String[] array = new String[]{token,timestamp,nonce,encrypt};
            StringBuffer sb = new StringBuffer();
            //字符串排序
            Arrays.sort(array);
            for(int i=0;i<4;i++){
                sb.append(array[i]);
            }
            String str = sb.toString();
            //SHA1签名生成
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            StringBuffer hexStr = new StringBuffer();
            String shaHex="";
            for(int i=0;i< digest.length;i++){
                shaHex = Integer.toHexString(digest[i]&0xFF);
                if(shaHex.length()<2){
                    hexStr.append(0);
                }
                hexStr.append(shaHex);
            }
            return hexStr.toString();
        } catch (Exception e) {
            log.info("sha加密生成签名失败");
            return null;
        }
    }

}
