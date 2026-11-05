package until.the.eternity.dcs.domain.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CommentActivityMigrationContractTest {
    @Test
    void migrationCreatesTheUserActivityIndexWithTheApprovedColumns() throws IOException {
        String sql =
                Files.readString(
                                Path.of(
                                        "src/main/resources/db/migration/"
                                                + "V20261105000000__add_comment_user_activity_index.sql"))
                        .replaceAll("\\s+", " ")
                        .trim();

        assertThat(sql)
                .contains("CREATE INDEX idx_comment_user_activity")
                .contains("ON comment (user_id, is_deleted, is_blocked, created_at, id)");
    }
}
