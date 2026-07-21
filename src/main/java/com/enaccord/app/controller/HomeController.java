package com.enaccord.app.controller;

import com.enaccord.app.model.Chanson;
import com.enaccord.app.model.Utilisateur;
import com.enaccord.app.repository.ChansonRepository;
import com.enaccord.app.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ChansonRepository chansonRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/")
    public String accueil(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        // 1. Si l'utilisateur n'est pas connecté, on redirige vers la page de connexion
        if (currentUser == null) {
            return "redirect:/connexion";
        }

        // 2. S'il est connecté, on récupère ses informations complètes
        Utilisateur utilisateur = utilisateurRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 3. Récuérer UNIQUEMENT les chansons créées par cet utilisateur
        // (Note : Assure-toi d'avoir une relation/un champ 'createur' ou 'utilisateur' dans ton entité Chanson)
        List<Chanson> mesChansons = chansonRepository.findByUtilisateurIdAndGroupeIsNull(utilisateur.getId());
        model.addAttribute("chansons", mesChansons);
        model.addAttribute("utilisateurConnecte", utilisateur);

        return "index"; // Renvoie vers index.html (qui est maintenant l'espace privé de l'utilisateur)
    }
}