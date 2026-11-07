package until.the.eternity.dcs.domain.guild;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guilds")
public class GuildContentController {
    final GuildContent content;
    final GuildMedia media;
    final GuildApplications applications;

    @ModelAttribute
    void noCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-store");
    }

    @GetMapping
    List<Map<String, Object>> list(
            @RequestParam(defaultValue = "") String server,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "false") boolean recruiting,
            @RequestParam(defaultValue = "0") int page) {
        return content.list(server, name, recruiting, page);
    }

    @GetMapping("/{g}")
    Map<String, Object> home(@PathVariable int g) {
        return content.home(g);
    }

    @PutMapping("/{g}/profile")
    void profile(@PathVariable int g, @RequestBody GuildContent.Profile profile) {
        content.profile(g, profile);
    }

    @GetMapping("/{g}/media/{kind}")
    void image(@PathVariable int g, @PathVariable String kind, HttpServletResponse response)
            throws IOException {
        writeImage(media.image(g, null, kind), response);
    }

    @PutMapping("/{g}/media/{kind}")
    void image(@PathVariable int g, @PathVariable String kind, @RequestParam MultipartFile file) {
        media.upload(g, null, kind, file);
    }

    @GetMapping("/{g}/posts/{id}/photo")
    void photo(@PathVariable int g, @PathVariable long id, HttpServletResponse response)
            throws IOException {
        writeImage(media.image(g, id, null), response);
    }

    @PutMapping("/{g}/posts/{id}/photo")
    void photo(@PathVariable int g, @PathVariable long id, @RequestParam MultipartFile file) {
        media.upload(g, id, null, file);
    }

    @GetMapping("/{g}/{section:posts|recruitments}")
    List<Map<String, Object>> writings(
            @PathVariable int g,
            @PathVariable String section,
            @RequestParam(defaultValue = "0") int page) {
        return content.writings(g, section.equals("posts"), page);
    }

    @GetMapping("/{g}/{section:posts|recruitments}/{id}")
    Map<String, Object> writing(
            @PathVariable int g, @PathVariable String section, @PathVariable long id) {
        return content.writing(g, id, section.equals("posts"));
    }

    @PostMapping("/{g}/{section:posts|recruitments}")
    Map<String, Long> create(
            @PathVariable int g,
            @PathVariable String section,
            @RequestBody GuildContent.Writing body) {
        return content.save(g, null, section.equals("posts"), body);
    }

    @PutMapping("/{g}/{section:posts|recruitments}/{id}")
    Map<String, Long> edit(
            @PathVariable int g,
            @PathVariable String section,
            @PathVariable long id,
            @RequestBody GuildContent.Writing body) {
        return content.save(g, id, section.equals("posts"), body);
    }

    @DeleteMapping("/{g}/{section:posts|recruitments}/{id}")
    void delete(@PathVariable int g, @PathVariable String section, @PathVariable long id) {
        content.remove(g, id, section.equals("posts"), true);
    }

    @PostMapping("/{g}/recruitments/{id}/close")
    void close(@PathVariable int g, @PathVariable long id) {
        content.remove(g, id, false, false);
    }

    private static void writeImage(ResponseEntity<byte[]> image, HttpServletResponse response)
            throws IOException {
        image.getHeaders()
                .forEach((key, values) -> values.forEach(value -> response.setHeader(key, value)));
        response.getOutputStream().write(Objects.requireNonNull(image.getBody()));
    }

    public record ApplicationRequest(String message) {}

    @PostMapping("/{g}/recruitments/{id}/applications")
    Map<String, Long> apply(
            @PathVariable int g,
            @PathVariable long id,
            @RequestBody(required = false) ApplicationRequest body) {
        return applications.apply(g, id, body == null ? null : body.message());
    }

    @GetMapping("/{g}/recruitments/{id}/applications")
    List<Map<String, Object>> inbox(@PathVariable int g, @PathVariable long id) {
        return applications.inbox(g, id);
    }

    @GetMapping("/my/applications")
    List<Map<String, Object>> mine() {
        return applications.mine();
    }

    @PostMapping("/{g}/recruitments/{id}/applications/{app}/{action:accept|reject|cancel}")
    void decide(
            @PathVariable int g,
            @PathVariable long id,
            @PathVariable long app,
            @PathVariable String action) {
        applications.decide(g, id, app, action);
    }
}
