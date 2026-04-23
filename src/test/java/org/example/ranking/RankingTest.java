package org.example.ranking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.preview.TextualFilePreview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RankingTest {
    @Autowired
    private MockMvc mvc;

    public static final String testPath = "C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/ranking/test_data";

    @BeforeEach
    void index() throws Exception {
        String path = "C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/ranking/test_data";

        ///clean root dirs
        mvc.perform(post("/post_root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("""
                                directory=none&type=3
                                """)
                )
                .andExpect(status().isOk());

        mvc.perform(post("/post_root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("directory=" + path + "&type=1")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/index"))
                .andExpect(status().isOk());
        ///reset to default root dirs
        mvc.perform(post("/post_root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("""
                                directory=none&type=0
                                """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void alphabeticRanking() throws Exception {
        mvc.perform(post("/modify_ranking_algorithm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("type=alphabetic")
                )
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test")
                )
                .andExpect(status().isOk())
                .andReturn();

        String filePreviewsJson = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(filePreviewsJson, new TypeReference<>(){});

        assertEquals(3, filePreviews.size());
        assertEquals("test1.txt", filePreviews.get(0).getFileName());
        assertEquals("test2.txt", filePreviews.get(1).getFileName());
        assertEquals("test3.txt", filePreviews.get(2).getFileName());
    }

    @Test
    void lastModifiedRanking() throws Exception {
        mvc.perform(post("/modify_ranking_algorithm")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("type=last_modified")
                )
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test")
                )
                .andExpect(status().isOk())
                .andReturn();

        String filePreviewsJson = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(filePreviewsJson, new TypeReference<>(){});

        assertEquals(3, filePreviews.size());
        assertEquals("test3.txt", filePreviews.get(0).getFileName());
        assertEquals("test2.txt", filePreviews.get(1).getFileName());
        assertEquals("test1.txt", filePreviews.get(2).getFileName());
    }

    @Test
    void combinedRanking() throws Exception {
        mvc.perform(post("/modify_ranking_algorithm")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("type=combined")
                )
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test content=first|second|third")
                )
                .andExpect(status().isOk())
                .andReturn();

        String filePreviewsJson = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(filePreviewsJson, new TypeReference<>(){});

        assertEquals(3, filePreviews.size());
        assertEquals("test1.txt", filePreviews.get(0).getFileName());
        assertEquals("test3.txt", filePreviews.get(1).getFileName());
        assertEquals("test2.txt", filePreviews.get(2).getFileName());
    }
}
