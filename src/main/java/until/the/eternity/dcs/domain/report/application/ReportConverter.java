package until.the.eternity.dcs.domain.report.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.response.ReportRepliedDetailResponse;
import until.the.eternity.dcs.domain.report.dto.response.ReportRepliedSummaryResponse;
import until.the.eternity.dcs.domain.report.dto.response.ReportRevivedDetailResponse;
import until.the.eternity.dcs.domain.report.dto.response.ReportRevivedSummaryResponse;
import until.the.eternity.dcs.domain.report.entitiy.Report;

@Component
public class ReportConverter {
    public Report fromReportCreateRequestToReport(ReportCreateRequest reportCreateRequest) {
        return Report.builder()
                .targetId(reportCreateRequest.targetId())
                .targetType(reportCreateRequest.targetType())
                .targetUserId(reportCreateRequest.targetUserId())
                .categoryCd(reportCreateRequest.categoryCd())
                .reason(reportCreateRequest.reason())
                .build();
    }

    public ReportRevivedDetailResponse fromReportToReportRevivedDetailResponse(Report report) {
        return ReportRevivedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd())
                .reason(report.getReason())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRevivedSummaryResponse fromReportToReportRevivedSummaryResponse(Report report) {
        return ReportRevivedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType())
                .targetUserId(report.getTargetUserId())
                .categoryCd(report.getCategoryCd())
                .revivedAt(report.getRevivedAt())
                .revivedBy(report.getRevivedBy())
                .build();
    }

    public ReportRepliedDetailResponse fromReportToReportRepliedDetailResponse(Report report) {
        return ReportRepliedDetailResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .userId(report.getUserId())
                .categoryCd(report.getCategoryCd())
                .reason(report.getReason())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }

    public ReportRepliedSummaryResponse fromReportToReportRepliedSummaryResponse(Report report) {
        return ReportRepliedSummaryResponse.builder()
                .Id(report.getId())
                .targetType(report.getTargetType())
                .targetUserId(report.getTargetUserId())
                .categoryCd(report.getCategoryCd())
                .repliedAt(report.getRepliedAt())
                .repliedBy(report.getRepliedBy())
                .build();
    }
}
