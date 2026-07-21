package com.enaccord.app.repository;

import com.enaccord.app.model.InvitationGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvitationGroupeRepository extends JpaRepository<InvitationGroupe, Long> {
    List<InvitationGroupe> findByInviteEmailAndStatut(String email, String statut);
}