package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class PointUnitTest {

    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;
    @InjectMocks
    private PointService pointService;

    @Test
    public void 유저_포인트_조회_테스트() {
        // given
        long id = 1;

        // when
        when(userPointTable.selectById(id)).thenReturn(new UserPoint(id, 0, System.currentTimeMillis()));

        UserPoint userPoint = pointService.point(id);

        // then
        long checkPoint = 0;
        assertEquals(checkPoint, userPoint.point());
    }

    @Test
    public void 유저의_포인트_충전_이용_내역을_조회_테스트() {
        // given
        long id = 1;
        List<PointHistory> pointHistoryList = new ArrayList<>();
        PointHistory pointHistory = new PointHistory(1, id, 10, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryList.add(pointHistory);
        PointHistory pointHistory2 = new PointHistory(2, id, 5, TransactionType.USE, System.currentTimeMillis());
        pointHistoryList.add(pointHistory2);

        // when
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(pointHistoryList);
        List<PointHistory> pointHistories = pointService.history(id);

        // then
        assertThat(pointHistories).hasSize(2);
        assertThat(pointHistories).extracting(PointHistory::type).contains(TransactionType.CHARGE);
        assertThat(pointHistories).extracting(PointHistory::type).contains(TransactionType.USE);
    }
    
    @Test
    public void 유저의_포인트를_충전하는_기능() throws Exception {
        // given
        long id = 1;    // userId
        long amount = 10;   // charge point

        // when
        when(userPointTable.insertOrUpdate(id, amount)).thenReturn(new UserPoint(id, amount, System.currentTimeMillis()));
        UserPoint result = pointService.charge(id, amount);

        // then
        assertEquals(result.point(), amount);   // 충전포인트가 충전됐는지 확인
        assertEquals(result.id(), id);  // 충전요청한 아이디로 충전됐는지 확인
    }

}
