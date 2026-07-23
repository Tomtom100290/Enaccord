package com.enaccord.app;

import com.enaccord.app.config.SecurityConfig;
import com.enaccord.app.controller.GroupeController;
import com.enaccord.app.model.*;
import com.enaccord.app.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupeController.class)
@Import(SecurityConfig.class)
public class GroupeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private GroupeRepository groupeRepository;
    @MockitoBean private UtilisateurRepository utilisateurRepository;
    @MockitoBean private MembreGroupeRepository membreGroupeRepository;
    @MockitoBean private InvitationGroupeRepository invitationGroupeRepository;
    @MockitoBean private ChansonRepository chansonRepository;
    @MockitoBean private AccordRepository accordRepository;

    private org.springframework.security.core.userdetails.UserDetails getMockUser(String email) {
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("password")
                .roles("USER")
                .build();
    }

    // ==========================================
    // 👥 TESTS : GESTION DES GROUPES & MEMBRES
    // ==========================================

    @Test
    void afficherFormulaireCreation_devraitRetournerLaVue() throws Exception {
        mockMvc.perform(get("/groupes/nouveau").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("creer_groupe"));
    }

    @Test
    void creerGroupe_avecInvitations_devraitToutEnregistrerEtRediriger() throws Exception {
        Utilisateur createur = new Utilisateur();
        createur.setId(1L);
        createur.setEmail("test@enaccord.com");

        Utilisateur invite = new Utilisateur();
        invite.setEmail("invite@enaccord.com");

        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.of(createur));
        when(utilisateurRepository.findByEmail("invite@enaccord.com")).thenReturn(Optional.of(invite));

        mockMvc.perform(post("/groupes/creer")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com")))
                        .param("nomDuGroupe", "Mon Super Groupe")
                        .param("description", "Une description")
                        .param("image", "avatar.png")
                        .param("emailsInvites", "invite@enaccord.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/mes-groupes"));

        verify(groupeRepository, times(1)).save(any(Groupe.class));
        verify(membreGroupeRepository, times(1)).save(any(MembreGroupe.class));
        verify(invitationGroupeRepository, times(1)).save(any(InvitationGroupe.class));
    }

    @Test
    void mesGroupes_devraitRetournerLesAdhesionsEtInvitations() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("test@enaccord.com");

        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.of(utilisateur));
        when(membreGroupeRepository.findByUtilisateurId(1L)).thenReturn(new ArrayList<>());
        when(invitationGroupeRepository.findByInviteEmailAndStatut("test@enaccord.com", "EN_ATTENTE")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/groupes/mes-groupes").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("mes_groupes"))
                .andExpect(model().attributeExists("adhesions"))
                .andExpect(model().attributeExists("invitations"));
    }

    @Test
    void accepterInvitation_devraitMettreAjourLeStatutEtCreerMembre() throws Exception {
        InvitationGroupe invitation = new InvitationGroupe();
        invitation.setId(10L);
        invitation.setGroupe(new Groupe());
        invitation.setInvite(new Utilisateur());

        when(invitationGroupeRepository.findById(10L)).thenReturn(Optional.of(invitation));

        mockMvc.perform(post("/groupes/invitations/10/accepter")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/mes-groupes"));

        verify(invitationGroupeRepository).save(argThat(i -> i.getStatut().equals("ACCEPTEE")));
        verify(membreGroupeRepository, times(1)).save(any(MembreGroupe.class));
    }

    @Test
    void refuserInvitation_devraitMettreAjourLeStatutSansCreerMembre() throws Exception {
        InvitationGroupe invitation = new InvitationGroupe();
        invitation.setId(11L);

        when(invitationGroupeRepository.findById(11L)).thenReturn(Optional.of(invitation));

        mockMvc.perform(post("/groupes/invitations/11/refuser")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/mes-groupes"));

        verify(invitationGroupeRepository).save(argThat(i -> i.getStatut().equals("REFUSEE")));
        verifyNoInteractions(membreGroupeRepository);
    }

    @Test
    void detailGroupe_utilisateurMembre_devraitRetournerLaVue() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@enaccord.com");

        MembreGroupe membre = new MembreGroupe();
        membre.setUtilisateur(utilisateur);
        groupe.setMembres(List.of(membre));

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(chansonRepository.findByGroupeId(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/groupes/1").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("detail_groupe"))
                .andExpect(model().attributeExists("groupe"))
                .andExpect(model().attributeExists("chansons"));
    }

    @Test
    void detailGroupe_utilisateurNonMembre_devraitRedirigerAvecErreur() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        groupe.setMembres(List.of());

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));

        mockMvc.perform(get("/groupes/1").with(user(getMockUser("non-membre@enaccord.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/mes-groupes?erreur=NonAutorise"));
    }

    @Test
    void accesGroupeSansAuthentification_devraitRedirigerVersConnexion() throws Exception {
        mockMvc.perform(get("/groupes/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/connexion"));
    }

    // ==========================================
    // 🎵 TESTS : CRUD DES CHANSONS DU GROUPE
    // ==========================================

    @Test
    void formulaireAjoutChanson_devraitRetournerLaVue() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        Utilisateur u = new Utilisateur();
        u.setEmail("test@enaccord.com");
        MembreGroupe m = new MembreGroupe();
        m.setUtilisateur(u);
        groupe.setMembres(List.of(m));

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(accordRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/groupes/1/chansons/nouvelle").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("formulaire_chanson_groupe"))
                .andExpect(model().attributeExists("chanson"))
                .andExpect(model().attributeExists("groupe"))
                .andExpect(model().attributeExists("accordsDisponibles"));
    }

    @Test
    void creerChansonGroupe_devraitEnregistrerLaChansonEtRediriger() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.of(new Utilisateur()));

        mockMvc.perform(post("/groupes/1/chansons/enregistrer")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com")))
                        .param("titre", "Wonderwall")
                        .param("artiste", "Oasis")
                        .param("contenuChordPro", "[Em]Today is [G]gonna be..."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/1"));

        verify(chansonRepository, times(1)).save(any(Chanson.class));
    }

    @Test
    void detailChansonGroupe_devraitRetournerLaVue() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        Utilisateur u = new Utilisateur();
        u.setEmail("test@enaccord.com");
        MembreGroupe m = new MembreGroupe();
        m.setUtilisateur(u);
        groupe.setMembres(List.of(m));

        Chanson chanson = new Chanson();
        chanson.setId(5L);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(chansonRepository.findById(5L)).thenReturn(Optional.of(chanson));
        when(accordRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/groupes/1/chansons/5").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("voir_chanson_groupe"))
                .andExpect(model().attributeExists("chanson"))
                .andExpect(model().attributeExists("groupe"));
    }

    @Test
    void formulaireModificationChanson_devraitRetournerLaVue() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        Utilisateur u = new Utilisateur();
        u.setEmail("test@enaccord.com");
        MembreGroupe m = new MembreGroupe();
        m.setUtilisateur(u);
        groupe.setMembres(List.of(m));

        Chanson chanson = new Chanson();
        chanson.setId(5L);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(chansonRepository.findById(5L)).thenReturn(Optional.of(chanson));

        mockMvc.perform(get("/groupes/1/chansons/5/modifier").with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("formulaire_chanson_groupe"));
    }

    @Test
    void modifierChansonGroupe_devraitMettreAJourEtRediriger() throws Exception {
        Chanson chansonExistante = new Chanson();
        chansonExistante.setId(5L);

        when(chansonRepository.findById(5L)).thenReturn(Optional.of(chansonExistante));

        mockMvc.perform(post("/groupes/1/chansons/5/modifier")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com")))
                        .param("titre", "Nouveau titre")
                        .param("artiste", "Nouvel artiste")
                        .param("contenuChordPro", "[C]Nouveau contenu"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/1"));

        verify(chansonRepository).save(argThat(c ->
                c.getTitre().equals("Nouveau titre") && c.getContenuChordPro().equals("[C]Nouveau contenu")
        ));
    }

    @Test
    void supprimerChansonGroupe_devraitSupprimerEtRediriger() throws Exception {
        Chanson chanson = new Chanson();
        chanson.setId(5L);

        when(chansonRepository.findById(5L)).thenReturn(Optional.of(chanson));

        mockMvc.perform(post("/groupes/1/chansons/5/supprimer")
                        .with(csrf())
                        .with(user(getMockUser("test@enaccord.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/1"));

        verify(chansonRepository, times(1)).delete(chanson);
    }
}