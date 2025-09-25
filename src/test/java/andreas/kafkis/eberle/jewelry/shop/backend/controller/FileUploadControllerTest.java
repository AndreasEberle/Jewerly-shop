package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileUploadController fileUploadController;

    private UUID testProductId;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(fileUploadController).build();
    }

    @Test
    void testUploadValidImageFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.webp",
                "image/webp",
                "fake image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("products/" + testProductId + "/")));
    }

    @Test
    void testUploadEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.webp",
                "image/webp",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    void testUploadFileTooLarge() throws Exception {
        // Given - Create a file larger than 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.webp",
                "image/webp",
                largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(largeFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File too large. Max size: 10MB"));
    }

    @Test
    void testUploadInvalidFileType() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid file type")));
    }

    @Test
    void testUploadFileWithNullFilename() throws Exception {
        // Given
        MockMultipartFile fileWithNullName = new MockMultipartFile(
                "file",
                null,
                "image/webp",
                "fake image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(fileWithNullName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid file type")));
    }

    @Test
    void testUploadValidJpegFile() throws Exception {
        // Given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake jpeg content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(jpegFile))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("products/" + testProductId + "/")));
    }

    @Test
    void testUploadValidPngFile() throws Exception {
        // Given
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "fake png content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/upload/{productId}", testProductId)
                .file(pngFile))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("products/" + testProductId + "/")));
    }
}
