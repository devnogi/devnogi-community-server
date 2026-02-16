package until.the.eternity.dcs.domain.report.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportCategory;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;
import until.the.eternity.dcs.domain.report.exception.CategoryNotFoundException;
import until.the.eternity.dcs.domain.report.exception.TargetNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;

@Component
@RequiredArgsConstructor
public class ReportConverter {
    private final UserSummaryService userSummaryService;

    public Report fromReportCreateRequestToReport(
            ReportCreateRequest reportCreateRequest, Long userId) {
        ReportTargetType targetType =
                ReportTargetType.fromCode(reportCreateRequest.targetType())
                        .orElseThrow(
                                () ->
                                        new TargetNotFoundException(
                                                reportCreateRequest.targetType()));
        ReportCategory reportCategory =
                ReportCategory.fromCode(reportCreateRequest.categoryCd())
                        .orElseThrow(
                                () ->
                                        new CategoryNotFoundException(
                                                reportCreateRequest.categoryCd()));
        return Report.builder()
                .targetId(reportCreateRequest.targetId())
                .targetType(targetType)
                .targetUserId(reportCreateRequest.targetUserId())
                .categoryCd(reportCategory)
                .userId(userId)
                .reason(reportCreateRequest.reason())
                .build();
    }

    public ReportRevivedDetailResponse fromReportToReportRevivedDetailResponse(
            Report report, String username) {
        return ReportRevivedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .targetUsername(username)
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRevivedSummaryResponse fromReportToReportRevivedSummaryResponse(
            Report report, String targetUserName) {
        return ReportRevivedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .targetUsername(targetUserName)
                .categoryCd(report.getCategoryCd().getCode())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRepliedDetailResponse fromReportToReportRepliedDetailResponse(Report report) {
        Long targetUserId = report.getTargetUserId();
        UserSummaryDetailResponse userSummary = userSummaryService.findUserSummary(targetUserId);
        return ReportRepliedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .targetUsername(userSummary.nickname())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }

    public ReportRepliedSummaryResponse fromReportToReportRepliedSummaryResponse(Report report) {
        // todo usersummaryservice 사용 부분 N+1 문제 발생 가능 -> 해결 예정
        Long targetUserId = report.getTargetUserId();
        UserSummaryDetailResponse userSummary = userSummaryService.findUserSummary(targetUserId);
        return ReportRepliedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .targetUsername(userSummary.nickname())
                .categoryCd(report.getCategoryCd().getCode())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }

    public ReportReportedDetailResponse fromReportToReportReportedDetailResponse(Report report) {
        Long targetUserId = report.getTargetUserId();
        UserSummaryDetailResponse userSummary = userSummaryService.findUserSummary(targetUserId);

        return ReportReportedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(targetUserId)
                .targetUsername(userSummary.nickname())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .build();
    }

    public ReportReportedSummaryResponse fromReportToReportReportedSummaryResponse(Report report) {
        Long targetUserId = report.getTargetUserId();
        UserSummaryDetailResponse userSummary = userSummaryService.findUserSummary(targetUserId);

        return ReportReportedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .targetUsername(userSummary.nickname())
                .categoryCd(report.getCategoryCd().getCode())
                .build();
    }

    public ReportPersistResponse fromReportToReportPersistResponse(Report report) {
        return ReportPersistResponse.of(report.getId());
    }
}
