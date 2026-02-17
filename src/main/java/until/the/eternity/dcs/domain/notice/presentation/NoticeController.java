package until.the.eternity.dcs.domain.notice.presentation;

import static org.springframework.http.HttpStatus.CREATED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.notice.application.NoticeService;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;
    private final RedisSender redisSender;

    @PostMapping
    @Operation(
            summary = "알림 전송 API",
            description = """
			- Description : 이 API는 알림을 전송합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = NoticePersistResponse.class)))
    public ResponseEntity<NoticePersistResponse> sendNotice(
            @RequestBody NoticeSendRequest noticeSendRequest) {
        NoticePersistResponse response = noticeService.createNotice(noticeSendRequest);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PostMapping("/test")
    public ResponseEntity<RecordId> sendNoticeTest() {
        NotificationJob job = NotificationJob.of(1L, NoticeType.ANNOUNCEMENT, 1L);
        RecordId enqueue = redisSender.enqueue(job);
        return ResponseEntity.status(CREATED).body(enqueue);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "알림 단건 조회 API",
            description = """
			- Description : 이 API는 특정 ID의 알림을 조회합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = NoticeCommonResponse.class)))
    public NoticeCommonResponse getDetailNotice(@PathVariable Long id) {
        return noticeService.getDetailNotice(id);
    }

    @GetMapping
    @Operation(
            summary = "알림 리스트 조회 API",
            description =
                    """
			- Description : 이 API는 최근 n일 간의 알림을 조회합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = NoticeCommonResponse.class)))
    public List<NoticeCommonResponse> getNoticeList(
            @RequestParam("userId") Long userId, @RequestParam("day") Integer day) {
        return noticeService.getNoticeList(userId, day);
    }
}
