package com.shopsavvy.shopshavvy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressDTO extends AddressDTO{
    private boolean makeDefault;

}
