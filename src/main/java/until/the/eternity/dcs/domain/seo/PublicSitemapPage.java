package until.the.eternity.dcs.domain.seo;

import java.util.List;

public record PublicSitemapPage(List<Entry> items, String nextCursor, boolean hasNext) {
    public record Entry(long id, String updatedAt) {}

    static PublicSitemapPage from(List<Entry> rows, int limit) {
        boolean hasNext = rows.size() > limit;
        List<Entry> items = List.copyOf(rows.subList(0, Math.min(limit, rows.size())));
        return new PublicSitemapPage(
                items, hasNext ? Long.toString(items.getLast().id()) : null, hasNext);
    }
}
