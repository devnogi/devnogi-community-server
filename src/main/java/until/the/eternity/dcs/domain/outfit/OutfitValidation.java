package until.the.eternity.dcs.domain.outfit;

import java.text.Normalizer;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class OutfitValidation {
    public static final Set<String> SLOTS =
            Set.of(
                    "HEAD",
                    "ACCESSORY_1",
                    "ACCESSORY_2",
                    "FACE",
                    "CLOTHING",
                    "TAIL",
                    "HANDS",
                    "FEET",
                    "WEAPON",
                    "ROBE",
                    "MOTION");

    private OutfitValidation() {}

    public static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static String text(String value, int max, boolean required) {
        String result =
                value == null ? "" : Normalizer.normalize(value.strip(), Normalizer.Form.NFC);
        if ((required && result.isBlank()) || result.codePointCount(0, result.length()) > max)
            throw invalid("입력 길이를 확인해 주세요.");
        return result;
    }

    public static String normalized(String value) {
        return text(value, 255, false).toLowerCase(Locale.ROOT);
    }

    public static List<String> tags(List<String> tags) {
        if (tags == null) return List.of();
        if (tags.size() > 10) throw invalid("태그는 최대 10개입니다.");
        Map<String, String> unique = new LinkedHashMap<>();
        for (String tag : tags) {
            String clean = text(tag, 21, true);
            if (clean.startsWith("#")) clean = clean.substring(1).strip();
            clean = text(clean, 20, true);
            unique.putIfAbsent(normalized(clean), clean);
        }
        return List.copyOf(unique.values());
    }

    public static OutfitDtos.Item item(OutfitDtos.Item item, boolean filter) {
        if (item == null
                || (!filter && (item.slot() == null || !SLOTS.contains(item.slot())))
                || (filter && item.slot() != null && !SLOTS.contains(item.slot())))
            throw invalid("잘못된 착용 부위입니다.");
        String type = item.inputType() == null ? "CATALOG" : item.inputType();
        if (!Set.of("CATALOG", "CUSTOM").contains(type)) throw invalid("잘못된 입력 유형입니다.");
        if (type.equals("CUSTOM")) {
            if (!"MOTION".equals(item.slot())) throw invalid("모션만 직접 입력할 수 있습니다.");
            return new OutfitDtos.Item(item.slot(), type, text(item.name(), 100, true), null, null);
        }
        return new OutfitDtos.Item(
                item.slot(),
                type,
                text(item.name(), 255, true),
                text(item.topCategory(), 50, true),
                text(item.subCategory(), 50, true));
    }

    public static OutfitDtos.Write write(OutfitDtos.Write input) {
        if (input == null || input.items() == null || input.items().size() > 11)
            throw invalid("착용 아이템을 입력해 주세요.");
        Set<String> seen = new HashSet<>();
        List<OutfitDtos.Item> items = new ArrayList<>();
        for (OutfitDtos.Item value : input.items()) {
            OutfitDtos.Item item = item(value, false);
            if (!seen.add(item.slot())) throw invalid("같은 부위를 중복 입력할 수 없습니다.");
            items.add(item);
        }
        if (!seen.containsAll(Set.of("CLOTHING", "HANDS", "FEET")))
            throw invalid("의상, 손(장갑), 발(신발)은 필수입니다.");
        if (input.mediaOrder() == null
                || input.mediaOrder().isEmpty()
                || input.mediaOrder().size() > 5) throw invalid("사진은 1~5장 필요합니다.");
        return new OutfitDtos.Write(
                text(input.content(), 1000, false),
                tags(input.tags()),
                List.copyOf(items),
                input.mediaOrder());
    }
}
