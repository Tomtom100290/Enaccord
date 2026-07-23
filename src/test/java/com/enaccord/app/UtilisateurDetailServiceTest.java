package com.enaccord.app.service;

import com.enaccord.app.model.Utilisateur;
import com.enaccord.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurDetailServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurDetailService utilisateurDetailService;

    @Test
    void loadUserByUsername_utilisateurExistant_devraitRetournerUserDetails() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("marie@enaccord.com");
        utilisateur.setMotDePasse("$2a$10$hashFictif");
        utilisateur.setRole("ROLE_MEMBRE");

        when(utilisateurRepository.findByEmail("marie@enaccord.com"))
                .thenReturn(Optional.of(utilisateur));

        UserDetails result = utilisateurDetailService.loadUserByUsername("marie@enaccord.com");

        assertThat(result.getUsername()).isEqualTo("marie@enaccord.com");
        assertThat(result.getPassword()).isEqualTo("$2a$10$hashFictif");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_MEMBRE");
    }

    @Test
    void loadUserByUsername_utilisateurInexistant_devraitLeverUneException() {
        when(utilisateurRepository.findByEmail("inconnu@enaccord.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                utilisateurDetailService.loadUserByUsername("inconnu@enaccord.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inconnu@enaccord.com");
    }
}