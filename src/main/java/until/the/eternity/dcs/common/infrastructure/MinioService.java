package until.the.eternity.dcs.common.infrastructure;

import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.*;

import io.minio.*;
import io.minio.http.Method;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.exception.ExceptionCode;
import until.the.eternity.dcs.common.exception.FileStorageException;
import until.the.eternity.dcs.common.exception.InvalidFileNameException;
import until.the.eternity.dcs.common.exception.MissingFileUploadException;
import until.the.eternity.dcs.domain.post.application.PostMediaValidator;

@Service
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioService(
            MinioClient minioClient, @Value("${minio.bucket}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    // 버킷 존재 여부 확인 및 생성
    public void createBucketIfNotExists() throws Exception {
        boolean exists =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    // 파일 업로드
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new MissingFileUploadException();
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || !originalFilename.contains(".")
                || originalFilename.lastIndexOf('.') == originalFilename.length() - 1) {
            throw new InvalidFileNameException();
        }
        PostMediaValidator.ValidatedMedia media = PostMediaValidator.validate(file);
        String extension = "." + media.extension();

        String fileName = UUID.randomUUID() + extension;
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    inputStream, file.getSize(), -1)
                            .contentType(media.mimeType())
                            .build());

            return fileName;
        } catch (IOException e) {
            throw storageFailure("upload", FILE_UPLOAD_FAILED_EXCEPTION, e);
        } catch (Exception e) {
            throw storageFailure("upload", FILE_UPLOAD_FAILED_EXCEPTION, e);
        }
    }

    // 파일 다운로드
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw storageFailure("download", FILE_DOWNLOAD_FAILED_EXCEPTION, e);
        }
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw storageFailure("delete", FILE_DELETE_FAILED_EXCEPTION, e);
        }
    }

    // 파일 URL 가져오기
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .build());
        } catch (Exception e) {
            throw storageFailure("presign", FILE_URL_GENERATION_FAILED_EXCEPTION, e);
        }
    }

    private FileStorageException storageFailure(
            String operation, ExceptionCode code, Exception exception) {
        Throwable cause = exception.getCause();
        log.error(
                "MinIO {} failed: exception={} cause={}",
                operation,
                exception.getClass().getSimpleName(),
                cause == null ? "none" : cause.getClass().getSimpleName());
        return new FileStorageException(code, exception);
    }
}
