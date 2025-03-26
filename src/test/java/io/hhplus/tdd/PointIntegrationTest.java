package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
public class PointIntegrationTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("유저 포인트 충전/사용/내역조회/조회 통합 테스트")
    public void chargeUserPointTest() throws Exception {
        // given
        long userId = 1;
        long chargePoint = 100;
        long usePoint = 50;

        // when
        UserPoint chargeResult = pointService.charge(userId, chargePoint); // 포인트 충전
        UserPoint useResult = pointService.use(userId, usePoint);   // 포인트 사용
        UserPoint userInfo = pointService.point(userId);    // 포인트 잔액 확인
        List<PointHistory> pointHistoryList = pointService.history(userId);

        // then
        assertEquals(chargePoint, chargeResult.point());   // 충전확인
        assertEquals(userId, chargeResult.id());  // 충전 유저 확인
        assertEquals(chargePoint - usePoint, useResult.point());   // 사용확인
        assertEquals(userId, useResult.id());  // 사용 유저 확인
        assertEquals(chargePoint - usePoint, userInfo.point()); // 잔여 포인트가 충전 후 사용 된 포인트와 동일한지 확인
        assertThat(pointHistoryList).hasSize(2);   // 유저 포인트 충전/사용 내역 조회
        assertThat(pointHistoryList).extracting(PointHistory::type).contains(TransactionType.CHARGE); // 유저 포인트 충전 내역 조회
        assertThat(pointHistoryList).extracting(PointHistory::type).contains(TransactionType.USE); // 유저 포인트 사용 내역 조회
    }

    @Test
    @DisplayName("유저 포인트 대량 충전 테스트")
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
        assertNotEquals(point * threadCount, userInfo.point());
        /**
         * 멀티쓰레드를 생성하여 포인트 충전을 진행하였으나, 예상하는 포인트 보유량과 다르게 포인트가 충전 됨
         * 동시성 이슈 발생 => 해결방안은?
         */
    }

}
