package until.the.eternity.dcs.domain.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.tag.entity.Tag;

@Builder
public record TagResponse(@Schema(description = "태그명", example = "보스") String name) {
    public static TagResponse from(Tag tag) {
        return TagResponse.builder().name(tag.getName()).build();
    }
}
