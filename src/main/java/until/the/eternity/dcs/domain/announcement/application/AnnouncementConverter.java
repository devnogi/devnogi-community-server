package until.the.eternity.dcs.domain.announcement.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.entity.Post;

@Component
public class AnnouncementConverter {
    public Announcement fromCreateRequestAndPost(
            AnnouncementCreateRequest request, Post post, PostMetaResponse postMeta) {
        return Announcement.builder()
                .postId(post.getId())
                .board(post.getBoard())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .isDraft(post.getIsDraft())
                .viewCount(postMeta.viewCount())
                .likeCount(postMeta.likeCount())
                .commentCount(postMeta.commentCount())
                .isGlobal(request.isGlobal())
                .build();
    }

    public AnnouncementPersistResponse fromEntityToPersistResponse(Announcement announcement) {
        return AnnouncementPersistResponse.from(announcement);
    }

    public AnnouncementToggleResponse fromEntityToToggleResponse(Announcement announcement) {
        return AnnouncementToggleResponse.from(announcement);
    }

    public AnnouncementPageResponseItem fromEntityToPageResponse(Announcement announcement) {
        return AnnouncementPageResponseItem.from(announcement);
    }
}
