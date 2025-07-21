package until.the.eternity.dcs.domain.report.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.report.entitiy.Report;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);
}
