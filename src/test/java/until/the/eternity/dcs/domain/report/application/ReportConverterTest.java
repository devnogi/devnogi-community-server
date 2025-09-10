package until.the.eternity.dcs.domain.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportCategory;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportConverter 테스트")
class ReportConverterTest {

    @InjectMocks private ReportConverter reportConverter;

    private ReportCreateRequest reportCreateRequest;
    private Report report;
    Long userId = 1L;

    @BeforeEach
    void setUp() {
        LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

        reportCreateRequest =
                new ReportCreateRequest(
                        "POST", // targetType
                        1L, // targetId
                        100L, // targetUserId
                        "SPAM", // categoryCd
                        "스팸 게시물입니다" // reason
                        );

        report =
                Report.builder()
                        .id(1L)
                        .targetId(1L)
                        .targetType(ReportTargetType.POST)
                        .targetUserId(100L)
                        .userId(200L)
                        .categoryCd(ReportCategory.SPAM)
                        .reason("스팸 게시물입니다")
                        .revivedAt(testDateTime)
                        .revivedBy(1L)
                        .repliedAt(testDateTime.plusDays(1))
                        .repliedBy(2L)
                        .build();
    }

    @Test
    @DisplayName("ReportCreateRequest를 Report로 변환 - 정상 케이스")
    void fromReportCreateRequestToReport_Success() {
        // when
        Report result =
                reportConverter.fromReportCreateRequestToReport(reportCreateRequest, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTargetId()).isEqualTo(reportCreateRequest.targetId());
        assertThat(result.getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(result.getTargetUserId()).isEqualTo(reportCreateRequest.targetUserId());
        assertThat(result.getCategoryCd()).isEqualTo(ReportCategory.SPAM);
        assertThat(result.getReason()).isEqualTo(reportCreateRequest.reason());
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("ReportCreateRequest를 Report로 변환 - null 입력값")
    void fromReportCreateRequestToReport_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportCreateRequestToReport(null, userId))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportRevivedDetailResponse로 변환 - 정상 케이스")
    void fromReportToReportRevivedDetailResponse_Success() {
        // when
        ReportRevivedDetailResponse result =
                reportConverter.fromReportToReportRevivedDetailResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetId()).isEqualTo(report.getTargetId());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.userId()).isEqualTo(report.getUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
        assertThat(result.reason()).isEqualTo(report.getReason());
        assertThat(result.revivedAt()).isEqualTo(report.getRevivedAt());
        assertThat(result.revivedBy()).isEqualTo(report.getRevivedBy());
    }

    @Test
    @DisplayName("Report를 ReportRevivedDetailResponse로 변환 - null 입력값")
    void fromReportToReportRevivedDetailResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportRevivedDetailResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportRevivedSummaryResponse로 변환 - 정상 케이스")
    void fromReportToReportRevivedSummaryResponse_Success() {
        // when
        ReportRevivedSummaryResponse result =
                reportConverter.fromReportToReportRevivedSummaryResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
        assertThat(result.revivedAt()).isEqualTo(report.getRevivedAt());
        assertThat(result.revivedBy()).isEqualTo(report.getRevivedBy());
    }

    @Test
    @DisplayName("Report를 ReportRevivedSummaryResponse로 변환 - null 입력값")
    void fromReportToReportRevivedSummaryResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportRevivedSummaryResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportRepliedDetailResponse로 변환 - 정상 케이스")
    void fromReportToReportRepliedDetailResponse_Success() {
        // when
        ReportRepliedDetailResponse result =
                reportConverter.fromReportToReportRepliedDetailResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetId()).isEqualTo(report.getTargetId());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.userId()).isEqualTo(report.getUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
        assertThat(result.reason()).isEqualTo(report.getReason());
        assertThat(result.repliedAt()).isEqualTo(report.getRepliedAt());
        assertThat(result.repliedBy()).isEqualTo(report.getRepliedBy());
    }

    @Test
    @DisplayName("Report를 ReportRepliedDetailResponse로 변환 - null 입력값")
    void fromReportToReportRepliedDetailResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportRepliedDetailResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportRepliedSummaryResponse로 변환 - 정상 케이스")
    void fromReportToReportRepliedSummaryResponse_Success() {
        // when
        ReportRepliedSummaryResponse result =
                reportConverter.fromReportToReportRepliedSummaryResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
        assertThat(result.repliedAt()).isEqualTo(report.getRepliedAt());
        assertThat(result.repliedBy()).isEqualTo(report.getRepliedBy());
    }

    @Test
    @DisplayName("Report를 ReportRepliedSummaryResponse로 변환 - null 입력값")
    void fromReportToReportRepliedSummaryResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportRepliedSummaryResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportReportedDetailResponse로 변환 - 정상 케이스")
    void fromReportToReportReportedDetailResponse_Success() {
        // when
        ReportReportedDetailResponse result =
                reportConverter.fromReportToReportReportedDetailResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetId()).isEqualTo(report.getTargetId());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.userId()).isEqualTo(report.getUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
        assertThat(result.reason()).isEqualTo(report.getReason());
    }

    @Test
    @DisplayName("Report를 ReportReportedDetailResponse로 변환 - null 입력값")
    void fromReportToReportReportedDetailResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportReportedDetailResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportReportedSummaryResponse로 변환 - 정상 케이스")
    void fromReportToReportReportedSummaryResponse_Success() {
        // when
        ReportReportedSummaryResponse result =
                reportConverter.fromReportToReportReportedSummaryResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.Id()).isEqualTo(report.getId());
        assertThat(result.targetType()).isEqualTo(report.getTargetType().getCode());
        assertThat(result.targetUserId()).isEqualTo(report.getTargetUserId());
        assertThat(result.categoryCd()).isEqualTo(report.getCategoryCd().getCode());
    }

    @Test
    @DisplayName("Report를 ReportReportedSummaryResponse로 변환 - null 입력값")
    void fromReportToReportReportedSummaryResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportReportedSummaryResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Report를 ReportPersistResponse로 변환 - 정상 케이스")
    void fromReportToReportPersistResponse_Success() {
        // when
        ReportPersistResponse result = reportConverter.fromReportToReportPersistResponse(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(report.getId());
    }

    @Test
    @DisplayName("Report를 ReportPersistResponse로 변환 - null 입력값")
    void fromReportToReportPersistResponse_WithNullInput() {
        // when & then
        assertThatThrownBy(() -> reportConverter.fromReportToReportPersistResponse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
