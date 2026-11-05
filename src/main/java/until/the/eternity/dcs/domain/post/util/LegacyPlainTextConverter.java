package until.the.eternity.dcs.domain.post.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyPlainTextConverter {
    private static final Pattern MARKDOWN_LINK =
            Pattern.compile("!?\\[([^]]*)]\\((https?://[^)]+)\\)");

    private LegacyPlainTextConverter() {}

    public static String convert(String title, String markdown) {
        String body = markdown == null ? "" : markdown;
        Matcher matcher = MARKDOWN_LINK.matcher(body);
        StringBuffer converted = new StringBuffer();
        while (matcher.find()) {
            String label =
                    matcher.group(1).isBlank()
                            ? matcher.group(2)
                            : matcher.group(1) + " (" + matcher.group(2) + ")";
            matcher.appendReplacement(converted, Matcher.quoteReplacement(label));
        }
        matcher.appendTail(converted);
        body = converted.toString();
        body = body.replaceAll("(?m)^\\s{0,3}(#{1,6}|>|[-+*]|\\d+[.)])\\s+", "");
        body = body.replace("```", "").replace("`", "");
        body = body.replaceAll("(\\*\\*|__|~~|\\*|_)", "");
        String normalizedTitle = title == null ? "" : title.strip();
        String normalizedBody = body.strip();
        if (normalizedTitle.isEmpty()) return normalizedBody;
        if (normalizedBody.isEmpty()) return normalizedTitle;
        return normalizedTitle + "\n\n" + normalizedBody;
    }
}
