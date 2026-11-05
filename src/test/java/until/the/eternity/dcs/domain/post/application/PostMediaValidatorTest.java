package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import until.the.eternity.dcs.domain.post.enums.PostMediaType;
import until.the.eternity.dcs.domain.post.exception.PostContractException;

class PostMediaValidatorTest {

    @Test
    void validatesPngByExtensionMimeAndMagicBytes() {
        byte[] bytes = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
        MockMultipartFile file = new MockMultipartFile("media", "image.png", "image/png", bytes);

        PostMediaValidator.ValidatedMedia result = PostMediaValidator.validate(file);

        assertThat(result.type()).isEqualTo(PostMediaType.IMAGE);
        assertThat(result.mimeType()).isEqualTo("image/png");
    }

    @Test
    void rejectsMimeSpoofing() {
        byte[] bytes = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0};
        MockMultipartFile file = new MockMultipartFile("media", "image.png", "image/png", bytes);

        assertThatThrownBy(() -> PostMediaValidator.validate(file))
                .isInstanceOf(PostContractException.class);
    }

    @Test
    void acceptsExactlyFiveMiBAndRejectsOneByteMore() {
        byte[] exact = new byte[(int) PostMediaValidator.MAX_FILE_SIZE];
        exact[0] = (byte) 0xff;
        exact[1] = (byte) 0xd8;
        exact[2] = (byte) 0xff;
        MockMultipartFile accepted =
                new MockMultipartFile("media", "image.jpg", "image/jpeg", exact);
        assertThat(PostMediaValidator.validate(accepted).type()).isEqualTo(PostMediaType.IMAGE);

        byte[] oversized = new byte[exact.length + 1];
        oversized[0] = (byte) 0xff;
        oversized[1] = (byte) 0xd8;
        oversized[2] = (byte) 0xff;
        MockMultipartFile rejected =
                new MockMultipartFile("media", "image.jpg", "image/jpeg", oversized);
        assertThatThrownBy(() -> PostMediaValidator.validate(rejected))
                .isInstanceOf(PostContractException.class);
    }
}
