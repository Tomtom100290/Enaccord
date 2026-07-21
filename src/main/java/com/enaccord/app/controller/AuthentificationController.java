package com.enaccord.app.controller;

import com.enaccord.app.model.Utilisateur;
import com.enaccord.app.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthentificationController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Formulaire de connexion
    @GetMapping("/connexion")
    public String connexion() {
        return "connexion"; // Fichier connexion.html
    }

    // 2. Formulaire d'inscription
    @GetMapping("/inscription")
    public String inscriptionForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "inscription"; // Fichier inscription.html
    }

    // 3. Traiter l'inscription
    @PostMapping("/inscription")
    public String inscrireUtilisateur(@ModelAttribute("utilisateur") Utilisateur utilisateur, Model model) {
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            model.addAttribute("error", "Cet email est déjà utilisé !");
            return "inscription";
        }

        // Chiffrement du mot de passe avant sauvegarde
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        utilisateurRepository.save(utilisateur);

        return "redirect:/connexion?success";
    }
}