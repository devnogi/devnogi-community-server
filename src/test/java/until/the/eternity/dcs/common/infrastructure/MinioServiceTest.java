package until.the.eternity.dcs.common.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.FILE_UPLOAD_FAILED_EXCEPTION;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import until.the.eternity.dcs.common.exception.FileStorageException;

class MinioServiceTest {

    private final MinioClient minioClient = mock(MinioClient.class);
    private final MinioService service = new MinioService(minioClient, "community-media");

    @Test
    void preservesUploadIoFailureAsCause() throws Exception {
        IOException failure = new IOException("connection reset");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.uploadFile(jpeg()))
                .isInstanceOfSatisfying(
                        FileStorageException.class,
                        exception -> {
                            assertThat(exception.getCode()).isEqualTo(FILE_UPLOAD_FAILED_EXCEPTION);
                            assertThat(exception.getCause()).isSameAs(failure);
                        });
    }

    @Test
    void preservesNonIoStorageFailureAsCause() throws Exception {
        IllegalStateException failure = new IllegalStateException("access denied");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.uploadFile(jpeg()))
                .isInstanceOfSatisfying(
                        FileStorageException.class,
                        exception -> {
                            assertThat(exception.getCode()).isEqualTo(FILE_UPLOAD_FAILED_EXCEPTION);
                            assertThat(exception.getCause()).isSameAs(failure);
                        });
    }

    private MockMultipartFile jpeg() {
        return new MockMultipartFile(
                "media",
                "look.jpg",
                "image/jpeg",
                new byte[] {
                    (byte) 0xff, (byte) 0xd8, (byte) 0xff, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
                });
    }
}
