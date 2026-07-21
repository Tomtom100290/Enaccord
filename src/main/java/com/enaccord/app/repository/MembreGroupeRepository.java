package com.enaccord.app.repository;

import com.enaccord.app.model.MembreGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MembreGroupeRepository extends JpaRepository<MembreGroupe, Long> {
    // Permet de trouver tous les groupes d'un utilisateur
    List<MembreGroupe> findByUtilisateurId(Long utilisateurId);
}