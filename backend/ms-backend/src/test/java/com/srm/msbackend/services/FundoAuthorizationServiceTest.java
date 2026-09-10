package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundoAuthorizationServiceTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock FundoRepository fundoRepository;
    @Mock Authentication authentication;
    @InjectMocks FundoAuthorizationService service;

    private Usuario usuario(String role) {
        return new Usuario("U", "u@u", new TipoUsuario(role, role));
    }

    @Test void resolveUsuarioAdministradorEAcesso() {
        Usuario admin = usuario("ADMIN");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("u@u");
        when(usuarioRepository.findByEmailIgnoreCase("u@u")).thenReturn(Optional.of(admin));
        assertThat(service.usuarioAtual(authentication)).isSameAs(admin);
        assertThat(service.ehAdministrador(authentication)).isTrue();
        assertThat(service.podeAcessar(authentication, 1L)).isTrue();
        assertThat(service.podeListar(authentication)).isTrue();
        service.validarAcesso(authentication, 1L);
    }

    @Test void resolveOperadorEPermissoes() {
        Usuario operador = usuario("OPERADOR");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("u@u");
        when(usuarioRepository.findByEmailIgnoreCase("u@u")).thenReturn(Optional.of(operador));
        when(fundoRepository.existsByIdAndUsuarios_Id(1L, null)).thenReturn(false);
        assertThat(service.ehAdministrador(authentication)).isFalse();
        assertThat(service.podeAcessar(authentication, 1L)).isFalse();
        assertThatThrownBy(() -> service.validarAcesso(authentication, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test void extraiIdentificadoresJwtEValidaFalhas() {
        Jwt emailJwt = Jwt.withTokenValue("t").header("alg", "none")
                .claim("email", "jwt@u").subject("sub").build();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(emailJwt);
        assertThat(service.extrairIdentificador(authentication)).isEqualTo("jwt@u");

        Jwt usernameJwt = Jwt.withTokenValue("t").header("alg", "none")
                .claim("preferred_username", "user").subject("sub").build();
        when(authentication.getPrincipal()).thenReturn(usernameJwt);
        assertThat(service.extrairIdentificador(authentication)).isEqualTo("user");

        Jwt subjectJwt = Jwt.withTokenValue("t").header("alg", "none").subject("sub").build();
        when(authentication.getPrincipal()).thenReturn(subjectJwt);
        assertThat(service.extrairIdentificador(authentication)).isEqualTo("sub");

        when(authentication.isAuthenticated()).thenReturn(false);
        assertThatThrownBy(() -> service.extrairIdentificador(authentication))
                .isInstanceOf(AccessDeniedException.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("missing")).thenReturn(Optional.empty());
        when(authentication.getPrincipal()).thenReturn("missing");
        when(authentication.getName()).thenReturn("missing");
        assertThatThrownBy(() -> service.usuarioAtual(authentication))
                .isInstanceOf(AccessDeniedException.class);
    }
}
