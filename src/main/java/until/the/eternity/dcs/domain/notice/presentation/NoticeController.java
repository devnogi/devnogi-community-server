package until.the.eternity.dcs.domain.notice.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.notice.application.NoticeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    // 알림 전송 API

    // 알림 확인 API

}
