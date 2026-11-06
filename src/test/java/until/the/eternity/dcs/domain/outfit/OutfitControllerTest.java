package until.the.eternity.dcs.domain.outfit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.common.response.ApiResponseAdvice;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService;

class OutfitControllerTest {
    private OutfitService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(OutfitService.class);
        var mapper = new ObjectMapper().findAndRegisterModules();
        mvc =
                MockMvcBuilders.standaloneSetup(
                                new OutfitController(
                                        service, mock(OutfitCatalogService.class), mapper))
                        .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                        .setControllerAdvice(
                                new OutfitExceptionHandler(),
                                new ApiResponseAdvice(mapper),
                                new until.the.eternity.dcs.common.exception
                                        .GlobalExceptionHandler())
                        .build();
    }

    @Test
    void publicFeedUsesEnvelopeAndNoStore() throws Exception {
        when(service.list(
                        anyString(),
                        anyInt(),
                        anyInt(),
                        nullable(String.class),
                        anyList(),
                        nullable(List.class),
                        nullable(String.class),
                        nullable(String.class)))
                .thenReturn(new OutfitDtos.Page<>(List.of(), null, false));
        mvc.perform(get("/api/outfits"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void authErrorRetainsStatusAndErrorEnvelope() throws Exception {
        when(service.reaction(1, true, true))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        mvc.perform(put("/api/outfits/1/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("OUTFIT_401"));
    }

    @Test
    void malformedSearchIs400() throws Exception {
        mvc.perform(get("/api/outfits").param("items", "{broken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/outfits/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void multipartPutAcceptsJsonPartAndMedia() throws Exception {
        var data =
                new MockMultipartFile(
                        "data",
                        "",
                        "application/json",
                        "{\"content\":\"hello\",\"tags\":[],\"items\":[],\"mediaOrder\":[]}"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mvc.perform(
                        multipart("/api/outfits/1")
                                .file(data)
                                .with(
                                        request -> {
                                            request.setMethod("PUT");
                                            return request;
                                        }))
                .andExpect(status().isOk());
        verify(service).write(eq(1L), argThat(write -> write.content().equals("hello")), isNull());
    }

    @Test
    void imageRedirectUsesFreshSignedLocation() throws Exception {
        when(service.image(1, 2)).thenReturn("https://storage.example/image.png?signature=fresh");
        mvc.perform(get("/api/outfits/1/images/2"))
                .andExpect(status().isFound())
                .andExpect(
                        header().string(
                                        "Location",
                                        "https://storage.example/image.png?signature=fresh"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void ownershipFailureAndDeleteNoContent() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 변경할 수 있습니다."))
                .when(service)
                .delete(1);
        mvc.perform(delete("/api/outfits/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(delete("/api/outfits/2"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
