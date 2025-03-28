package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;
    private final PolicyControllerValidator policyValidator;

    public PointController(PointService pointService, PolicyControllerValidator policyValidator) {
        this.pointService = pointService;
        this.policyValidator = policyValidator;
    }

    /**
     * 특정 유저의 포인트를 조회
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        policyValidator.validateUserId(id);
        return pointService.point(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        policyValidator.validateUserId(id);
        return pointService.history(id);
    }

    /**
     * 유저의 포인트를 충전하는 기능
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        policyValidator.validateUserId(id);
        policyValidator.validateCharePoint(amount);
        return pointService.charge(id, amount);
    }

    /**
     * 유저의 포인트를 사용하는 기능
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        policyValidator.validateUserId(id);
        policyValidator.validateUsePoint(amount);
        return pointService.use(id, amount);
    }
}
