package until.the.eternity.dcs.domain.post.application;

import io.minio.*;
import io.minio.http.Method;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
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
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        return fileName;
    }

    // 파일 다운로드
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
    }

    // 파일 삭제
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
    }

    // 파일 URL 가져오기
    public String getPresignedUrl(String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .method(Method.GET)
                        .build());
    }
}
