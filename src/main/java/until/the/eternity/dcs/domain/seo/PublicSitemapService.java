package until.the.eternity.dcs.domain.seo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicSitemapService {
    private final PublicSitemapRepository repository;

    public PublicSitemapPage posts(String cursor, String limit) {
        long afterId = cursor(cursor);
        int pageSize = limit(limit);
        return PublicSitemapPage.from(repository.posts(afterId, pageSize + 1), pageSize);
    }

    public PublicSitemapPage outfits(String cursor, String limit) {
        long afterId = cursor(cursor);
        int pageSize = limit(limit);
        return PublicSitemapPage.from(repository.outfits(afterId, pageSize + 1), pageSize);
    }

    private long cursor(String value) {
        if (value == null) return 0;
        if (!value.matches("[1-9][0-9]{0,18}")) throw new InvalidPageRequestException();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new InvalidPageRequestException();
        }
    }

    private int limit(String value) {
        if (value == null) return 1000;
        if (!value.matches("[1-9][0-9]{0,3}")) throw new InvalidPageRequestException();
        int limit = Integer.parseInt(value);
        if (limit > 1000) throw new InvalidPageRequestException();
        return limit;
    }
}
