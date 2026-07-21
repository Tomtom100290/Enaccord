package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Accord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_fr", nullable = false)
    private String libelleFr; // Ex: "Do", "Ré"

    @Column(name = "libelle_en", nullable = false)
    private String libelleEn; // Ex: "C", "D"

    @Column(nullable = false)
    private String couleur; // Ex: "#AE1451"

    @Column(nullable = false)
    private Integer valeur; // Ex: 1 pour Do, 2 pour Do#, 3 pour Ré...
}
