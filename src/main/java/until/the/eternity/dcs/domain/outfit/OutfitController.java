package until.the.eternity.dcs.domain.outfit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService;

@RestController
@RequestMapping("/api/outfits")
@RequiredArgsConstructor
public class OutfitController {
    private final OutfitService service;
    private final OutfitCatalogService catalog;
    private final ObjectMapper mapper;

    @ModelAttribute
    public void cachePolicy(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    @GetMapping
    public OutfitDtos.Page<OutfitDtos.Outfit> list(
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String items,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String motion) {
        return service.list(sort, size, page, cursor, filters(items), tags, motion, null);
    }

    @GetMapping("/mine")
    public OutfitDtos.Page<OutfitDtos.Outfit> mine(
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(required = false) String cursor) {
        return service.list("LATEST", size, 0, cursor, List.of(), List.of(), null, "mine");
    }

    @GetMapping("/bookmarks")
    public OutfitDtos.Page<OutfitDtos.Outfit> bookmarks(
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(required = false) String cursor) {
        return service.list("LATEST", size, 0, cursor, List.of(), List.of(), null, "saved");
    }

    @GetMapping("/items")
    public List<OutfitCatalogService.Item> items(
            @RequestParam(required = false) String slot,
            @RequestParam(defaultValue = "") String query) {
        return catalog.suggest(slot, query);
    }

    @GetMapping("/motions")
    public List<String> motions(@RequestParam(defaultValue = "") String query) {
        return service.motions(query);
    }

    @GetMapping("/viewer-state")
    public List<OutfitDtos.ViewerState> states(@RequestParam List<Long> ids) {
        return service.states(ids);
    }

    @GetMapping("/{id}")
    public OutfitDtos.Outfit detail(@PathVariable long id) {
        return service.detail(id);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<OutfitDtos.Outfit> create(
            @RequestPart("data") OutfitDtos.Write request,
            @RequestPart(value = "media", required = false) List<MultipartFile> media) {
        return ResponseEntity.status(201).body(service.write(null, request, media));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public OutfitDtos.Outfit update(
            @PathVariable long id,
            @RequestPart("data") OutfitDtos.Write request,
            @RequestPart(value = "media", required = false) List<MultipartFile> media) {
        return service.write(id, request, media);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/like")
    public OutfitDtos.ViewerState like(@PathVariable long id) {
        return service.reaction(id, true, true);
    }

    @DeleteMapping("/{id}/like")
    public OutfitDtos.ViewerState unlike(@PathVariable long id) {
        return service.reaction(id, true, false);
    }

    @PutMapping("/{id}/bookmark")
    public OutfitDtos.ViewerState bookmark(@PathVariable long id) {
        return service.reaction(id, false, true);
    }

    @DeleteMapping("/{id}/bookmark")
    public OutfitDtos.ViewerState unbookmark(@PathVariable long id) {
        return service.reaction(id, false, false);
    }

    @PostMapping("/{id}/shares")
    public OutfitDtos.ShareResult share(
            @PathVariable long id, @RequestBody OutfitDtos.ShareWrite request) {
        return service.share(id, request);
    }

    @GetMapping("/{id}/comments")
    public OutfitDtos.Page<OutfitDtos.Comment> comments(
            @PathVariable long id,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.comments(id, parentId, page, size);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<OutfitDtos.Comment> comment(
            @PathVariable long id, @RequestBody OutfitDtos.CommentWrite request) {
        return ResponseEntity.status(201).body(service.comment(id, null, request));
    }

    @PutMapping("/{id}/comments/{commentId}")
    public OutfitDtos.Comment updateComment(
            @PathVariable long id,
            @PathVariable long commentId,
            @RequestBody OutfitDtos.CommentWrite request) {
        return service.comment(id, commentId, request);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long id, @PathVariable long commentId) {
        service.deleteComment(id, commentId);
    }

    @GetMapping("/{id}/images/{imageId}")
    public void image(
            @PathVariable long id, @PathVariable long imageId, HttpServletResponse response) {
        String location = service.image(id, imageId);
        response.setStatus(302);
        response.setHeader("Location", location);
    }

    private List<OutfitDtos.Item> filters(String json) {
        if (json == null || json.isBlank()) return List.of();
        if (json.length() > 10000) throw OutfitValidation.invalid("검색 조건이 너무 깁니다.");
        try {
            List<OutfitDtos.Item> items = mapper.readValue(json, new TypeReference<>() {});
            if (items == null) throw OutfitValidation.invalid("잘못된 아이템 검색 조건입니다.");
            return items;
        } catch (JsonProcessingException ex) {
            throw OutfitValidation.invalid("잘못된 아이템 검색 조건입니다.");
        }
    }
}
