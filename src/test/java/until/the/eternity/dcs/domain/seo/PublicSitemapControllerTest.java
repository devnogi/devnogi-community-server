package until.the.eternity.dcs.domain.seo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import until.the.eternity.dcs.common.exception.GlobalExceptionHandler;
import until.the.eternity.dcs.common.response.ApiResponseAdvice;

class PublicSitemapControllerTest {
    private PublicSitemapRepository repository;
    private MockMvc mvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        repository = mock(PublicSitemapRepository.class);
        mapper = new ObjectMapper().findAndRegisterModules();
        mvc =
                MockMvcBuilders.standaloneSetup(
                                new PublicSitemapController(new PublicSitemapService(repository)))
                        .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                        .setControllerAdvice(
                                new ApiResponseAdvice(mapper), new GlobalExceptionHandler())
                        .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"posts", "outfits"})
    void anonymousGetReturnsOnlyPublicProjectionFieldsInTheExistingEnvelope(String kind)
            throws Exception {
        var rows =
                List.of(
                        new PublicSitemapPage.Entry(12, "2026-11-13T00:15:30Z"),
                        new PublicSitemapPage.Entry(18, "2026-11-13T01:00:00Z"));
        if (kind.equals("posts")) when(repository.posts(0, 2)).thenReturn(rows);
        else when(repository.outfits(0, 2)).thenReturn(rows);
        var response =
                mvc.perform(get("/api/" + kind + "/sitemap").param("limit", "1"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Cache-Control", "no-store"))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.items[0].id").value(12))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        var expected =
                mapper.readTree(Files.readString(Path.of("contracts/public-sitemap.example.json")));
        assertThat(mapper.readTree(response).get("data")).isEqualTo(expected.get("data"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"posts", "outfits"})
    void invalidInputIs400BeforeQueryingTheDatabase(String kind) throws Exception {
        String path = "/api/" + kind + "/sitemap";
        for (String invalid :
                List.of("", "0", "-1", "+1", "01", " 1", "1.0", "abc", "9223372036854775808")) {
            mvc.perform(get(path).param("cursor", invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
        for (String invalid : List.of("", "0", "-1", "01", "1001", "100000000000", "abc")) {
            mvc.perform(get(path).param("limit", invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"posts", "outfits"})
    void databaseFailureIsNotReportedAsAnEmptySuccessfulPage(String kind) throws Exception {
        if (kind.equals("posts"))
            when(repository.posts(anyLong(), anyInt()))
                    .thenThrow(
                            new DataAccessResourceFailureException(
                                    "synthetic unavailable database"));
        else
            when(repository.outfits(anyLong(), anyInt()))
                    .thenThrow(
                            new DataAccessResourceFailureException(
                                    "synthetic unavailable database"));
        mvc.perform(get("/api/" + kind + "/sitemap"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"posts", "outfits"})
    void endpointDoesNotAcceptMutations(String kind) throws Exception {
        mvc.perform(post("/api/" + kind + "/sitemap")).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(repository);
    }
}
