package com.socialauction.backend.products.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.socialauction.backend.products.admin.dto.ImageDto;
import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.repository.ProductRepository;
import com.socialauction.backend.products.admin.repository.ImageRepository;
import com.socialauction.backend.products.entity.ProductEntity;
import com.socialauction.backend.products.entity.ImageEntity;
import com.socialauction.backend.category.repository.CategoryRepository;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.global.FileService;
import com.socialauction.backend.global.UploadFolder;

class ProductImageOrderTest {
    ProductRepository products;
    ImageRepository images;
    FileService files;
    ProductService service;
    ProductEntity existing;

    @BeforeEach void setup() {
        products = mock(ProductRepository.class);
        images = mock(ImageRepository.class);
        files = mock(FileService.class);
        var categories = mock(CategoryRepository.class);
        var organizations = mock(OrganizationRepository.class);
        service = new ProductService(products, images, categories, organizations,
                mock(AuctionRepository.class), files);
        when(categories.findById(1)).thenReturn(Optional.of(new com.socialauction.backend.category.entity.CategoryEntity()));
        when(organizations.findById(1)).thenReturn(Optional.of(new com.socialauction.backend.organization.entity.OrganizationEntity()));
        when(files.upload(eq(UploadFolder.IMAGES), any())).thenReturn("test.png");
        when(products.save(any())).thenAnswer(inv -> {
            ProductEntity product = inv.getArgument(0);
            product.setProductId(10);
            return product;
        });
        existing = ProductEntity.builder().productId(10).build();
        ImageEntity cover = ImageEntity.builder().imageId(20).image("old.png")
                .sortOrder(1).productEntity(existing).build();
        existing.getImageEntity().add(cover);
        when(products.findById(10)).thenReturn(Optional.of(existing));
        when(images.findById(20)).thenReturn(Optional.of(cover));
    }
    ImageDto image(Integer slot) {
        return ImageDto.builder().sortOrder(slot).image("new.png")
                .file(new MockMultipartFile("file", "test.png", "image/png", new byte[]{1})).build();
    }
    ProductDto request(ImageDto... entries) {
        return ProductDto.builder().productId(10).name("test").startPrice(100)
                .organizationId(1).categoryId(1).images(List.of(entries)).build();
    }
    void badCreate(ProductDto dto) {
        var error = assertThrows(ResponseStatusException.class, () -> service.saveProduct(dto));
        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(files);
        verify(products, never()).save(any());
    }
    @Test void creationRequiresCover() { badCreate(request(image(3))); }
    @Test void creationRequiresImages() { badCreate(request()); }
    @Test void rejectsDuplicateSlotsBeforeWritingFiles() { badCreate(request(image(1), image(1))); }
    @Test void rejectsOutOfRangeAndMissingSlot() {
        badCreate(request(image(1), image(6)));
        badCreate(request(image(null)));
        badCreate(request(image(0)));
    }
    @Test void rejectsEmptyCoverFile() {
        var cover = image(1); cover.setFile(null); badCreate(request(cover));
    }
    @Test void rejectsMoreThanFiveImages() {
        badCreate(request(image(1),image(2),image(3),image(4),image(5),image(5)));
    }
    @Test void preservesExplicitSlotsWithGapsAndRequestOrder() {
        assertTrue(service.saveProduct(request(image(5),image(1),image(3))));
        var captor = org.mockito.ArgumentCaptor.forClass(ProductEntity.class);
        verify(products).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(List.of(5,1,3), saved.getImageEntity().stream().map(ImageEntity::getSortOrder).toList());
        assertTrue(saved.getImageEntity().stream().allMatch(i -> i.getProductEntity() == saved));
        assertEquals(3, saved.getImageEntity().size());
    }
    @Test void replacementKeepsIdAndCoverSlot() {
        var dto=image(1); dto.setImageId(20);
        assertTrue(service.updateProduct(request(dto)));
        assertEquals(20, existing.getImageEntity().get(0).getImageId());
        assertEquals(1, existing.getImageEntity().get(0).getSortOrder());
        assertEquals("new.png", existing.getImageEntity().get(0).getImage());
    }
    @Test void cannotMoveCoverOrReplaceAnotherProductsImage() {
        var moved=image(2); moved.setImageId(20);
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(request(moved)));
        var foreign=image(1); foreign.setImageId(999);
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(request(foreign)));
        assertEquals("old.png", existing.getImageEntity().get(0).getImage());
    }
    @Test void cannotAddToOccupiedSlot() {
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(request(image(1))));
    }
    @Test void canAddIntoEmptySlotWithoutResendingCover() {
        assertTrue(service.updateProduct(request(image(4))));
        assertEquals(List.of(1,4), existing.getImageEntity().stream().map(ImageEntity::getSortOrder).toList());
    }
    @Test void omittedImagesPreserveCoverButMissingCoverFails() {
        assertTrue(service.updateProduct(request()));
        existing.getImageEntity().clear();
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(request()));
    }
    @Test void dtoIncludesSlot() {
        assertEquals(1, ImageDto.from(existing.getImageEntity().get(0)).getSortOrder());
    }
}
