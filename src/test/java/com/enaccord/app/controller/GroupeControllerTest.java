package com.enaccord.app.controller;

import com.enaccord.app.config.SecurityConfig;
import com.enaccord.app.model.*;
import com.enaccord.app.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // CORRIGÉ
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // COMPATIBLE SPRING BOOT 3.4+
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(GroupeController.class)
@Import(SecurityConfig.class)
class GroupeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private GroupeRepository groupeRepository;
    @MockitoBean private UtilisateurRepository utilisateurRepository;
    @MockitoBean private MembreGroupeRepository membreGroupeRepository;
    @MockitoBean private InvitationGroupeRepository invitationGroupeRepository;
    @MockitoBean private ChansonRepository chansonRepository;
    @MockitoBean private AccordRepository accordRepository;

    @Test
    void creerChansonGroupe_devraitEnregistrerLaChansonEtRediriger() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        groupe.setNomDuGroupe("Les Testeurs");

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(utilisateurRepository.findByEmail("test@enaccord.com"))
                .thenReturn(Optional.of(new Utilisateur()));

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@enaccord.com")
                .password("password")
                .roles("USER")
                .build();

        mockMvc.perform(post("/groupes/1/chansons/enregistrer")
                        .with(csrf())
                        .with(user(userDetails))
                        .param("titre", "Wonderwall")
                        .param("artiste", "Oasis")
                        .param("contenuChordPro", "[Em]Today is [G]gonna be..."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/1"));

        verify(chansonRepository, times(1)).save(any(Chanson.class));
    }

    @Test
    void modifierChansonGroupe_devraitMettreAJourLesChampsEtRediriger() throws Exception {
        Chanson chansonExistante = new Chanson();
        chansonExistante.setId(5L);
        chansonExistante.setTitre("Ancien titre");
        chansonExistante.setArtiste("Ancien artiste");

        when(chansonRepository.findById(5L)).thenReturn(Optional.of(chansonExistante));

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@enaccord.com")
                .password("password")
                .roles("USER")
                .build();

        mockMvc.perform(post("/groupes/1/chansons/5/modifier")
                        .with(csrf())
                        .with(user(userDetails))
                        .param("titre", "Nouveau titre")
                        .param("artiste", "Nouvel artiste")
                        .param("contenuChordPro", "[C]Nouveau contenu"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/1"));

        verify(chansonRepository).save(argThat(c ->
                c.getTitre().equals("Nouveau titre") && c.getArtiste().equals("Nouvel artiste")
        ));
    }

    @Test
    void detailGroupe_utilisateurNonMembre_devraitRedirigerAvecErreur() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(1L);
        groupe.setMembres(List.of());

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("non-membre@enaccord.com")
                .password("password")
                .roles("USER")
                .build();

        mockMvc.perform(get("/groupes/1")
                        .with(user(userDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/mes-groupes?erreur=NonAutorise"));
    }

    @Test
    void accesGroupeSansAuthentification_devraitRedirigerVersConnexion() throws Exception {
        mockMvc.perform(get("/groupes/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connexion"));
    }
}