package until.the.eternity.dcs.domain.post.dto.request;

import java.util.List;

public record PostMediaUpdateRequest(
        String content, String contentFormat, List<Long> retainedMediaIds) {}
