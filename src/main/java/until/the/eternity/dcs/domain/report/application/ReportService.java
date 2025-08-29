package until.the.eternity.dcs.domain.report.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.COMMENT_BLOCKED;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_BLOCKED;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.REPORT_RESULT;
import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import until.the.eternity.dcs.domain.report.exception.ReportModifyForbiddenException;
import until.the.eternity.dcs.domain.report.exception.ReportNotFoundException;
import until.the.eternity.dcs.domain.report.exception.StatusNotFoundException;
import until.the.eternity.dcs.domain.report.infrastructure.ReportRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportConverter reportConverter;
    private final UserService fakeUserService;
    private final RedisSender redisSender;

    @Transactional
    public ReportPersistResponse createReport(ReportCreateRequest request) {
        Report newReport = reportConverter.fromReportCreateRequestToReport(request);
        return reportConverter.fromReportToReportPersistResponse(reportRepository.save(newReport));
    }

    public ReportRepliedDetailResponse getRepliedReport(Long id) {
        Report report = findById(id);
        return reportConverter.fromReportToReportRepliedDetailResponse(report);
    }

    public ReportRevivedDetailResponse getRevivedReport(Long id) {
        Report report = findById(id);
        return reportConverter.fromReportToReportRevivedDetailResponse(report);
    }

    public ReportReportedDetailResponse getReportedReport(Long id) {
        Report report = findById(id);
        return reportConverter.fromReportToReportReportedDetailResponse(report);
    }

    public Page<ReportRepliedSummaryResponse> findRepliedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> repliedReports =
                reportRepository.findAllByStatusCd(ReportStatus.ACCEPT, pageable);

        return repliedReports.map(reportConverter::fromReportToReportRepliedSummaryResponse);
    }

    public Page<ReportRevivedSummaryResponse> findRevivedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> revivedReports =
                reportRepository.findAllByStatusCd(ReportStatus.REJECT, pageable);

        return revivedReports.map(reportConverter::fromReportToReportRevivedSummaryResponse);
    }

    public Page<ReportReportedSummaryResponse> findReportedReports(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Report> reportedReports =
                reportRepository.findAllByStatusCd(ReportStatus.REPORTED, pageable);

        return reportedReports.map(reportConverter::fromReportToReportReportedSummaryResponse);
    }

    @Transactional
    public ReportPersistResponse updateReport(Long id, ReportUpdateRequest reportUpdateRequest) {
        UserSummary user = getCurrentUser();

        checkManagerAuthority(user.getGrade());

        ReportStatus status =
                ReportStatus.fromCode(reportUpdateRequest.statusCd())
                        .orElseThrow(
                                () -> new StatusNotFoundException(reportUpdateRequest.statusCd()));

        Report report = findById(id);
        LocalDateTime time = LocalDateTime.now();
        Long userId = user.getId();

        if (status.equals(ReportStatus.ACCEPT)) {
            report.update(status, time, userId, null, null);
            sendNotice(report);
        } else if (status.equals(ReportStatus.REJECT)) {
            report.update(status, null, null, time, userId);
        }

        return reportConverter.fromReportToReportPersistResponse(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        UserSummary user = getCurrentUser();
        checkManagerAuthority(user.getGrade());
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

    private UserSummary getCurrentUser() {
        return fakeUserService.getCurrentUser();
    }

    private void checkManagerAuthority(UserGrade grade) {
        if (!grade.equals(ADMIN)) {
            throw new ReportModifyForbiddenException();
        }
    }

    private Report findById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ReportNotFoundException(id));
    }
}
