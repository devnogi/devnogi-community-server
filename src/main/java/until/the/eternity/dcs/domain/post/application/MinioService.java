package until.the.eternity.dcs.domain.post.application;

import io.minio.*;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    public String uploadFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + extension;

            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 파일 다운로드
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패", e);
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
            throw new RuntimeException("URL 생성 실패", e);
        }
    }
}
