package com.socialauction.backend.global.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component 
public class AESdecryption {
    
    /*
        복호화는 암호화 역순으로진행
    */
   
   public static String AESDecryption(String cryptogram , String secretkey){
        // 받은 암호문  base64 디코딩 
        byte[] block = Base64.getDecoder().decode(cryptogram);

        // 암호화 할때 사용한 키 인코딩 
        final byte[] key = secretkey.getBytes(StandardCharsets.UTF_8);

        // 마지막 라운드 키 구하기 (10라운드) 후 XOR 연산
        byte[] key10 = makekeyExpansion(key,10);
        for(int i = 0 ; i<block.length ; i++){
            block[i] = (byte)(block[i] ^ key10[i]);
        }

        // 라운드 9부터 1까지 반복
        for(int i = 9 ; i >= 1 ; i--){
            block = invShiftRows(block);        // 행이동
            block = invSubbytes(block);         // 바이트 치환
            AESencryption.AddRoundKey(block, makekeyExpansion(key, i)); // 라운드 키 추가
            block = invmixColumns(block);       // 열혼합 
        }
        // 마지막 역행 , 마지막 역바이트 치환
        block = invShiftRows(block);
        block = invSubbytes(block);

        // 초기 비밀키와 XOR 연산(0라운드 키)
        block = AESencryption.AddRoundKey(block, key);

        // 패딩제거
        block = invaddPadding(block);
        // UTF_8문자열로 변환 -> 평문
        String result = new String(block , StandardCharsets.UTF_8);
        
        return result;
   } //복호화 끝

   // shiftRow함수 
    public static byte[] invShiftRows(byte[] text){
        for( int i = 1 ; i <= 3 ; i++){
            switch (i) {
                case 1:
                    byte box;
                    box = text[1];
                    text[1] = text[13];
                    text[13] = text[9];
                    text[9] = text[5];
                    text[5] = box;
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
                    text[3] = text[7];
                    text[7] = text[11];
                    text[11] = text[15];
                    text[15] = box;
                    break;
            }// switch end 
        } // for end
        return text;
    } // invshiftRows end


    // invSubbytes
    public static  byte[] invSubbytes(byte[] text){
        // for(byte bite : text){
        //     bite = (byte)AESConstants.invSBOX[bite & 0xFF];
        // }
        for(int i = 0 ; i < text.length; i++){
            text[i] = (byte)AESConstants.invSBOX[text[i] & 0xFF ]; 
        }
        return text;
    } // invSubbyte

    // 특정 번호 라운드 키 만들기 
    public static byte[] makekeyExpansion(byte[] key , int round){
        for(int i = 1 ; i <= round ; i++){
            key = AESencryption.keyExpansion(key, i);
        }
        return key;
    } // 특정 번호 라운드키 만들기 end

    // mul8
    public static  byte Mul8(byte b){
        return AESencryption.Mul2(AESencryption.Mul2(AESencryption.Mul2(b)));
    }

    // mul 4
    public static byte Mul4(byte b){
        return AESencryption.Mul2(AESencryption.Mul2(b));
    }

    // invmixColumns 연산
    public static byte[] invmixColumns(byte[] text){
        byte[] newtext = new byte[16];
        for(int i = 0 ; i < 16 ; i = i+4){
            byte a0 = text[i];
            byte a1 = text[i+1];
            byte a2 = text[i+2];
            byte a3 = text[i+3];
            
            /*
                invMixColumns 규칙 
                b0,1 = (14 × a0,1) ⊕ (11 × a1,1) ⊕ (13 × a2,1) ⊕ (9 × a3,1)
                b1,1 = (9 × a0,1) ⊕ (14 × a1,1) ⊕ (11 × a2,1) ⊕ (13 × a3,1)
                b2,1 = (13 × a0,1) ⊕ (9 × a1,1) ⊕ (14 × a2,1) ⊕ (11 × a3,1)
                b3,1 = (11 × a0,1) ⊕ (13 × a1,1) ⊕ (9 × a2,1) ⊕ (14 × a3,1)
            */

            newtext[i] = (byte)( (Mul8(a0)^Mul4(a0)^AESencryption.Mul2(a0)) ^ ((Mul8(a1)) ^ AESencryption.Mul3(a1)) ^ ((Mul8(a2) ^ Mul4(a2) ^ a2)) ^ (Mul8(a3) ^ a3 )  );
            newtext[i+1] = (byte)( (Mul8(a0) ^ a0 ) ^ (Mul8(a1)^Mul4(a1)^AESencryption.Mul2(a1)) ^ ((Mul8(a2)) ^ AESencryption.Mul3(a2)) ^ ((Mul8(a3) ^ Mul4(a3) ^ a3))  );
            newtext[i+2] = (byte)( ((Mul8(a0) ^ Mul4(a0) ^ a0)) ^ (Mul8(a1) ^ a1 ) ^ (Mul8(a2)^Mul4(a2)^AESencryption.Mul2(a2)) ^ ((Mul8(a3)) ^ AESencryption.Mul3(a3))  );
            newtext[i+3] = (byte)( ((Mul8(a0)) ^ AESencryption.Mul3(a0)) ^ ((Mul8(a1) ^ Mul4(a1) ^ a1)) ^ (Mul8(a2) ^ a2 ) ^ (Mul8(a3)^Mul4(a3)^AESencryption.Mul2(a3))  );
        }

        return newtext;
    } // invmixColumns 연산 end 

    // 블록제거 함수 
    public static byte[] invaddPadding(byte[] block){
        int plaintextsize = (int)block[15];
        byte[] plaintext =new byte[16-plaintextsize];
        for( int i = 0 ; i < 16-plaintextsize; i++){
            plaintext[i] = block[i];
        }
        return plaintext;
    } // 블록제거 함수

}
