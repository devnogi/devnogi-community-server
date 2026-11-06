package until.the.eternity.dcs.domain.outfit.catalog;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogException.Code.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService.Item;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService.Selection;

class OutfitCatalogServiceTest {
    private MockRestServiceServer server;
    private OutfitCatalogService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://catalog.test/oab");
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        service = new OutfitCatalogService(builder.build(), Duration.ofSeconds(8), false);
    }

    @AfterEach
    void tearDown() {
        service.close();
        server.verify();
    }

    @Test
    void filtersEntireCategoryBeforeApplyingLimit() {
        String entries =
                java.util.stream.IntStream.range(0, 30)
                        .mapToObj(
                                i ->
                                        "{\"name\":\"부츠"
                                                + i
                                                + "\",\"topCategory\":\"방어 장비\",\"subCategory\":\"신발\"}")
                        .collect(java.util.stream.Collectors.joining(","));
        server.expect(decodedQuery("topCategory", "방어 장비"))
                .andExpect(decodedQuery("subCategory", "신발"))
                .andRespond(
                        withSuccess(
                                "{\"success\":true,\"data\":[" + entries + "]}",
                                MediaType.APPLICATION_JSON));
        assertThat(service.suggest("FEET", "부츠29"))
                .containsExactly(new Item("부츠29", "방어 장비", "신발"));
    }

    @Test
    void sharedAccessoryItemIsValidatedOnlyOnce() {
        Item item = new Item("반지", "액세서리", "액세서리");
        server.expect(decodedQuery("name", "반지"))
                .andRespond(withSuccess(response(item), MediaType.APPLICATION_JSON));
        service.validateItems(
                List.of(new Selection("ACCESSORY_1", item), new Selection("ACCESSORY_2", item)));
    }

    @Test
    void hatWigCandidatesUseTheHeadSlot() {
        Item item = new Item("가발", "방어 장비", "모자/가발");
        server.expect(decodedQuery("subCategory", "모자/가발"))
                .andRespond(withSuccess(response(item), MediaType.APPLICATION_JSON));
        assertThat(service.suggest("HEAD", "가발")).containsExactly(item);
    }

    @Test
    void acceptsGestureOnlyInMotionSlot() {
        Item item = new Item("춤", "기타", "제스처");
        assertInvalid(() -> service.validate("HEAD", item));
        server.expect(decodedQuery("name", "춤"))
                .andRespond(withSuccess(response(item), MediaType.APPLICATION_JSON));
        service.validate("MOTION", item);
    }

    @Test
    void rejectsUnknownSlotsWrongCategoriesAndAmmunitionWithoutCallingUpstream() {
        assertInvalid(() -> service.suggest("WINGS", "날개"));
        assertInvalid(() -> service.validate("FEET", new Item("검", "근거리 장비", "검")));
        assertInvalid(() -> service.validate("WEAPON", new Item("화살", "원거리 장비", "원거리 소모품")));
        assertInvalid(() -> service.validate("CLOTHING", new Item("가짜", "갑옷 장비", "없는 분류")));
        assertThat(service.suggest("FEET", " ")).isEmpty();
    }

    @Test
    void missingItemIsAnInputError() {
        server.expect(anything())
                .andRespond(
                        withSuccess("{\"success\":true,\"data\":[]}", MediaType.APPLICATION_JSON));
        assertInvalid(() -> service.validate("FEET", new Item("없는 신발", "방어 장비", "신발")));
    }

    @Test
    void upstreamFailureIsRetryableRatherThanInvalidItem() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void malformedSuccessEnvelopeIsRetryable() {
        server.expect(anything())
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void failureEnvelopeIsNotCachedAsEmptySuccess() {
        server.expect(anything())
                .andRespond(
                        withSuccess("{\"success\":false,\"data\":[]}", MediaType.APPLICATION_JSON));
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void requestDeadlineCancelsSlowCatalogCall() {
        service.close();
        RestClient.Builder builder = RestClient.builder().baseUrl("https://catalog.test/oab");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new OutfitCatalogService(builder.build(), Duration.ofMillis(200), false);
        server.expect(anything())
                .andRespond(
                        request -> {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                            }
                            return withSuccess(
                                            "{\"success\":true,\"data\":[]}",
                                            MediaType.APPLICATION_JSON)
                                    .createResponse(request);
                        });
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void blankItemInSuccessfulEnvelopeIsUpstreamFailure() {
        server.expect(anything())
                .andRespond(
                        withSuccess(
                                response(new Item(" ", "방어 장비", "신발")),
                                MediaType.APPLICATION_JSON));
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void nullItemInSuccessfulEnvelopeIsUpstreamFailure() {
        server.expect(anything())
                .andRespond(
                        withSuccess(
                                "{\"success\":true,\"data\":[null]}", MediaType.APPLICATION_JSON));
        assertUnavailable(() -> service.suggest("FEET", "신발"));
    }

    @Test
    void weaponSuggestionsIncludeAstrologyAndExcludeConsumables() {
        for (String category : List.of("근거리 장비", "원거리 장비", "마법 장비", "점성술 장비")) {
            Item item = new Item("무기", category, category.equals("원거리 장비") ? "원거리 소모품" : "장비");
            server.expect(decodedQuery("topCategory", category))
                    .andRespond(withSuccess(response(item), MediaType.APPLICATION_JSON));
        }
        assertThat(service.suggest("WEAPON", "무기"))
                .hasSize(3)
                .extracting(Item::topCategory)
                .contains("점성술 장비")
                .doesNotContain("원거리 장비");
    }

    private static org.springframework.test.web.client.RequestMatcher decodedQuery(
            String name, String value) {
        return request -> assertThat(request.getURI().getQuery()).contains(name + "=" + value);
    }

    private static String response(Item item) {
        return "{\"success\":true,\"data\":[{\"name\":\""
                + item.name()
                + "\",\"topCategory\":\""
                + item.topCategory()
                + "\",\"subCategory\":\""
                + item.subCategory()
                + "\"}]}";
    }

    private static void assertInvalid(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(
                        OutfitCatalogException.class,
                        error -> assertThat(error.getCode()).isEqualTo(OUTFIT_INVALID_ITEM));
    }

    private static void assertUnavailable(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(
                        OutfitCatalogException.class,
                        error -> assertThat(error.getCode()).isEqualTo(OUTFIT_CATALOG_UNAVAILABLE));
    }
}
