package com.shopsavvy.shopshavvy.dto.categoryDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetadataFieldDTO {

    private String id;
    private String name;
    private List<String> values;

}
