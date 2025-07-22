package until.the.eternity.dcs.domain.report.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportCategory;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;
import until.the.eternity.dcs.domain.report.exception.CategoryNotFoundException;
import until.the.eternity.dcs.domain.report.exception.TargetNotFoundException;

@Component
public class ReportConverter {
    public Report fromReportCreateRequestToReport(ReportCreateRequest reportCreateRequest) {
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
                .reason(reportCreateRequest.reason())
                .build();
    }

    public ReportRevivedDetailResponse fromReportToReportRevivedDetailResponse(Report report) {
        return ReportRevivedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRevivedSummaryResponse fromReportToReportRevivedSummaryResponse(Report report) {
        return ReportRevivedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRepliedDetailResponse fromReportToReportRepliedDetailResponse(Report report) {
        return ReportRepliedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }

    public ReportRepliedSummaryResponse fromReportToReportRepliedSummaryResponse(Report report) {
        return ReportRepliedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }

    public ReportReportedDetailResponse fromReportToReportReportedDetailResponse(Report report) {
        return ReportReportedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .reason(report.getReason())
                .build();
    }

    public ReportReportedSummaryResponse fromReportToReportReportedSummaryResponse(Report report) {
        return ReportReportedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType().getCode())
                .targetUserId(report.getTargetUserId())
                .categoryCd(report.getCategoryCd().getCode())
                .build();
    }

    public ReportPersistResponse fromReportToReportPersistResponse(Report report) {
        return ReportPersistResponse.of(report.getId());
    }
}
