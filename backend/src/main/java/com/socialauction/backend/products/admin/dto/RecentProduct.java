package com.socialauction.backend.products.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder 
public class RecentProduct {
    
    Integer productId;

    String productName;

    Integer startPrice;

    String organizationName;

    String image;

}
