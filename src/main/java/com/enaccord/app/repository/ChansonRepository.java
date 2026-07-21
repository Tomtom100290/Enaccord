package com.enaccord.app.repository;

import com.enaccord.app.model.Chanson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChansonRepository extends JpaRepository<Chanson, Long> {

    // Pour la partie personnel (uniquement ses chansons propres à l'utilisisateur)
    List<Chanson> findByUtilisateurIdAndGroupeIsNull(Long utilisateurId);

    // Pour l'espace groupe, affiche les chansons du gorupe.
    List<Chanson> findByGroupeId(Long groupeId);
}