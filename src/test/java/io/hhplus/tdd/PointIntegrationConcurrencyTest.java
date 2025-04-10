package io.hhplus.tdd;

import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointIntegrationConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("동시성 제어 적용 유저 포인트 대량 충전 테스트")
    public void manyChargeUserPointTest() throws Exception {
        // given
        long userId = 1;
        long point = 10;
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<4; i++) {
            executor.submit(() -> {
                pointService.charge(userId, point); // 멀티스레드 활용, 포인트 충전
            });
        }
        latch.await(3, TimeUnit.SECONDS);
        executor.shutdown();

        UserPoint userInfo = pointService.point(userId);

        // then
        assertEquals(point * threadCount, userInfo.point());
    }

    @Test
    @DisplayName("동시성 제어 적용 유저 포인트 사용 테스트")
    public void useUserPointTest() throws Exception {
        // given
        long userId = 1;
        long chargePoint = 10;
        long usePoint = 2;
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<threadCount; i++) {
            executor.submit(() -> {
                pointService.charge(userId, chargePoint); // 멀티스레드 활용, 포인트 충전
            });
        }
        for(int i=0; i<threadCount; i++) {
            executor.submit(() -> {
                pointService.use(userId, usePoint); // 멀티스레드 활용, 포인트 사용
            });
        }
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        UserPoint userInfo = pointService.point(userId);

        // then
        assertEquals(chargePoint * threadCount - usePoint * threadCount, userInfo.point());
    }

    @Test
    @DisplayName("동시성 제어 적용 유저별 충전 테스트")
    public void chargeRandomUserTest() throws Exception {
        // given
        long point = 10;
        int threadCount = 4;
        Map<Long, Long> userInfo = new HashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<threadCount; i++) {
            long userId = i+1;
            executor.submit(() -> {
                pointService.charge(userId, point); // 멀티스레드 활용, 포인트 충전
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        for(int i=0; i<threadCount; i++) {
            long userId = i+1;
            UserPoint resultUserInfo = pointService.point(userId);
            userInfo.put(userId, resultUserInfo.point());
        }

        // then
        for(int i=0; i<threadCount; i++) {
            long userId = i+1;
            assertEquals(point, userInfo.get(userId));
        }
    }
}
