package com.socialauction.backend.global;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Checks {
    private Checks() {}

    // 조건이 참이면 잘못된 요청으로 처리
    public static void check(boolean condition, String message) {
        if (condition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
