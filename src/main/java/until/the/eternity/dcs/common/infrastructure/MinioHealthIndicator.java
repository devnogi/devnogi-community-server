package until.the.eternity.dcs.common.infrastructure;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("minioHealthIndicator")
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioHealthIndicator(
            MinioClient minioClient, @Value("${minio.bucket}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public Health health() {
        try {
            boolean exists =
                    minioClient.bucketExists(
                            BucketExistsArgs.builder().bucket(bucketName).build());
            return exists
                    ? Health.up().build()
                    : Health.down().withDetail("error", "bucket-not-found").build();
        } catch (Exception exception) {
            return Health.down()
                    .withDetail("error", exception.getClass().getSimpleName())
                    .build();
        }
    }
}
