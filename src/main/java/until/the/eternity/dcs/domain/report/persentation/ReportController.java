package until.the.eternity.dcs.domain.report.persentation;

import static org.springframework.http.HttpStatus.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.common.response.CustomPageResponse;
import until.the.eternity.dcs.domain.report.application.ReportService;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.request.ReportUpdateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    @Operation(
            summary = "신고 생성 API",
            description = """
			- Description : 이 API는 신고를 생성합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = ReportPersistResponse.class)))
    public ResponseEntity<ReportPersistResponse> createReport(
            @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(CREATED).body(reportService.createReport(request));
    }

    @GetMapping("/reported/{id}")
    @Operation(
            summary = "처리된 신고 조회  API",
            description = """
			- Description : 이 API는 접수된 신고를 단건 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = ReportReportedDetailResponse.class)))
    public ReportReportedDetailResponse getReportedReport(@PathVariable Long id) {
        return reportService.getReportedReport(id);
    }

    @GetMapping("/replied/{id}")
    @Operation(
            summary = "처리된 신고 조회  API",
            description = """
			- Description : 이 API는 처리된 신고를 단건 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = ReportRepliedDetailResponse.class)))
    public ReportRepliedDetailResponse getRepliedReport(@PathVariable Long id) {
        return reportService.getRepliedReport(id);
    }

    @GetMapping("/revived/{id}")
    @Operation(
            summary = "복구된 신고 조회 API",
            description = """
			- Description : 이 API는 복구된 신고를 단건조회 합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = ReportRevivedDetailResponse.class)))
    public ReportRevivedDetailResponse getRevivedReport(@PathVariable Long id) {
        return reportService.getRevivedReport(id);
    }

    @GetMapping("/replied")
    @Operation(
            summary = "처리된 신고 리스트 조회 API",
            description =
                    """
			- Description : 이 API는 처리된 신고 리스트를 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = ReportRepliedSummaryResponse.class)))
    public CustomPageResponse<ReportRepliedSummaryResponse> getRepliedReports(
            @ModelAttribute CustomPageRequest request) {
        return CustomPageResponse.from(reportService.findRepliedReports(request));
    }

    @GetMapping("/revived")
    @Operation(
            summary = "복구된 신고 리스트 조회 API",
            description =
                    """
			- Description : 이 API는 복구된 신고 리스트를 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = ReportRevivedSummaryResponse.class)))
    public CustomPageResponse<ReportRevivedSummaryResponse> getRevivedReports(
            @ModelAttribute CustomPageRequest request) {
        return CustomPageResponse.from(reportService.findRevivedReports(request));
    }

    @GetMapping("/reported")
    @Operation(
            summary = "복구된 신고 리스트 조회 API",
            description =
                    """
			- Description : 이 API는 접수된 신고 리스트를 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(
                            schema = @Schema(implementation = ReportReportedSummaryResponse.class)))
    public CustomPageResponse<ReportReportedSummaryResponse> getRevivedReports(
            @ModelAttribute CustomPageRequest request) {
        return CustomPageResponse.from(reportService.findReportedReports(request));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "신고 상태 수정 API",
            description = """
			- Description : 이 API는 신고 상태를 수정합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ReportPersistResponse.class)))
    public ResponseEntity<ReportPersistResponse> updatePost(
            @PathVariable Long id, @Valid @RequestBody ReportUpdateRequest ReportUpdateRequest) {
        return ResponseEntity.status(OK).body(reportService.updatePost(id, ReportUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "신고 삭제 API",
            description = """
			- Description : 이 API는 신고를 삭제합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
