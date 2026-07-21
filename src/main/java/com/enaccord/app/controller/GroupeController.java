package com.enaccord.app.controller;

import com.enaccord.app.model.*;
import com.enaccord.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/groupes")
public class GroupeController {

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private MembreGroupeRepository membreGroupeRepository;

    @Autowired
    private InvitationGroupeRepository invitationGroupeRepository;

    @Autowired
    private ChansonRepository chansonRepository;

    @Autowired
    private AccordRepository accordRepository; // REQUIS pour charger les accords dans l'éditeur interactif

    // ==========================================
    // 👥 GESTION DES GROUPES & MEMBRES
    // ==========================================

    @GetMapping("/nouveau")
    public String afficherFormulaireCreation() {
        return "creer_groupe";
    }

    @PostMapping("/creer")
    public String creerGroupe(
            @RequestParam("nomDuGroupe") String nom,
            @RequestParam("description") String description,
            @RequestParam("image") String image,
            @RequestParam("emailsInvites") String emailsInvites,
            @AuthenticationPrincipal UserDetails currentUser) {

        Utilisateur createur = utilisateurRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Groupe groupe = new Groupe();
        groupe.setNomDuGroupe(nom);
        groupe.setDescription(description);
        groupe.setImage(image);
        groupeRepository.save(groupe);

        // Ajout immédiat du créateur comme membre actif
        MembreGroupe membreCreateur = new MembreGroupe();
        membreCreateur.setGroupe(groupe);
        membreCreateur.setUtilisateur(createur);
        membreCreateur.setDateCreation(LocalDateTime.now());
        membreGroupeRepository.save(membreCreateur);

        if (emailsInvites != null && !emailsInvites.isEmpty()) {
            String[] emails = emailsInvites.split(",");
            for (String email : emails) {
                Optional<Utilisateur> inviteOpt = utilisateurRepository.findByEmail(email.trim());
                if (inviteOpt.isPresent()) {
                    InvitationGroupe invitation = new InvitationGroupe();
                    invitation.setGroupe(groupe);
                    invitation.setInvite(inviteOpt.get());
                    invitation.setStatut("EN_ATTENTE");
                    invitationGroupeRepository.save(invitation);
                }
            }
        }

        return "redirect:/groupes/mes-groupes";
    }

    @GetMapping("/mes-groupes")
    public String mesGroupes(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<MembreGroupe> mesAdhesions = membreGroupeRepository.findByUtilisateurId(utilisateur.getId());
        model.addAttribute("adhesions", mesAdhesions);

        List<InvitationGroupe> invitations = invitationGroupeRepository.findByInviteEmailAndStatut(utilisateur.getEmail(), "EN_ATTENTE");
        model.addAttribute("invitations", invitations);

        return "mes_groupes";
    }

    @PostMapping("/invitations/{id}/accepter")
    public String accepterInvitation(@PathVariable("id") Long id) {
        InvitationGroupe invitation = invitationGroupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation introuvable"));

        invitation.setStatut("ACCEPTEE");
        invitationGroupeRepository.save(invitation);

        // Dès que c'est accepté, l'utilisateur est lié au groupe
        MembreGroupe nouveauMembre = new MembreGroupe();
        nouveauMembre.setGroupe(invitation.getGroupe());
        nouveauMembre.setUtilisateur(invitation.getInvite());
        nouveauMembre.setDateCreation(LocalDateTime.now());
        membreGroupeRepository.save(nouveauMembre);

        return "redirect:/groupes/mes-groupes";
    }

    @PostMapping("/invitations/{id}/refuser")
    public String refuserInvitation(@PathVariable("id") Long id) {
        InvitationGroupe invitation = invitationGroupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation introuvable"));

        invitation.setStatut("REFUSEE");
        invitationGroupeRepository.save(invitation);

        return "redirect:/groupes/mes-groupes";
    }

    // Afficher les détails du groupe (Membres actifs + Chansons communes)
    @GetMapping("/{id}")
    public String detailGroupe(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Groupe groupe = groupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Sécurité : Vérifier si l'utilisateur connecté est bien membre du groupe
        boolean estMembre = groupe.getMembres().stream()
                .anyMatch(m -> m.getUtilisateur().getEmail().equals(currentUser.getUsername()));

        if (!estMembre) {
            return "redirect:/groupes/mes-groupes?erreur=NonAutorise";
        }

        List<Chanson> chansonsDuGroupe = chansonRepository.findByGroupeId(id);

        model.addAttribute("groupe", groupe);
        model.addAttribute("chansons", chansonsDuGroupe);

        return "detail_groupe";
    }

    // ==========================================
    // 🎵 CRUD DES CHANSONS DU GROUPE
    // ==========================================

    // C - Formulaire d'ajout d'une chanson au groupe
    @GetMapping("/{groupeId}/chansons/nouvelle")
    public String formulaireAjoutChanson(
            @PathVariable("groupeId") Long groupeId,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) {

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Sécurité : Vérification d'accès
        boolean estMembre = groupe.getMembres().stream()
                .anyMatch(m -> m.getUtilisateur().getEmail().equals(currentUser.getUsername()));
        if (!estMembre) {
            return "redirect:/groupes/mes-groupes?erreur=NonAutorise";
        }

        Chanson chanson = new Chanson();
        chanson.setGroupe(groupe);

        model.addAttribute("chanson", chanson);
        model.addAttribute("groupe", groupe);
        model.addAttribute("accordsDisponibles", accordRepository.findAll()); // Envoi des accords pour l'éditeur

        return "formulaire_chanson_groupe"; // Assure-toi que ton fichier s'appelle bien ainsi (ou nouvelle_chanson_groupe)
    }

    // C - Traitement de la création
    @PostMapping("/{groupeId}/chansons/enregistrer")
    public String creerChansonGroupe(
            @PathVariable("groupeId") Long groupeId,
            @ModelAttribute("chanson") Chanson chanson,
            @AuthenticationPrincipal UserDetails currentUser) {

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        Utilisateur auteur = utilisateurRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));

        chanson.setGroupe(groupe);
        chanson.setUtilisateur(auteur); // Qui a ajouté la chanson
        chansonRepository.save(chanson);

        return "redirect:/groupes/" + groupeId;
    }

    // R - Affichage détail (lecture seule) d'une chanson du groupe
    @GetMapping("/{groupeId}/chansons/{chansonId}")
    public String detailChansonGroupe(
            @PathVariable("groupeId") Long groupeId,
            @PathVariable("chansonId") Long chansonId,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) {

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Sécurité : Vérification d'accès
        boolean estMembre = groupe.getMembres().stream()
                .anyMatch(m -> m.getUtilisateur().getEmail().equals(currentUser.getUsername()));
        if (!estMembre) {
            return "redirect:/groupes/mes-groupes?erreur=NonAutorise";
        }

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson introuvable"));

        model.addAttribute("chanson", chanson);
        model.addAttribute("groupe", groupe);
        model.addAttribute("accordsDisponibles", accordRepository.findAll()); // pour le script de transposition

        return "voir_chanson_groupe";
    }
    // U - Formulaire de modification d'une chanson du groupe
    @GetMapping("/{groupeId}/chansons/{chansonId}/modifier")
    public String formulaireModificationChanson(
            @PathVariable("groupeId") Long groupeId,
            @PathVariable("chansonId") Long chansonId,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) {

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Sécurité : Vérification d'accès
        boolean estMembre = groupe.getMembres().stream()
                .anyMatch(m -> m.getUtilisateur().getEmail().equals(currentUser.getUsername()));
        if (!estMembre) {
            return "redirect:/groupes/mes-groupes?erreur=NonAutorise";
        }

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson introuvable"));

        model.addAttribute("chanson", chanson);
        model.addAttribute("groupe", groupe);
        model.addAttribute("accordsDisponibles", accordRepository.findAll()); // Envoi des accords pour l'édition

        return "formulaire_chanson_groupe";
    }

    // U - Traitement de la modification
    @PostMapping("/{groupeId}/chansons/{chansonId}/modifier")
    public String modifierChansonGroupe(
            @PathVariable("groupeId") Long groupeId,
            @PathVariable("chansonId") Long chansonId,
            @ModelAttribute("chanson") Chanson chansonModifiee) {

        Chanson chansonExistante = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson introuvable"));

        // Mise à jour des informations + du contenu ChordPro contenant tes accords modifiés !
        chansonExistante.setTitre(chansonModifiee.getTitre());
        chansonExistante.setArtiste(chansonModifiee.getArtiste());
        chansonExistante.setContenuChordPro(chansonModifiee.getContenuChordPro());

        chansonRepository.save(chansonExistante);

        return "redirect:/groupes/" + groupeId;
    }

    // D - Suppression d'une chanson du groupe
    @PostMapping("/{groupeId}/chansons/{chansonId}/supprimer")
    public String supprimerChansonGroupe(
            @PathVariable("groupeId") Long groupeId,
            @PathVariable("chansonId") Long chansonId) {

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson introuvable"));

        chansonRepository.delete(chanson);

        return "redirect:/groupes/" + groupeId;
    }
}