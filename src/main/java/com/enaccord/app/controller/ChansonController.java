package com.enaccord.app.controller;

import com.enaccord.app.model.*;
import com.enaccord.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/chansons")
public class ChansonController {

    @Autowired
    private ChansonRepository chansonRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AccordRepository accordRepository;

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
        model.addAttribute("accordsDisponibles", accordRepository.findAll());

        return "formulaire_chanson";
    }

    // NOUVEAU : 2. Afficher les détails d'une chanson personnelle
    @GetMapping("/{id}")
    public String detailChanson(@PathVariable("id") Long id, Model model) {
        Chanson chanson = chansonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chanson introuvable"));

        model.addAttribute("chanson", chanson);
        return "voir_chanson"; // Renvoie vers src/main/resources/templates/detail_chanson.html
    }

    // NOUVEAU : 3. Formulaire de modification d'une chanson existante
    @GetMapping("/{id}/modifier")
    public String modifierChansonForm(@PathVariable("id") Long id, Model model) {
        Chanson chanson = chansonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chanson introuvable"));

        Long groupeId = (chanson.getGroupe() != null) ? chanson.getGroupe().getId() : null;

        model.addAttribute("chanson", chanson);
        model.addAttribute("groupeId", groupeId);
        model.addAttribute("accordsDisponibles", accordRepository.findAll());

        return "modifier_chanson"; // Réutilise le même formulaire HTML que pour la création
    }

    // 4. Sauvegarder la chanson (Création OU Modification)
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

        if (groupeId != null) {
            return "redirect:/groupes/" + groupeId;
        }
        return "redirect:/";
    }
}