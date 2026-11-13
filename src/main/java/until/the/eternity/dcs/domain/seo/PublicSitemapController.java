package until.the.eternity.dcs.domain.seo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicSitemapController {
    private final PublicSitemapService service;

    @GetMapping("/api/posts/sitemap")
    public ResponseEntity<PublicSitemapPage> posts(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.posts(cursor, limit));
    }

    @GetMapping("/api/outfits/sitemap")
    public ResponseEntity<PublicSitemapPage> outfits(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.outfits(cursor, limit));
    }
}
