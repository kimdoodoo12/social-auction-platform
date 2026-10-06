package com.socialauction.backend.member.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialauction.backend.member.dto.UserDto;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;



@RestController @RequestMapping("/api/redis") @RequiredArgsConstructor
public class RedisController {
    private final StringRedisTemplate stringRedisTemplate;
    //
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 전체조회 
    @GetMapping("/member")
    public List<UserDto> findAll() throws JsonMappingException, JsonProcessingException {
        Set<String> keys = stringRedisTemplate.keys("member:*");

        List<UserDto> list = new ArrayList<>();
        for(String key : keys){
            String value = stringRedisTemplate.opsForValue().get(key);

            UserDto userDto = objectMapper.readValue(value, UserDto.class);

            list.add(userDto);
        }
        return list;
    } // 전체조회 end


    // 개별조회 
    @GetMapping("/member/find")
    public UserDto findDto(@RequestParam(name = "mno") Long mno) throws JsonMappingException, JsonProcessingException {
        String findkey = "member:"+mno;
        String value = stringRedisTemplate.opsForValue().get(findkey);
        if(value == null)return null;
        UserDto userDto = objectMapper.readValue(value, UserDto.class);
        return userDto;
    } // 개별조회 end


    // 삭제 
    @DeleteMapping("/member")
    public boolean delete(@RequestParam(name = "mno")Long mno){
        String deleteKey = "member:"+mno;
        boolean result = stringRedisTemplate.delete(deleteKey);
        return result;
    }


    // 수정
    @PutMapping("/member")
    public boolean update(@RequestBody UserDto userDto) throws JsonProcessingException{
        String updateKey = "member:"+userDto.getMemberId();
        if(updateKey == null) return false;

        String value = objectMapper.writeValueAsString(userDto);
        stringRedisTemplate.opsForValue().set(updateKey, value);
        return true;
    }
    





}
