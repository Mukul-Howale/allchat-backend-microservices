package com.example.AllChat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long phoneNumber;
    private Long friendsCount;
    private Long thumbsUp;
    private Long thumbsDown;
    private Boolean isPaidUser;
    private Boolean isEmailVerified;
}
