package until.the.eternity.dcs.domain.post.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LegacyPlainTextConverterTest {
    @Test
    void preservesLinkLabelAndUrlWhileRemovingMarkdownMarkers() {
        String result =
                LegacyPlainTextConverter.convert(
                        "제목", "## **강조**\n- [공식 사이트](https://example.com)\n`code`");

        assertThat(result).isEqualTo("제목\n\n강조\n공식 사이트 (https://example.com)\ncode");
    }

    @Test
    void doesNotTruncateLongLegacyContent() {
        String body = "가".repeat(1200);
        assertThat(LegacyPlainTextConverter.convert(null, body)).hasSize(1200);
    }
}
