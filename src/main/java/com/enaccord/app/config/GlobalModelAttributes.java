package com.enaccord.app.config;

import com.enaccord.app.model.Utilisateur;
import com.enaccord.app.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @ModelAttribute("utilisateurConnecte")
    public Utilisateur ajouterUtilisateurConnecte(Authentication authentication) {
        // Si personne n'est connecté (page publique comme /connexion), on ne fait rien
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email).orElse(null);
    }
}