package com.example.file_service.controllers;


import com.example.file_service.utils.TestJwtUtil;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class FileControllerIT {


    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void testDir_cleanUp() throws Exception{
        String token = TestJwtUtil.generateAdminToken();
        String json = mockMvc.perform(get("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> fileNameList = objectMapper.readValue(json, new TypeReference<List<String>>(){});
        for (String fileName : fileNameList) {
            mockMvc.perform(delete("/api/v1/files/{key}", fileName)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

    }

    @Test
    public void upload_success() throws Exception{

        String token = TestJwtUtil.generateAdminToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );
        MvcResult json = mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                 .andExpect(status().isCreated())
                .andReturn();

    }

    @Test
    public void uploadBatch_Success() throws Exception{
        String token = TestJwtUtil.generateAdminToken();

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World1".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/files/batch")
                        .file(file1)
                        .file(file2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void download_Success() throws Exception{
        String token = TestJwtUtil.generateAdminToken();

        // First upload a file to get a real key
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the key from upload response
        String key = JsonPath.read(response, "$.key");

        // Now download using that key
        mockMvc.perform(get("/api/v1/files/{key}", key)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment")))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString(key)))
                .andExpect(content().bytes("Hello World".getBytes()));
    }

    @Test
    void delete_success()throws Exception{
        String token = TestJwtUtil.generateAdminToken();

        // First upload a file to get a real key
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the key from upload response
        String key = JsonPath.read(response, "$.key");

        // Now delete file using that key
        mockMvc.perform(delete("/api/v1/files/{key}", key)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }


    @Test
    public void listFiles_Success() throws Exception{

        // uploading first file
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );
        MockMultipartFile file3 = new MockMultipartFile(
                "files",
                "test3.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/v1/files/batch")
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateAdminToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String fileName1 = JsonPath.read(response, "$[0].key");
        String fileName2 = JsonPath.read(response, "$[1].key");
        String fileName3 = JsonPath.read(response, "$[2].key");




        // Now list file Names
        String result = mockMvc.perform(get("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtUtil.generateAdminToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn()
                .getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();

        List<String> fileNameList = objectMapper.readValue(result, new TypeReference<List<String>>(){});

        Assertions.assertTrue(fileNameList.contains(fileName1)&& fileNameList.contains(fileName2)&& fileNameList.contains(fileName3));

    }



}
