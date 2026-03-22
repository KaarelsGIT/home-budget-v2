package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.StatsCategoryItem;
import ee.kaarel.homebudgetappv2.dto.StatsMonthlyItem;
import ee.kaarel.homebudgetappv2.dto.StatsSummaryResponse;
import ee.kaarel.homebudgetappv2.dto.StatsTrendItem;
import ee.kaarel.homebudgetappv2.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(statisticsService.getSummary(year, month, userId));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<StatsMonthlyItem>> getMonthly(
            @RequestParam Integer year,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(statisticsService.getMonthly(year, userId));
    }

    @GetMapping("/category")
    public ResponseEntity<List<StatsCategoryItem>> getCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(statisticsService.getCategoryBreakdown(year, month, userId));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<StatsTrendItem>> getTrends(
            @RequestParam Integer year,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(statisticsService.getTrends(year, userId));
    }
}
