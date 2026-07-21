package com.enaccord.app.repository;

import com.enaccord.app.model.Accord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccordRepository extends JpaRepository<Accord, Long> {
    // On peut ajouter une méthode pour récupérer les accords triés par leur valeur numérique !
    List<Accord> findAllByOrderByValeurAsc();
}