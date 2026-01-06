package until.the.eternity.dcs.common.infrastructure;

import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.*;

import io.minio.*;
import io.minio.http.Method;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.exception.FileStorageException;
import until.the.eternity.dcs.common.exception.InvalidExtensionException;
import until.the.eternity.dcs.common.exception.InvalidFileNameException;
import until.the.eternity.dcs.common.exception.MissingFileUploadException;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

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
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new InvalidExtensionException();
        }

        String fileName = UUID.randomUUID() + extension;
        try {
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return fileName;
        } catch (IOException e) {
            throw new FileStorageException(FILE_UPLOAD_FAILED_EXCEPTION);
        } catch (Exception e) {
            throw new FileStorageException(FILE_READ_FAILED_EXCEPTION);
        }
    }

    // 파일 다운로드
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new FileStorageException(FILE_DOWNLOAD_FAILED_EXCEPTION);
        }
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new FileStorageException(FILE_DELETE_FAILED_EXCEPTION);
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
            throw new FileStorageException(FILE_URL_GENERATION_FAILED_EXCEPTION);
        }
    }
}
