package until.the.eternity.dcs.domain.notice.presentation;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.notice.application.NoticeService;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<NoticePersistResponse> sendNotice(
            @RequestBody NoticeSendRequest noticeSendRequest) {
        NoticePersistResponse response = noticeService.createNotice(noticeSendRequest);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping("/{id}")
    public NoticeCommonResponse getDetailNotice(@PathVariable Long id) {
        return noticeService.getDetailNotice(id);
    }

    @GetMapping
    public List<NoticeCommonResponse> getNoticeList(@RequestParam("day") Integer day) {
        return noticeService.getNoticeList(day);
    }
}
// todo 스웨거
