package until.the.eternity.dcs.common.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class MinioHealthIndicatorTest {

    private final MinioClient minioClient = mock(MinioClient.class);
    private final MinioHealthIndicator indicator =
            new MinioHealthIndicator(minioClient, "community-media");

    @Test
    void reportsUpWhenConfiguredBucketIsAccessible() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void reportsDownWhenConfiguredBucketDoesNotExist() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "bucket-not-found");
    }

    @Test
    void reportsDownWithoutExposingExceptionMessages() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new IllegalStateException("sensitive endpoint detail"));

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "IllegalStateException");
        assertThat(health.getDetails().toString()).doesNotContain("sensitive endpoint detail");
    }
}
