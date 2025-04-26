package com.shopsavvy.shopshavvy.model.token;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BlackListedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "type", nullable = false)
    private String type;
}
