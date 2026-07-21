package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "membre_groupe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembreGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liaison vers l'utilisateur (id_membre)
    @ManyToOne
    @JoinColumn(name = "id_membre", nullable = false)
    private Utilisateur utilisateur;

    // Liaison vers le groupe (id_groupe)
    @ManyToOne
    @JoinColumn(name = "id_groupe", nullable = false)
    private Groupe groupe;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // Un petit constructeur pratique pour l'associer facilement
    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now(); // S'applique automatiquement à la création
    }
}