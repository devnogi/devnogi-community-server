package until.the.eternity.dcs.domain.post.application;

import static until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.domain.post.enums.PostMediaType;
import until.the.eternity.dcs.domain.post.exception.PostContractException;

public final class PostMediaValidator {
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Map<String, String> MIME_BY_EXTENSION =
            Map.of(
                    "jpg", "image/jpeg",
                    "jpeg", "image/jpeg",
                    "png", "image/png",
                    "webp", "image/webp",
                    "gif", "image/gif",
                    "mp4", "video/mp4",
                    "webm", "video/webm");

    private PostMediaValidator() {}

    public static ValidatedMedia validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PostContractException(INVALID_MEDIA);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PostContractException(MEDIA_TOO_LARGE);
        }
        String filename = file.getOriginalFilename();
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new PostContractException(INVALID_MEDIA);
        }
        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        String expectedMime = MIME_BY_EXTENSION.get(extension);
        if (expectedMime == null || !expectedMime.equalsIgnoreCase(file.getContentType())) {
            throw new PostContractException(INVALID_MEDIA);
        }
        byte[] header = new byte[16];
        int read;
        try (InputStream input = file.getInputStream()) {
            read = input.read(header);
        } catch (IOException e) {
            throw new PostContractException(INVALID_MEDIA);
        }
        if (!matchesMagic(extension, header, read)) {
            throw new PostContractException(INVALID_MEDIA);
        }
        PostMediaType type =
                expectedMime.startsWith("video/") ? PostMediaType.VIDEO : PostMediaType.IMAGE;
        return new ValidatedMedia(type, expectedMime, extension);
    }

    private static boolean matchesMagic(String extension, byte[] h, int length) {
        return switch (extension) {
            case "jpg", "jpeg" ->
                    length >= 3 && u(h[0]) == 0xff && u(h[1]) == 0xd8 && u(h[2]) == 0xff;
            case "png" ->
                    length >= 8 && u(h[0]) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
            case "gif" -> length >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8';
            case "webp" -> length >= 12 && text(h, 0, "RIFF") && text(h, 8, "WEBP");
            case "mp4" -> length >= 12 && text(h, 4, "ftyp");
            case "webm" ->
                    length >= 4
                            && u(h[0]) == 0x1a
                            && u(h[1]) == 0x45
                            && u(h[2]) == 0xdf
                            && u(h[3]) == 0xa3;
            default -> false;
        };
    }

    private static boolean text(byte[] bytes, int offset, String value) {
        for (int i = 0; i < value.length(); i++) {
            if (bytes[offset + i] != value.charAt(i)) return false;
        }
        return true;
    }

    private static int u(byte value) {
        return value & 0xff;
    }

    public record ValidatedMedia(PostMediaType type, String mimeType, String extension) {}
}
