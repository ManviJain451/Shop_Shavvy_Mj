package com.shopsavvy.shopshavvy.model.categories;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "category_metadata_field_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetadataFieldValues {

    @EmbeddedId
    private CategoryMetadataFieldValueId id;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @MapsId("categoryMetadataFieldId")
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField categoryMetadataField;

    private String values;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class CategoryMetadataFieldValueId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryMetadataFieldId;

}
