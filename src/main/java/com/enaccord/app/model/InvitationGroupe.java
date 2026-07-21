package com.enaccord.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations_groupe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvitationGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_groupe", nullable = false)
    private Groupe groupe;

    @ManyToOne
    @JoinColumn(name = "id_invite", nullable = false)
    private Utilisateur invite;

    private String statut = "EN_ATTENTE"; // EN_ATTENTE, ACCEPTEE, REFUSEE

    private LocalDateTime dateInvitation = LocalDateTime.now();
}