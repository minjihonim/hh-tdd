package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PolicyServiceValidator policyServiceValidator;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, PolicyServiceValidator policyServiceValidator) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.policyServiceValidator = policyServiceValidator;
    }
    
    /**
     * 특정 유저의 포인트를 조회
     */
    public UserPoint point(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     */
    public List<PointHistory> history(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
    
    /**
     * 유저의 포인트를 충전하는 기능
     */
    public UserPoint charge(long id, long amount) {
        ReentrantLock user = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        user.lock();
        try {
            // 유저 포인트 조회
            UserPoint userPoint = userPointTable.selectById(id);
            // 히스토리 등록
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
            // 보유 포인트 + 충전 포인트
            long updatePoint = userPoint.point() + amount;
            return userPointTable.insertOrUpdate(id, updatePoint);
        } finally {
            user.unlock();
        }
    }

    /**
     * 유저의 포인트를 사용하는 기능
     */
    public UserPoint use(long id, long amount) {
        ReentrantLock user = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        user.lock();
        try {
            // 유저 포인트 조회
            UserPoint userPoint = userPointTable.selectById(id);
            policyServiceValidator.validateUseUserPoint(userPoint, amount);
            // 히스토리 등록
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
            // 남은 포인트 계산
            long afterPoint = userPoint.point() - amount;
            return userPointTable.insertOrUpdate(id, afterPoint);
        } finally {
            user.unlock();
        }
    }
}
