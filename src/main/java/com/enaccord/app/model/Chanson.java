package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chansons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chanson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String artiste;

    // 🎵 Le contenu au format ChordPro (ajouté ici)
    @Column(columnDefinition = "TEXT")
    private String contenuChordPro;

    // 👤 Propriétaire (Créateur initial de la chanson)
    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    // 👥 Groupe associé (Optionnel : si renseigné, la chanson appartient au groupe)
    @ManyToOne
    @JoinColumn(name = "id_groupe", nullable = true)
    private Groupe groupe;
}