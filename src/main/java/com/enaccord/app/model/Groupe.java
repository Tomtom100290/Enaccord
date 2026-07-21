package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "groupes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_du_groupe", nullable = false)
    private String nomDuGroupe;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image; // Stockera l'URL ou le chemin de l'image (ex: "/images/groupes/rock.png")

    // Relation inverse pour récupérer facilement tous les membres d'un groupe
    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembreGroupe> membres;
}