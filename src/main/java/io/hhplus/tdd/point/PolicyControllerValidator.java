package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

@Component
public class PolicyControllerValidator {

    // userId 검증
    public void validateUserId(long id) {
        if(id < 1) {
            throw new IllegalArgumentException("잘못된 유저 아이디 입니다.");
        }
    }
    
    // 충전 포인트 검증
    public void validateCharePoint(long amount) {
        if(amount < 1) {
            throw new IllegalArgumentException("포인트 충전 최소 금액은 1포인트 이상이여야 합니다..");
        }
    }

    // 사용 포인트 검증
    public void validateUsePoint(long amount) {
        if(amount < 1) {
            throw new IllegalArgumentException("포인트 사용 최소 금액은 1포인트 이상이여야 합니다.");
        }
    }
}
