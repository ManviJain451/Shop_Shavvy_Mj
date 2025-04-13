package com.shopsavvy.shopshavvy.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SellerUpdateProfileDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private String companyContact;
    private MultipartFile profileImage;
}
