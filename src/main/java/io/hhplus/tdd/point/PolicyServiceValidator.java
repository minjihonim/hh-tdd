package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

@Component
public class PolicyServiceValidator {

    // 사용 포인트가 보유 포인트보다 부족할 경우 사용 불가
    public void  validateUseUserPoint(UserPoint userPoint, long amount) {
        if(userPoint.point() < amount) {
            throw new RuntimeException("보유 포인트가 부족합니다");
        }
    }

}
