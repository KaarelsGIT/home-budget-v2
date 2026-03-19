package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.FamilyOverviewResponse;
import ee.kaarel.homebudgetappv2.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/family-overview")
    public ResponseEntity<FamilyOverviewResponse> getFamilyOverview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(reportService.getFamilyOverview(selectedYear, month));
    }
}
