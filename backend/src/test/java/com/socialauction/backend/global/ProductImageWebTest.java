package com.socialauction.backend.global;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductImageWebTest {
    @TempDir Path directory;

    @Configuration
    @EnableWebMvc
    @Import(ProductImageWebConfig.class)
    static class Config {}

    @Test void urlsPreserveExistingPathsAndEncodeNewFilenames() {
        assertNull(ProductImageUrls.from(null));
        assertNull(ProductImageUrls.from(""));
        assertEquals("/images/new.png", ProductImageUrls.from("new.png"));
        assertEquals("/images/old.png", ProductImageUrls.from("/images/old.png"));
        assertEquals("https://example.com/a.png", ProductImageUrls.from("https://example.com/a.png"));
        assertEquals("/images/a%20b%23.png", ProductImageUrls.from("a b#.png"));
        assertTrue(ProductImageUrls.from("사진.jpg").startsWith("/images/%"));
    }
    @Test void allResponseDtosReturnImageUrls() {
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ImageDto.from(
                com.socialauction.backend.products.entity.ImageEntity.builder().image("new.png")
                .productEntity(com.socialauction.backend.products.entity.ProductEntity.builder().productId(1).build()).build()).getImage());
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ProductListResponse.builder().imageUrl("new.png").build().getImageUrl());
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ProductManageDto.builder().image("new.png").build().getImage());
        assertEquals("/images/new.png", com.socialauction.backend.products.user.dto.ProductDto.builder().image("new.png").build().getImage());
    }
    @Test void servesFilesCreatedAndReplacedAfterContextStartup() throws Exception {
        String originalDir = System.getProperty("user.dir");
        try (var context = new AnnotationConfigWebApplicationContext()) {
            System.setProperty("user.dir", directory.toString());
            context.setServletContext(new MockServletContext());
            context.register(Config.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/images/live.png")).andExpect(status().isNotFound());
            Path image = directory.resolve("backend/src/main/resources/static/images/live.png");
            Files.createDirectories(image.getParent());
            Files.write(image, new byte[]{1,2,3});
            mvc.perform(get("/images/live.png")).andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[]{1,2,3}));
            Files.write(image, new byte[]{4,5,6,7});
            mvc.perform(get("/images/live.png")).andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[]{4,5,6,7}));
            mvc.perform(get("/images/../application.properties")).andExpect(status().is4xxClientError());
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}
