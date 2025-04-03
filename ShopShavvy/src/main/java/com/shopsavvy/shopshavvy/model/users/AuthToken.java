package com.shopsavvy.shopshavvy.model.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

}
