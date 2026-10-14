package com.socialauction.backend.global.encryption;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component 
public class AESencryption {

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

        *** 현제 평문이 반드시 15바이트 이하이여야 됨.  ***
    */

    
    public static String AES(String plaintext , String secretkey){
        // 받은 평문을 인코딩
        byte[] bytes = plaintext.getBytes(StandardCharsets.UTF_8);

        // 블록 만드는 함수에 전달 
        byte[] block = addPadding(bytes); 

        // 평문을 암호화 하기전에 수행하는 최초에 xor
        
        byte [] key = secretkey.getBytes(StandardCharsets.UTF_8);

        // 4. 최초 비밀키와 XOR (0라운드)
        block = AddRoundKey(block, key);

        // 5. AES 10라운드 반복
        for(int round = 1; round <= 10; round++){

            // 바이트 치환
            block = SubBytes(block);
            // 행 이동
            block = ShiftRows(block);

            // 열 혼합 (마지막 라운드는 생략)
            if(round != 10){
                block = MixColumns(block);
            }
            // 다음 라운드 키 생성
            key = keyExpansion(key, round);
            // 라운드 키와 XOR
            block = AddRoundKey(block, key);
            System.out.println(round + "라운드 완료");
        }

        // Base64 인코딩
        String result = Base64.getEncoder().encodeToString(block);
        // 결과 출력
        System.out.println(result); 

        return result;
    } // AES end

    // 블록만드는 함수 / 집어넣는 배열의 크기 최대 15바이트 이하로 제한 
    public static byte[] addPadding(byte[] plaintext){
        // 총길이 16바이트 
        int blocksize = 16;
        
        // 패딩에 필요한 크기 저장
        int needsize = blocksize - (plaintext.length);

        // 패딩을 담을 새 배열 추가 
        byte[] block = new byte[plaintext.length + needsize];

        for(int i = 0 ; i < blocksize; i++){
            if( i < plaintext.length){
                block[i] = plaintext[i];
            }
            else{ block[i] = (byte)needsize; } 
        }
        return block;
    } // 블록만드는 함수 end

    public static byte[] SubBytes(byte[] text){
        for(int i = 0 ; i < text.length ; i++){
            // byte자료형 특성상 -128 ~ 127범위이므로 & 0xFF를 해주어 0~255로 변환
            // byte 로 다시 형변환
            text[i] = (byte)AESConstants.SBOX[text[i] & 0xFF ];
        }
        return text;
    }

    // shiftRow함수 
    public static byte[] ShiftRows(byte[] text){
        for( int i = 1 ; i <= 3 ; i++){
            switch (i) {
                case 1:
                    byte box;
                    box = text[1];
                    text[1] = text[5];
                    text[5] = text[9];
                    text[9] = text[13];
                    text[13] = box;
                    break;
                case 2:
                    box = text[2];
                    text[2] = text[10];
                    text[10] = box;
                    box = text[6];
                    text[6] = text[14];
                    text[14] = box;
                    break;
                case 3:
                    box = text[3];
                    text[3] = text[15];
                    text[15] = text[11];
                    text[11] = text[7];
                    text[7] = box;
                    break;
            }// switch end 
        } // for end
        return text;
    } // shiftRows end

    // MixColumns
    public static byte[] MixColumns(byte[] text){
        byte[] newtext = new byte[16];


        // mixcolumns연산
        for(int i = 0 ; i < 16; i = i+4){
            byte a0 = text[i];
            byte a1 = text[i+1];
            byte a2 = text[i+2];
            byte a3 = text[i+3];

            /*
                b0,1 = (2 × a0,1) ⊕ (3 × a1,1) ⊕ (1 × a2,1) ⊕ (1 × a3,1)
                b1,1 = (1 × a0,1) ⊕ (2 × a1,1) ⊕ (3 × a2,1) ⊕ (1 × a3,1)
                b2,1 = (1 × a0,1) ⊕ (1 × a1,1) ⊕ (2 × a2,1) ⊕ (3 × a3,1)
                b3,1 = (3 × a0,1) ⊕ (1 × a1,1) ⊕ (1 × a2,1) ⊕ (2 × a3,1)
            */

            newtext[i] = (byte)(Mul2(a0) ^ Mul3(a1) ^ a2 ^ a3);
            newtext[i+1] = (byte)(a0 ^ Mul2(a1) ^ Mul3(a2) ^ a3);
            newtext[i+2] = (byte)(a0 ^ a1 ^ Mul2(a2) ^ Mul3(a3));
            newtext[i+3] = (byte)(Mul3(a0) ^ a1 ^ a2 ^ Mul2(a3));
        } // 새 배열에 복사 끝
        
        return newtext;

    } // mixcolumns end

    // LeftShift
    // 오버플로우가 생겼을때 AES 표준에 정해진 값 AES 표준에 정해진 값 와 XOR 연산을 한다. 
    public static byte Mul2(byte b){
        if((b & 0x80) != 0){  
            return (byte)((b << 1) ^ 0x1B);
        }
        return (byte)(b << 1);
    }
    // 곱하기 3 / LeftShift하고 나온 결과와 기존값을 XOR 연산한다. 
    public static byte Mul3(byte b) {
        return (byte)(Mul2(b) ^ b);
    }

    // 라운드키 만들기 
    public static byte[] keyExpansion(byte[] key , int round){
        // 0~3바이트 정해진수와 xor
        // 4 ~ 15바이트 0~11바이트와 xor
        // 새로운 라운드 키 저장 
        byte[] newKey = new byte[16];


        byte[] temp = { key[13], key[14], key[15], key[12]};
        temp = SubBytes(temp);

        // 정해진 숫자와 xor 연산 / RCON : 라운드키 만들떄 필요한 정해진 수
        temp[0] = (byte)(temp[0] ^ AESConstants.RCON[round]);

        //새로운 키 첫 4바이트
        for(int i = 0 ; i < 4 ; i++){
            newKey[i] = (byte)(key[i] ^ temp[i]);
        }
        // 나머지 5바이트 부터 16바이트
        for(int i = 4; i < 16; i++){
            newKey[i] = (byte)(key[i] ^ newKey[i-4]);
        }
        return newKey;
    } // 라운드 키 만들기 end

    // AddRoundKey함수
    public static byte[] AddRoundKey(byte[] text, byte[] key) {
        // mixColumns와 라운드 키 xor
        for(int i = 0; i < 16; i++){
            text[i] = (byte)(text[i] ^ key[i]);
        }
        return text;
    } // addRoundKey end

}// 암호 class end

