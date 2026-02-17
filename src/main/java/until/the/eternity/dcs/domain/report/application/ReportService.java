package until.the.eternity.dcs.domain.report.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.*;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.COMMENT_BLOCKED;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_BLOCKED;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.REPORT_RESULT;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.request.ReportUpdateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;
import until.the.eternity.dcs.domain.report.exception.ReportNotFoundException;
import until.the.eternity.dcs.domain.report.exception.ReportStatusNotMatchException;
import until.the.eternity.dcs.domain.report.exception.StatusNotFoundException;
import until.the.eternity.dcs.domain.report.infrastructure.ReportRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportConverter reportConverter;
    private final RedisSender redisSender;
    private final ReportPermissionEvaluator reportPermissionEvaluator;
    private final UserSummaryService userSummaryService;

    @Transactional
    @PreAuthorize("@reportPermissionEvaluator.canCreate(authentication)")
    public ReportPersistResponse createReport(ReportCreateRequest request) {
        Long userId = getCurrentUserId();
        Report newReport = reportConverter.fromReportCreateRequestToReport(request, userId);
        return reportConverter.fromReportToReportPersistResponse(reportRepository.save(newReport));
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public ReportRepliedDetailResponse getRepliedReport(Long id) {
        Report report = findById(id);
        reportStatusValid(report, ReportStatus.ACCEPT);
        UserSummaryDetailResponse userSummary =
                userSummaryService.findUserSummary(report.getTargetUserId());

        return reportConverter.fromReportToReportRepliedDetailResponse(
                report, userSummary.nickname());
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public ReportRevivedDetailResponse getRevivedReport(Long id) {
        Report report = findById(id);
        reportStatusValid(report, ReportStatus.REJECT);
        UserSummaryDetailResponse userSummary =
                userSummaryService.findUserSummary(report.getTargetUserId());

        return reportConverter.fromReportToReportRevivedDetailResponse(
                report, userSummary.nickname());
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public ReportReportedDetailResponse getReportedReport(Long id) {
        Report report = findById(id);
        reportStatusValid(report, ReportStatus.REPORTED);
        UserSummaryDetailResponse userSummary =
                userSummaryService.findUserSummary(report.getTargetUserId());

        return reportConverter.fromReportToReportReportedDetailResponse(
                report, userSummary.nickname());
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public Page<ReportRepliedSummaryResponse> findRepliedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> repliedReports =
                reportRepository.findAllByStatusCd(ReportStatus.ACCEPT, pageable);

        List<Long> userIdList =
                repliedReports.stream().map(Report::getTargetUserId).distinct().toList();
        Map<Long, UserSummary> userSummaryMap =
                userSummaryService.findByIdIn(userIdList).stream()
                        .collect(Collectors.toMap(UserSummary::getId, u -> u));

        return repliedReports.map(
                report -> {
                    UserSummary userSummary = userSummaryMap.get(report.getTargetUserId());
                    String nickname = (userSummary != null) ? userSummary.getNickname() : "알수없음";
                    return reportConverter.fromReportToReportRepliedSummaryResponse(
                            report, nickname);
                });
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public Page<ReportRevivedSummaryResponse> findRevivedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> revivedReports =
                reportRepository.findAllByStatusCd(ReportStatus.REJECT, pageable);

        List<Long> userIdList =
                revivedReports.stream().map(Report::getTargetUserId).distinct().toList();
        Map<Long, UserSummary> userSummaryMap =
                userSummaryService.findByIdIn(userIdList).stream()
                        .collect(Collectors.toMap(UserSummary::getId, u -> u));

        return revivedReports.map(
                report -> {
                    UserSummary userSummary = userSummaryMap.get(report.getTargetUserId());
                    String nickname = (userSummary != null) ? userSummary.getNickname() : "알수없음";
                    return reportConverter.fromReportToReportRevivedSummaryResponse(
                            report, nickname);
                });
    }

    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public Page<ReportReportedSummaryResponse> findReportedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> reportedReports =
                reportRepository.findAllByStatusCd(ReportStatus.REPORTED, pageable);

        List<Long> userIdList =
                reportedReports.stream().map(Report::getTargetUserId).distinct().toList();
        Map<Long, UserSummary> userSummaryMap =
                userSummaryService.findByIdIn(userIdList).stream()
                        .collect(Collectors.toMap(UserSummary::getId, u -> u));

        return reportedReports.map(
                report -> {
                    UserSummary userSummary = userSummaryMap.get(report.getTargetUserId());
                    String nickname = (userSummary != null) ? userSummary.getNickname() : "알수없음";
                    return reportConverter.fromReportToReportReportedSummaryResponse(
                            report, nickname);
                });
    }

    @Transactional
    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public ReportPersistResponse updateReport(Long id, ReportUpdateRequest reportUpdateRequest) {

        ReportStatus status =
                ReportStatus.fromCode(reportUpdateRequest.statusCd())
                        .orElseThrow(
                                () -> new StatusNotFoundException(reportUpdateRequest.statusCd()));

        Report report = findById(id);
        LocalDateTime time = LocalDateTime.now();
        Long userId = getCurrentUserId();

        if (status.equals(ReportStatus.ACCEPT)) {
            report.update(status, time, userId, null, null);
            sendNotice(report);
        } else if (status.equals(ReportStatus.REJECT)) {
            report.update(status, null, null, time, userId);
        }

        return reportConverter.fromReportToReportPersistResponse(report);
    }

    @Transactional
    @PreAuthorize("@reportPermissionEvaluator.isAuthorized(authentication)")
    public void deleteReport(Long id) {
        findById(id);
        reportRepository.deleteById(id);
    }

    private void sendNotice(Report report) {
        redisSender.enqueue(NotificationJob.of(report.getUserId(), REPORT_RESULT, report.getId()));
        if (report.getTargetType() == ReportTargetType.POST) {
            redisSender.enqueue(
                    NotificationJob.of(
                            report.getTargetUserId(), POST_BLOCKED, report.getTargetId()));
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            redisSender.enqueue(
                    NotificationJob.of(
                            report.getTargetUserId(), COMMENT_BLOCKED, report.getTargetId()));
        }
    }

    private Report findById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ReportNotFoundException(id));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return reportPermissionEvaluator.getCurrentUserId(auth);
    }

    private void reportStatusValid(Report report, ReportStatus status) {
        if (!report.getStatusCd().equals(status)) {
            throw new ReportStatusNotMatchException(report.getStatusCd());
        }
    }
}
