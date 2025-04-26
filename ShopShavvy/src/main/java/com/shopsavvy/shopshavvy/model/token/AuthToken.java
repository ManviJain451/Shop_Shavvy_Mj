package com.shopsavvy.shopshavvy.model.token;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @Column(name = "expiration_time", nullable = false)
    private Date expirationTime;

}
