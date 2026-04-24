package org.example.suggest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SuggestTest {
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        mvc.perform(delete("/api/history/requests"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/history/results"))
                .andExpect(status().isOk());

        String path = "C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data";

        ///clean root dirs
        mvc.perform(post("/api/system/root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("""
                                directory=none&type=3
                                """)
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/system/root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("directory=" + path + "&type=1")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/system/index"))
                .andExpect(status().isOk());
        ///reset to default root dirs
        mvc.perform(post("/api/system/root_directory_rules")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("""
                                directory=none&type=0
                                """)
                )
                .andExpect(status().isOk());
    }

    @AfterEach
    void deleteHistory() throws Exception{
        mvc.perform(delete("/api/history/requests"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/history/results"))
                .andExpect(status().isOk());
    }

    @Test
    void testSuggest() throws Exception {
        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test3")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test2")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test1")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data name=test3")
                )
                .andExpect(status().isOk());


        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test1")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test3")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data name=test3")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data name=test3")
                )
                .andExpect(status().isOk());

        mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test1")
                )
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/api/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data name=test3")
                )
                .andExpect(status().isOk()).andReturn();

        MvcResult topRequestsJson = mvc.perform(get("/api/history/requests/top")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("top=3")
                )
                .andExpect(status().isOk())
                .andReturn();

        MvcResult topResultsJson = mvc.perform(get("/api/history/results/top")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("top=3")
                )
                .andExpect(status().isOk())
                .andReturn();

        MvcResult topSuggestionsJson = mvc.perform(get("/api/history/requests/suggestions")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("top=3&query=name%3Dtest1")
                )
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> topRequests = mapper.readValue(topRequestsJson.getResponse().getContentAsString(), new TypeReference<>() {});
        Map<String, Integer> topResults = mapper.readValue(topResultsJson.getResponse().getContentAsString(), new TypeReference<>() {});
        List<String> topSuggestions = mapper.readValue(topSuggestionsJson.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(3, topRequests.size());
        int i = 0;
        for(Map.Entry<String, Integer> entry : topRequests.entrySet()) {
            ++i;
            if(i == 1) {
                assertEquals("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data name=test3", entry.getKey());
                assertEquals(4, entry.getValue());
            }
            if(i == 2) {
                assertEquals("name=test1", entry.getKey());
                assertEquals(3, entry.getValue());
            }
            if(i == 3) {
                assertEquals("name=test3", entry.getKey());
                assertEquals(2, entry.getValue());
            }
        }

        assertEquals(3, topResults.size());
        i = 0;
        for(Map.Entry<String, Integer> entry : topResults.entrySet()) {
            ++i;
            if(i == 1) {
                assertEquals("C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data/test3.txt", entry.getKey());
                assertEquals(6, entry.getValue());
            }
            if(i == 2) {
                assertEquals("C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data/test1.txt", entry.getKey());
                assertEquals(3, entry.getValue());
            }
            if(i == 3){
                assertEquals("C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/suggest/test_data/test2.txt", entry.getKey());
                assertEquals(1, entry.getValue());
            }
        }
        assertEquals(1, topSuggestions.size());
        assertEquals("name=test1", topSuggestions.get(0));

    }
}
