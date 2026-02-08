package com.duri.duriauth.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@Entity
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String username;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "platform_type", length = 20, nullable = false)
    private String platformType;

    @Column(nullable = false)
    @Enumerated(value = STRING)
    private Role role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

}