package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String pseudo;

    @Column(nullable = false)
    private String motDePasse; // Sera stocké sous forme de hash BCrypt

    @Column(nullable = true) // On le met optionnel au début
    private String poste;


    private String role = "ROLE_MEMBRE"; // Rôle par défaut
}