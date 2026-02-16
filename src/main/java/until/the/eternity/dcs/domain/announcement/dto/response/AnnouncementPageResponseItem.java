package until.the.eternity.dcs.domain.announcement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record AnnouncementPageResponseItem(
        @Schema(description = "게시글 ID", example = "1") Long postId,
        @Schema(description = "게시글 제목", example = "게시글 제목입니다.") String title,
        @Schema(description = "공지글 전체공개 여부", example = "true", requiredMode = REQUIRED)
                Boolean isGlobal) {

    public static AnnouncementPageResponseItem from(Announcement announcement) {
        return AnnouncementPageResponseItem.builder()
                .postId(announcement.getPostId())
                .title(announcement.getTitle())
                .isGlobal(announcement.getIsGlobal())
                .build();
    }
}
