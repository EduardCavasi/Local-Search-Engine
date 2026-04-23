package org.example.parser;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ParserTest {
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void index() throws Exception {
        String path = "C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/parser/test_data";

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
    void simpleParser() throws Exception {
        String searchRequest = "path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/parser/test_data content=main";

        MvcResult result = mvc.perform(post("/search")
                .contentType(MediaType.TEXT_PLAIN)
                .content(searchRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(jsonContent, new TypeReference<>(){});

        assertEquals(3, filePreviews.size());
        for(TextualFilePreview filePreview : filePreviews) {
            assertTrue(filePreview.getFileName().equals("test1.txt") || filePreview.getFileName().equals("test4.txt") || filePreview.getFileName().equals("test5.txt"));
            assertTrue(filePreview.getContent().contains("main"));
        }
    }

    @Test
    void orParser() throws Exception {
        String searchRequest = "content=main|ana";

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(searchRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(jsonContent, new TypeReference<>(){});

        assertEquals(5, filePreviews.size());

        for(TextualFilePreview filePreview : filePreviews) {
            assertTrue(filePreview.getFileName().equals("test1.txt") || filePreview.getFileName().equals("test2.txt") || filePreview.getFileName().equals("test3.txt") || filePreview.getFileName().equals("test4.txt") || filePreview.getFileName().equals("test5.txt"));
            if(filePreview.getFileName().equals("test1.txt")){
                assertTrue(filePreview.getContent().contains("main"));
            }
            else if(filePreview.getFileName().equals("test2.txt")){
                assertTrue(filePreview.getContent().contains("ana"));
            }
            else if(filePreview.getFileName().equals("test3.txt")){
                assertTrue(filePreview.getContent().contains("ana"));
            }
            else if(filePreview.getFileName().equals("test4.txt")){
                assertTrue(filePreview.getContent().contains("main"));
            }
            else {
                assertTrue(filePreview.getContent().contains("main"));
            }
        }
    }

    @Test
    void andOrParser() throws Exception {
        String searchRequest = "path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/parser/test_data content=main|ana content=\"lorem ipsum\"";

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(searchRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(jsonContent, new TypeReference<>(){});

        assertEquals(1, filePreviews.size());
        assertEquals("test4.txt", filePreviews.get(0).getFileName());
        for (TextualFilePreview filePreview : filePreviews) {
            assertTrue(filePreview.getContent().contains("main"));
            assertTrue(filePreview.getContent().contains("lorem ipsum"));
        }
    }

    @Test
    void difficultParser1() throws Exception{
        String searchRequest = new StringBuilder()
                .append("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/parser/test_data ")
                .append("content=main|ana ")
                .append("content=\"lorem ipsum\" ")
                .append("content=\"inca  niste   content\" ")
                .append("extension=txt ")
                .append("name=test4 ")
                .append("size=<10000000|>0 ")
                .append("size=<10000000 ")
                .append("created=<2026-04-23T10:15:30.123Z|>2026-01-23T10:15:30.123Z")
                .toString();

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(searchRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(jsonContent, new TypeReference<>(){});

        assertEquals(1, filePreviews.size());
        assertEquals("test4.txt", filePreviews.get(0).getFileName());
        for (TextualFilePreview filePreview : filePreviews) {
            assertTrue(filePreview.getContent().contains("main"));
            assertTrue(filePreview.getContent().contains("lorem ipsum"));
        }
    }

    @Test
    void difficultParser2() throws Exception{
        String searchRequest = new StringBuilder()
                .append("path=C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/test/java/org/example/parser/test_data ")
                .append("content=main|ana ")
                .append("content=\"lorem ipsum\"|function|mere ")
                .append("extension=txt ")
                .append("size=<10000000|>0 ")
                .append("size=<10000000 ")
                .append("created=<2026-04-23T10:15:30.123Z|>2026-01-23T10:15:30.123Z")
                .toString();

        MvcResult result = mvc.perform(post("/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(searchRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<TextualFilePreview> filePreviews = mapper.readValue(jsonContent, new TypeReference<>(){});

        assertEquals(3, filePreviews.size());
        for(TextualFilePreview filePreview: filePreviews){
            assertTrue(filePreview.getFileName().equals("test2.txt") || filePreview.getFileName().equals("test4.txt") || filePreview.getFileName().equals("test5.txt"));
            if(filePreview.getFileName().equals("test2.txt")){
                assertTrue(filePreview.getContent().contains("ana"));
                assertTrue(filePreview.getContent().contains("mere"));
            }
            else if(filePreview.getFileName().equals("test4.txt")){
                assertTrue(filePreview.getContent().contains("main"));
                assertTrue(filePreview.getContent().contains("lorem"));
                assertTrue(filePreview.getContent().contains("ipsum"));
            }
            else{
                assertTrue(filePreview.getContent().contains("main"));
                assertTrue(filePreview.getContent().contains("function"));
            }
        }

    }
}
