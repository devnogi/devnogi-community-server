package until.the.eternity.dcs.domain.announcement.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnnouncementTest {

    @Test
    @DisplayName("toggleIsGlobal은 현재 isGlobal 상태를 토글합니다.")
    void toggleIsGlobal() {
        // given
        Announcement announcement = Announcement.builder().isGlobal(true).build();

        // when
        announcement.toggleIsGlobal();
        // then
        assertThat(announcement.getIsGlobal()).isFalse();

        // when
        announcement.toggleIsGlobal();
        // then
        assertThat(announcement.getIsGlobal()).isTrue();
    }
}
