package until.the.eternity.dcs.domain.outfit.catalog;

import static until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogException.Code.*;

import jakarta.annotation.PreDestroy;
import java.net.http.HttpClient;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Reads the Batch-owned Redis-backed catalog through its public Gateway contract. */
@Service
@Slf4j
public class OutfitCatalogService {
    private static final Map<String, List<Category>> CATEGORIES = categories();
    private final RestClient client;
    private final Duration deadline;
    private final boolean warmupEnabled;
    private final AtomicBoolean warming = new AtomicBoolean();
    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    3,
                    3,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(64),
                    Thread.ofPlatform().daemon().name("outfit-catalog-", 0).factory());

    public record Item(String name, String topCategory, String subCategory) {}

    public record Selection(String slot, Item item) {}

    private record Category(String top, String sub) {}

    private record CatalogResponse(Boolean success, List<Item> data) {}

    @Autowired
    public OutfitCatalogService(
            RestClient.Builder builder,
            @Value("${outfit.catalog.base-url:http://localhost:8090/oab}") String baseUrl,
            @Value("${outfit.catalog.warmup-enabled:true}") boolean warmupEnabled) {
        this(createClient(builder, baseUrl), Duration.ofSeconds(8), warmupEnabled);
    }

    OutfitCatalogService(RestClient client, Duration deadline, boolean warmupEnabled) {
        this.client = client;
        this.deadline = deadline;
        this.warmupEnabled = warmupEnabled;
    }

    public List<Item> suggest(String slot, String query) {
        String normalized = normalize(query);
        if (normalized.length() > 255) {
            throw new OutfitCatalogException(OUTFIT_INVALID_ITEM);
        }
        List<Category> filters =
                slot == null || slot.isBlank()
                        ? CATEGORIES.values().stream().flatMap(List::stream).distinct().toList()
                        : categoriesFor(slot);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<Callable<List<Item>>> calls =
                filters.stream()
                        .<Callable<List<Item>>>map(filter -> () -> load(filter, null))
                        .toList();
        // Limit after filtering: the upstream contract returns a full category, not a top-N search.
        return invoke(calls).stream()
                .flatMap(List::stream)
                .filter(item -> allowed(filters, item))
                .filter(item -> normalize(item.name()).contains(normalized))
                .distinct()
                .sorted(
                        Comparator.<Item, Boolean>comparing(
                                        item -> !normalize(item.name()).startsWith(normalized))
                                .thenComparing(Item::name)
                                .thenComparing(Item::topCategory)
                                .thenComparing(Item::subCategory))
                .limit(20)
                .toList();
    }

    public void validate(String slot, Item item) {
        validateItems(List.of(new Selection(slot, item)));
    }

    /** Caller supplies only new/changed catalog selections, never custom motion strings. */
    public void validateItems(List<Selection> selections) {
        if (selections == null || selections.size() > 11) {
            throw new OutfitCatalogException(OUTFIT_INVALID_ITEM);
        }
        for (Selection selection : selections) {
            if (selection == null || !allowed(categoriesFor(selection.slot()), selection.item())) {
                throw new OutfitCatalogException(OUTFIT_INVALID_ITEM);
            }
        }
        List<Callable<Boolean>> calls =
                selections.stream()
                        .map(Selection::item)
                        .distinct()
                        .<Callable<Boolean>>map(
                                item ->
                                        () -> {
                                            List<Item> found =
                                                    load(
                                                            new Category(
                                                                    item.topCategory(),
                                                                    item.subCategory()),
                                                            item.name());
                                            if (!found.contains(item)) {
                                                throw new OutfitCatalogException(
                                                        OUTFIT_INVALID_ITEM);
                                            }
                                            return true;
                                        })
                        .toList();
        invoke(calls);
    }

    private static List<Category> categoriesFor(String slot) {
        List<Category> filters = CATEGORIES.get(slot);
        if (filters == null) {
            throw new OutfitCatalogException(OUTFIT_INVALID_ITEM);
        }
        return filters;
    }

    private static boolean allowed(List<Category> filters, Item item) {
        if (item == null
                || item.name() == null
                || item.name().isBlank()
                || item.name().length() > 255
                || item.topCategory() == null
                || item.subCategory() == null
                || "원거리 소모품".equals(item.subCategory())) {
            return false;
        }
        return filters.stream()
                .anyMatch(
                        filter ->
                                filter.top().equals(item.topCategory())
                                        && (filter.sub() == null
                                                || filter.sub().equals(item.subCategory())));
    }

    private List<Item> load(Category filter, String name) {
        try {
            CatalogResponse result =
                    client.get()
                            .uri(
                                    uri -> {
                                        uri.path("/api/item-infos/summary")
                                                .queryParam("topCategory", filter.top())
                                                .queryParam("direction", "ASC");
                                        if (filter.sub() != null)
                                            uri.queryParam("subCategory", filter.sub());
                                        if (name != null) uri.queryParam("name", name);
                                        return uri.build();
                                    })
                            .retrieve()
                            .body(CatalogResponse.class);
            if (result == null
                    || !Boolean.TRUE.equals(result.success())
                    || result.data() == null
                    || result.data().stream()
                            .anyMatch(
                                    item ->
                                            item == null
                                                    || item.name() == null
                                                    || item.name().isBlank()
                                                    || item.topCategory() == null
                                                    || item.topCategory().isBlank()
                                                    || item.subCategory() == null
                                                    || item.subCategory().isBlank())) {
                throw new OutfitCatalogException(OUTFIT_CATALOG_UNAVAILABLE);
            }
            return result.data();
        } catch (OutfitCatalogException exception) {
            throw exception;
        } catch (Exception exception) {
            // Do not expose upstream URLs, request payloads or response bodies in errors/logs.
            throw new OutfitCatalogException(OUTFIT_CATALOG_UNAVAILABLE);
        }
    }

    private <T> List<T> invoke(List<Callable<T>> calls) {
        long end = System.nanoTime() + deadline.toNanos();
        List<Future<T>> futures = new ArrayList<>();
        try {
            for (Callable<T> call : calls) futures.add(executor.submit(call));
            List<T> values = new ArrayList<>();
            for (Future<T> future : futures) {
                long remaining = end - System.nanoTime();
                if (remaining <= 0) throw new TimeoutException();
                values.add(future.get(remaining, TimeUnit.NANOSECONDS));
            }
            return values;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OutfitCatalogException(OUTFIT_CATALOG_UNAVAILABLE);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof OutfitCatalogException catalogException) {
                throw catalogException;
            }
            throw new OutfitCatalogException(OUTFIT_CATALOG_UNAVAILABLE);
        } catch (TimeoutException | RejectedExecutionException exception) {
            throw new OutfitCatalogException(OUTFIT_CATALOG_UNAVAILABLE);
        } finally {
            futures.stream()
                    .filter(future -> !future.isDone())
                    .forEach(future -> future.cancel(true));
            executor.purge();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(
            fixedDelayString = "${outfit.catalog.warmup-interval-ms:3600000}",
            initialDelayString = "${outfit.catalog.warmup-interval-ms:3600000}")
    public void warmup() {
        if (!warmupEnabled || !warming.compareAndSet(false, true)) return;
        Thread.startVirtualThread(
                () -> {
                    try {
                        // Normal GETs preserve Batch's one-hour TTL, eviction and DB fallback
                        // ownership.
                        for (Category category :
                                CATEGORIES.values().stream()
                                        .flatMap(List::stream)
                                        .distinct()
                                        .toList()) {
                            try {
                                invoke(List.of(() -> load(category, null)));
                            } catch (OutfitCatalogException exception) {
                                log.warn(
                                        "Outfit catalog warmup category unavailable; normal requests may retry");
                            }
                        }
                    } finally {
                        warming.set(false);
                    }
                });
    }

    @PreDestroy
    public void close() {
        executor.shutdownNow();
    }

    private static RestClient createClient(RestClient.Builder builder, String baseUrl) {
        HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(
                        Objects.requireNonNullElse(value, "").strip(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    private static Map<String, List<Category>> categories() {
        Map<String, List<Category>> result = new LinkedHashMap<>();
        result.put("HEAD", List.of(new Category("방어 장비", "모자/가발")));
        result.put("ACCESSORY_1", List.of(new Category("액세서리", "액세서리")));
        result.put("ACCESSORY_2", List.of(new Category("액세서리", "액세서리")));
        result.put("FACE", List.of(new Category("액세서리", "얼굴 장식")));
        result.put(
                "CLOTHING",
                List.of(
                        new Category("갑옷 장비", "중갑옷"),
                        new Category("갑옷 장비", "경갑옷"),
                        new Category("갑옷 장비", "천옷")));
        result.put("TAIL", List.of(new Category("액세서리", "꼬리")));
        result.put("HANDS", List.of(new Category("방어 장비", "장갑")));
        result.put("FEET", List.of(new Category("방어 장비", "신발")));
        result.put(
                "WEAPON",
                List.of(
                        new Category("근거리 장비", null),
                        new Category("원거리 장비", null),
                        new Category("마법 장비", null),
                        new Category("점성술 장비", null)));
        result.put("ROBE", List.of(new Category("방어 장비", "로브")));
        result.put("MOTION", List.of(new Category("기타", "제스처")));
        return result;
    }
}
