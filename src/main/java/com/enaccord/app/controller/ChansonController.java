package com.enaccord.app.controller;

import com.enaccord.app.model.*;
import com.enaccord.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chansons")
public class ChansonController {

    @Autowired
    private ChansonRepository chansonRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // 1. Formulaire de création d'une chanson
    @GetMapping("/nouvelle")
    public String nouvelleChansonForm(@RequestParam(value = "groupeId", required = false) Long groupeId, Model model) {
        Chanson chanson = new Chanson();
        if (groupeId != null) {
            Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
            chanson.setGroupe(groupe);
        }
        model.addAttribute("chanson", chanson);
        model.addAttribute("groupeId", groupeId);
        return "formulaire_chanson";
    }

    // 2. Sauvegarder la chanson
    @PostMapping("/enregistrer")
    public String enregistrerChanson(
            @ModelAttribute("chanson") Chanson chanson,
            @RequestParam(value = "groupeId", required = false) Long groupeId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        chanson.setUtilisateur(utilisateur);

        if (groupeId != null) {
            Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
            chanson.setGroupe(groupe);
        }

        chansonRepository.save(chanson);

        // Si c'est une chanson de groupe, on redirige vers l'espace du groupe
        if (groupeId != null) {
            return "redirect:/groupes/" + groupeId;
        }
        return "redirect:/"; // Sinon vers l'accueil perso
    }
}