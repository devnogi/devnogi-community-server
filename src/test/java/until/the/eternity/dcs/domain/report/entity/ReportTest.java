package until.the.eternity.dcs.domain.report.entity;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;

public class ReportTest {
    Report report;

    @BeforeEach
    void setUp() {
        report =
                Report.builder()
                        .id(1L)
                        .statusCd(ReportStatus.REPORTED)
                        .repliedAt(null)
                        .repliedBy(null)
                        .revivedAt(null)
                        .revivedBy(null)
                        .build();
    }

    @Test
    @DisplayName("report replied test")
    void update_Replied_Success() {

        // given
        ReportStatus status = ReportStatus.ACCEPT;
        LocalDateTime repliedTime = LocalDateTime.now();
        Long userId = 1L;

        // when
        report.update(status, repliedTime, userId, null, null);

        // then
        Assertions.assertEquals(report.getStatusCd(), status);
        Assertions.assertEquals(report.getRepliedAt(), repliedTime);
        Assertions.assertEquals(report.getRepliedBy(), userId);
        Assertions.assertNull(report.getRevivedAt());
        Assertions.assertNull(report.getRevivedBy());
    }

    @Test
    @DisplayName("report revived test")
    void update_Revived_Success() {

        // given
        ReportStatus status = ReportStatus.REJECT;
        LocalDateTime revivedTime = LocalDateTime.now();
        Long userId = 1L;

        // when
        report.update(status, null, null, revivedTime, userId);

        // then
        Assertions.assertEquals(report.getStatusCd(), status);
        Assertions.assertEquals(report.getRevivedAt(), revivedTime);
        Assertions.assertEquals(report.getRevivedBy(), userId);
        Assertions.assertNull(report.getRepliedAt());
        Assertions.assertNull(report.getRepliedBy());
    }
}
