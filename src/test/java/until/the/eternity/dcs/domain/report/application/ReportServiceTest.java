package until.the.eternity.dcs.domain.report.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.report.dto.request.ReportCreateRequest;
import until.the.eternity.dcs.domain.report.dto.request.ReportUpdateRequest;
import until.the.eternity.dcs.domain.report.dto.response.*;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;
import until.the.eternity.dcs.domain.report.exception.ReportNotFoundException;
import until.the.eternity.dcs.domain.report.exception.StatusNotFoundException;
import until.the.eternity.dcs.domain.report.infrastructure.ReportRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 단위 테스트")
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;

    @Mock private ReportConverter reportConverter;

    @Mock private RedisSender redisSender;

    @Mock private UserSummaryService userSummaryService;

    @Mock private ReportPermissionEvaluator reportPermissionEvaluator;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @InjectMocks private ReportService reportService;

    private Report mockReport;
    private UserSummary mockUser;
    private UserSummary mockAdmin;
    Long userId = 1L;
    Long adminId = 2L;
    String username = "username";
    String unknownUsername = "알수없음";

    @BeforeEach
    void setUp() {
        mockReport = Report.builder().id(1L).targetUserId(userId).build();
        mockUser =
                UserSummary.builder().id(userId).nickname(username).grade(UserGrade.USER).build();
        mockAdmin = UserSummary.builder().id(adminId).grade(UserGrade.ADMIN).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("신고 생성 테스트")
    class CreateReportTest {

        @Test
        @DisplayName("신고 생성 성공")
        void createReport_Success() {
            // given
            ReportCreateRequest request = mock(ReportCreateRequest.class);
            Report newReport = mock(Report.class);
            Report savedReport = mock(Report.class);
            ReportPersistResponse expectedResponse = mock(ReportPersistResponse.class);

            given(reportConverter.fromReportCreateRequestToReport(request, adminId))
                    .willReturn(newReport);
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(reportPermissionEvaluator.getCurrentUserId(authentication)).willReturn(adminId);
            given(reportRepository.save(newReport)).willReturn(savedReport);
            given(reportConverter.fromReportToReportPersistResponse(savedReport))
                    .willReturn(expectedResponse);

            // when
            ReportPersistResponse result = reportService.createReport(request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(reportConverter).fromReportCreateRequestToReport(request, adminId);
            verify(reportRepository).save(newReport);
            verify(reportConverter).fromReportToReportPersistResponse(savedReport);
        }
    }

    @Nested
    @DisplayName("신고 조회 테스트")
    class GetReportTest {

        @Test
        @DisplayName("답변된 신고 상세 조회 성공")
        void getRepliedReport_Success() {
            // given
            Long reportId = 1L;
            ReportRepliedDetailResponse expectedResponse = mock(ReportRepliedDetailResponse.class);

            given(userSummaryService.findUserSummary(anyLong()))
                    .willReturn(UserSummaryDetailResponse.from(mockUser));
            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));
            given(reportConverter.fromReportToReportRepliedDetailResponse(mockReport, username))
                    .willReturn(expectedResponse);

            // when
            ReportRepliedDetailResponse result = reportService.getRepliedReport(reportId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(reportRepository).findById(reportId);
            verify(reportConverter).fromReportToReportRepliedDetailResponse(mockReport, username);
        }

        @Test
        @DisplayName("존재하지 않는 신고 조회 시 예외 발생")
        void getRepliedReport_NotFound() {
            // given
            Long reportId = 999L;
            given(reportRepository.findById(reportId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportService.getRepliedReport(reportId))
                    .isInstanceOf(ReportNotFoundException.class);
        }

        @Test
        @DisplayName("복구된 신고 상세 조회 성공")
        void getRevivedReport_Success() {
            // given
            Long reportId = 1L;
            ReportRevivedDetailResponse expectedResponse = mock(ReportRevivedDetailResponse.class);

            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));
            given(
                            reportConverter.fromReportToReportRevivedDetailResponse(
                                    mockReport, mockUser.getNickname()))
                    .willReturn(expectedResponse);
            given(userSummaryService.findUserSummary(any()))
                    .willReturn(UserSummaryDetailResponse.from(mockUser));

            // when
            ReportRevivedDetailResponse result = reportService.getRevivedReport(reportId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("신고된 게시물 상세 조회 성공")
        void getReportedReport_Success() {
            // given
            Long reportId = 1L;
            ReportReportedDetailResponse expectedResponse =
                    mock(ReportReportedDetailResponse.class);

            given(userSummaryService.findUserSummary(anyLong()))
                    .willReturn(UserSummaryDetailResponse.from(mockUser));
            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));
            given(reportConverter.fromReportToReportReportedDetailResponse(mockReport, username))
                    .willReturn(expectedResponse);

            // when
            ReportReportedDetailResponse result = reportService.getReportedReport(reportId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("신고 목록 조회 테스트")
    class FindReportsTest {

        @Test
        @DisplayName("답변된 신고 목록 조회 성공")
        void findRepliedReports_Success() {
            // given
            CustomPageRequest request = createCustomPageRequest();
            Pageable pageable = request.toPageable();
            Page<Report> reportPage = new PageImpl<>(List.of(mockReport));
            ReportRepliedSummaryResponse summaryResponse = mock(ReportRepliedSummaryResponse.class);

            given(reportRepository.findAllByStatusCd(ReportStatus.ACCEPT, pageable))
                    .willReturn(reportPage);
            given(
                            reportConverter.fromReportToReportRepliedSummaryResponse(
                                    mockReport, unknownUsername))
                    .willReturn(summaryResponse);

            // when
            Page<ReportRepliedSummaryResponse> result = reportService.findRepliedReports(request);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(summaryResponse);
        }

        @Test
        @DisplayName("복구된 신고 목록 조회 성공")
        void findRevivedReports_Success() {
            // given
            CustomPageRequest request = createCustomPageRequest();
            Pageable pageable = request.toPageable();
            Page<Report> reportPage = new PageImpl<>(List.of(mockReport));
            ReportRevivedSummaryResponse summaryResponse = mock(ReportRevivedSummaryResponse.class);

            given(reportRepository.findAllByStatusCd(ReportStatus.REJECT, pageable))
                    .willReturn(reportPage);
            given(
                            reportConverter.fromReportToReportRevivedSummaryResponse(
                                    mockReport, unknownUsername))
                    .willReturn(summaryResponse);

            // when
            Page<ReportRevivedSummaryResponse> result = reportService.findRevivedReports(request);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(summaryResponse);
        }

        @Test
        @DisplayName("신고된 게시물 목록 조회 성공")
        void findReportedReports_Success() {
            // given
            CustomPageRequest request = createCustomPageRequest();
            Pageable pageable = request.toPageable();
            Page<Report> reportPage = new PageImpl<>(List.of(mockReport));
            ReportReportedSummaryResponse summaryResponse =
                    mock(ReportReportedSummaryResponse.class);

            given(reportRepository.findAllByStatusCd(ReportStatus.REPORTED, pageable))
                    .willReturn(reportPage);
            given(
                            reportConverter.fromReportToReportReportedSummaryResponse(
                                    mockReport, unknownUsername))
                    .willReturn(summaryResponse);

            // when
            Page<ReportReportedSummaryResponse> result = reportService.findReportedReports(request);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(summaryResponse);
        }
    }

    @Nested
    @DisplayName("신고 상태 업데이트 테스트")
    class UpdateReportTest {

        @Test
        @DisplayName("관리자가 신고를 승인으로 업데이트 성공")
        void updatePost_AcceptStatus_Success() {
            // given
            Long reportId = 1L;
            String acceptStatusCode = "ACCEPT";
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(reportPermissionEvaluator.getCurrentUserId(authentication)).willReturn(adminId);
            ReportUpdateRequest request = new ReportUpdateRequest(acceptStatusCode);
            ReportPersistResponse expectedResponse = mock(ReportPersistResponse.class);

            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));
            given(reportConverter.fromReportToReportPersistResponse(mockReport))
                    .willReturn(expectedResponse);

            // when
            ReportPersistResponse result = reportService.updateReport(reportId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(mockReport.getStatusCd()).isEqualTo(ReportStatus.ACCEPT);
            assertThat(mockReport.getRepliedBy()).isEqualTo(mockAdmin.getId());
        }

        @Test
        @DisplayName("관리자가 신고를 거부로 업데이트 성공")
        void updatePost_RejectStatus_Success() {
            // given
            Long reportId = 1L;
            String rejectStatusCode = "REJECT";
            ReportUpdateRequest request = new ReportUpdateRequest(rejectStatusCode);
            ReportPersistResponse expectedResponse = mock(ReportPersistResponse.class);
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(reportPermissionEvaluator.getCurrentUserId(authentication)).willReturn(adminId);

            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));
            given(reportConverter.fromReportToReportPersistResponse(mockReport))
                    .willReturn(expectedResponse);

            // when
            ReportPersistResponse result = reportService.updateReport(reportId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(mockReport.getStatusCd()).isEqualTo(ReportStatus.REJECT);
            assertThat(mockReport.getRevivedBy()).isEqualTo(mockAdmin.getId());
        }

        @Test
        @DisplayName("유효하지 않은 상태 코드로 업데이트 시 예외 발생")
        void updatePost_InvalidStatusCode() {
            // given
            Long reportId = 1L;
            String invalidStatusCode = "INVALID";
            ReportUpdateRequest request = new ReportUpdateRequest(invalidStatusCode);
            SecurityContextHolder.setContext(securityContext);
            given(reportPermissionEvaluator.getCurrentUserId(authentication)).willReturn(adminId);

            // when & then
            assertThatThrownBy(() -> reportService.updateReport(reportId, request))
                    .isInstanceOf(StatusNotFoundException.class);
            assertThat(authentication).isNotNull();
            assertThat(reportPermissionEvaluator.getCurrentUserId(authentication))
                    .isEqualTo(adminId);
        }

        @Test
        @DisplayName("존재하지 않는 신고 업데이트 시 예외 발생")
        void updatePost_ReportNotFound() {
            // given
            Long reportId = 999L;
            ReportUpdateRequest request = new ReportUpdateRequest("ACCEPT");
            SecurityContextHolder.setContext(securityContext);

            given(reportRepository.findById(reportId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportService.updateReport(reportId, request))
                    .isInstanceOf(ReportNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("신고 삭제 테스트")
    class DeleteReportTest {

        @Test
        @DisplayName("관리자가 신고 삭제 성공")
        void deleteReport_Success() {
            // given
            Long reportId = 1L;
            SecurityContextHolder.setContext(securityContext);
            given(reportRepository.findById(reportId)).willReturn(Optional.of(mockReport));

            // when
            reportService.deleteReport(reportId);

            // then
            verify(reportRepository).findById(reportId);
            verify(reportRepository).deleteById(reportId);
        }

        @Test
        @DisplayName("존재하지 않는 신고 삭제 시 예외 발생")
        void deleteReport_ReportNotFound() {
            // given
            Long reportId = 999L;
            SecurityContextHolder.setContext(securityContext);
            given(reportRepository.findById(reportId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportService.deleteReport(reportId))
                    .isInstanceOf(ReportNotFoundException.class);

            verify(reportRepository, never()).deleteById(any());
        }
    }

    // 테스트 헬퍼 메서드들
    private CustomPageRequest createCustomPageRequest() {
        CustomPageRequest request = mock(CustomPageRequest.class);
        given(request.toPageable()).willReturn(mock(Pageable.class));
        return request;
    }
}
