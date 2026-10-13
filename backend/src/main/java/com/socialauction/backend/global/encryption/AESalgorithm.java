package com.socialauction.backend.global.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class AESalgorithm {
    /*
        평문을 받아서 AES 암호화를 진행함. 
        평문은 16바이트(128비트)단위로 쪼갠다.
        비밀키의 크기는 128비트로 한다. (16바이트)
        암호화 횟수는 10회로 한다. 
        암호화 방식
        1. 바이트 값 치환
        2. 행을 왼쪽으로 이동
        3. 열 내부 데이터 혼합
        4. 라운드 키와 XOR
    */
    public String AESencryption(String plaintext){
        return "아직 미구현입니다.";
    }
     public static void main(String[] args) {

        // 1. 원본 문자열
        String id = "qwer1234";

        // 2. UTF-8 인코딩
        byte[] bytes = id.getBytes(StandardCharsets.UTF_8);

        // 3. 바이트 배열 출력
        System.out.println(Arrays.toString(bytes));

        // 4. 다시 문자열로 변환
        String result = new String(bytes, StandardCharsets.UTF_8);

        System.out.println(result);
    }

}
