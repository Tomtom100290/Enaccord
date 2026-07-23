package com.enaccord.app;

import com.enaccord.app.config.SecurityConfig;
import com.enaccord.app.controller.ChansonController;
import com.enaccord.app.model.Chanson;
import com.enaccord.app.model.Groupe;
import com.enaccord.app.model.Utilisateur;
import com.enaccord.app.repository.AccordRepository;
import com.enaccord.app.repository.ChansonRepository;
import com.enaccord.app.repository.GroupeRepository;
import com.enaccord.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChansonController.class)
@Import(SecurityConfig.class)
public class ChansonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ChansonRepository chansonRepository;
    @MockitoBean private GroupeRepository groupeRepository;
    @MockitoBean private UtilisateurRepository utilisateurRepository;
    @MockitoBean private AccordRepository accordRepository;

    private org.springframework.security.core.userdetails.UserDetails getMockUser() {
        return org.springframework.security.core.userdetails.User
                .withUsername("test@enaccord.com")
                .password("password")
                .roles("USER")
                .build();
    }


    // 🎵 TESTS : FORMULAIRE DE CRÉATION

    @Test
    void nouvelleChansonForm_sansGroupe_devraitRetournerLaVueAvecChansonVide() throws Exception {
        mockMvc.perform(get("/chansons/nouvelle")
                        .with(user(getMockUser()))
                        .flashAttr("invitations", new ArrayList<>()) // Répare le premier crash Thymeleaf
                        .flashAttr("adhesions", new ArrayList<>()))  // 👈 Répare le second crash Thymeleaf
                .andExpect(status().isOk())
                .andExpect(view().name("formulaire_chanson"))
                .andExpect(model().attributeExists("chanson"))
                .andExpect(model().attribute("groupeId", (Object) null));
    }

    @Test
    void nouvelleChansonForm_avecGroupeExistant_devraitLierLeGroupe() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setId(42L);
        when(groupeRepository.findById(42L)).thenReturn(Optional.of(groupe));

        mockMvc.perform(get("/chansons/nouvelle")
                        .param("groupeId", "42")
                        .with(user(getMockUser()))
                        .flashAttr("invitations", new ArrayList<>()) // Répare le premier crash Thymeleaf
                        .flashAttr("adhesions", new ArrayList<>()))  // 👈 Répare le second crash Thymeleaf
                .andExpect(status().isOk())
                .andExpect(view().name("formulaire_chanson"))
                .andExpect(model().attributeExists("chanson"))
                .andExpect(model().attribute("groupeId", 42L));
    }

    // ==========================================
    // 🎵 TESTS : ENREGISTREMENT
    // ==========================================

    @Test
    void enregistrerChanson_sansGroupe_devraitSauvegarderEtRedirigerVersAccueil() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@enaccord.com");
        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.of(utilisateur));

        mockMvc.perform(post("/chansons/enregistrer")
                        .with(csrf())
                        .with(user(getMockUser()))
                        .param("titre", "Imagine")
                        .param("artiste", "John Lennon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(chansonRepository, times(1)).save(any(Chanson.class));
    }

    @Test
    void enregistrerChanson_avecGroupe_devraitSauvegarderEtRedirigerVersLeGroupe() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@enaccord.com");

        Groupe groupe = new Groupe();
        groupe.setId(99L);

        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.of(utilisateur));
        when(groupeRepository.findById(99L)).thenReturn(Optional.of(groupe));

        mockMvc.perform(post("/chansons/enregistrer")
                        .with(csrf())
                        .with(user(getMockUser()))
                        .param("groupeId", "99")
                        .param("titre", "Time")
                        .param("artiste", "Pink Floyd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groupes/99"));

        verify(chansonRepository).save(argThat(chanson ->
                chanson.getGroupe() != null && chanson.getGroupe().getId() == 99L
        ));
    }

    @Test
    void enregistrerChanson_utilisateurIntrouvable_devraitLeverException() {
        when(utilisateurRepository.findByEmail("test@enaccord.com")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/chansons/enregistrer")
                    .with(csrf())
                    .with(user(getMockUser()))
                    .param("titre", "Error Song"));
        });
    }
}