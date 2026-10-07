package com.socialauction.backend.global;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.socialauction.backend.global.upload.UploadFolder;
import com.socialauction.backend.global.upload.UploadUrls;
import com.socialauction.backend.global.upload.UploadWebConfig;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UploadWebTest {
    @TempDir Path directory;

    // @Configuration을 붙이지 않는다. 붙이면 VS Code 실행 시 클래스패스에 들어간 테스트 클래스가
    // AppStart의 컴포넌트 스캔에 걸리고, @EnableWebMvc 때문에 스프링 부트 MVC 자동 설정이 꺼진다.
    // context.register()로 직접 등록하므로 테스트에는 필요 없다.
    @EnableWebMvc
    @Import(UploadWebConfig.class)
    static class Config {}

    @Test void urlsPreserveExistingPathsAndEncodeNewFilenames() {
        assertNull(UploadUrls.from(UploadFolder.IMAGES, null));
        assertNull(UploadUrls.from(UploadFolder.IMAGES, ""));
        assertEquals("/images/new.png", UploadUrls.from(UploadFolder.IMAGES, "new.png"));
        assertEquals("/images/old.png", UploadUrls.from(UploadFolder.IMAGES, "/images/old.png"));
        assertEquals("https://example.com/a.png", UploadUrls.from(UploadFolder.IMAGES, "https://example.com/a.png"));
        assertEquals("/images/a%20b%23.png", UploadUrls.from(UploadFolder.IMAGES, "a b#.png"));
        assertTrue(UploadUrls.from(UploadFolder.IMAGES, "사진.jpg").startsWith("/images/%"));
        assertEquals("/organization/uuid_logo%20(1).png", UploadUrls.from(UploadFolder.ORGANIZATION, "uuid_logo (1).png"));
    }
    @Test void allResponseDtosReturnImageUrls() {
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ImageDto.from(
                com.socialauction.backend.products.entity.ImageEntity.builder().image("new.png")
                .productEntity(com.socialauction.backend.products.entity.ProductEntity.builder().productId(1).build()).build()).getImage());
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ProductListResponse.builder().imageUrl("new.png").build().getImageUrl());
        assertEquals("/images/new.png", com.socialauction.backend.products.admin.dto.ProductManageDto.builder().image("new.png").build().getImage());
        assertEquals("/images/new.png", com.socialauction.backend.products.user.dto.ProductDto.builder().image("new.png").build().getImage());
        assertEquals("/organization/logo.png", com.socialauction.backend.organization.dto.admin.OrganizationInfoResponse.from(
                com.socialauction.backend.organization.entity.OrganizationEntity.builder().organizationImageFileName("logo.png").build())
                .getOrganizationImageFileName());
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

            // 기관 폴더도 같은 방식으로 업로드 직후 제공된다
            Path logo = directory.resolve("backend/src/main/resources/static/organization/live-logo.png");
            Files.createDirectories(logo.getParent());
            Files.write(logo, new byte[]{9,8});
            mvc.perform(get("/organization/live-logo.png")).andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[]{9,8}));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
    @Test void fileServiceUploadsToFolderAndKeepsSampleFilesOnDelete() throws Exception {
        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", directory.toString());
            FileService files = new FileService();
            assertNull(files.upload(UploadFolder.ORGANIZATION, null));
            String saved = files.upload(UploadFolder.ORGANIZATION,
                    new MockMultipartFile("file", "my_logo.png", "image/png", new byte[]{1}));
            assertTrue(saved.endsWith("_my-logo.png"));
            Path savedPath = UploadFolder.ORGANIZATION.dir().resolve(saved);
            assertTrue(Files.exists(savedPath));

            Path sample = UploadFolder.ORGANIZATION.dir().resolve("00000000-0000-4000-8000-000000000001_org01-logo.png");
            Files.write(sample, new byte[]{1});
            files.delete(UploadFolder.ORGANIZATION, sample.getFileName().toString());
            assertTrue(Files.exists(sample), "샘플 파일은 지우지 않는다");

            files.delete(UploadFolder.ORGANIZATION, saved);
            assertFalse(Files.exists(savedPath), "업로드 파일은 지운다");
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}
