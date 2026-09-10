package com.srm.msbackend.services;

import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.repositories.FundoRepository;
import com.srm.msbackend.repositories.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FundoAuthorizationService {
    private final UsuarioRepository usuarioRepository;
    private final FundoRepository fundoRepository;

    public FundoAuthorizationService(UsuarioRepository usuarioRepository, FundoRepository fundoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.fundoRepository = fundoRepository;
    }

    @Transactional(readOnly = true)
    public Usuario usuarioAtual(Authentication authentication) {
        String email = extrairIdentificador(authentication);
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não cadastrado"));
    }

    @Transactional(readOnly = true)
    public boolean ehAdministrador(Authentication authentication) {
        return "ADMIN".equalsIgnoreCase(usuarioAtual(authentication).getTipo().getCodigo());
    }

    @Transactional(readOnly = true)
    public boolean podeAcessar(Authentication authentication, Long fundoId) {
        Usuario usuario = usuarioAtual(authentication);
        return ehAdministrador(usuario)
                || fundoRepository.existsByIdAndUsuarios_Id(fundoId, usuario.getId());
    }

    @Transactional(readOnly = true)
    public boolean podeListar(Authentication authentication) {
        usuarioAtual(authentication);
        return true;
    }

    @Transactional(readOnly = true)
    public void validarAcesso(Authentication authentication, Long fundoId) {
        Usuario usuario = usuarioAtual(authentication);
        if (ehAdministrador(usuario)) {
            return;
        }

        if (!fundoRepository.existsByIdAndUsuarios_Id(fundoId, usuario.getId())) {
            throw new AccessDeniedException("Usuário não possui acesso a este fundo");
        }
    }

    public String extrairIdentificador(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Autenticação obrigatória");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }

            String username = jwt.getClaimAsString("preferred_username");
            if (username != null && !username.isBlank()) {
                return username;
            }

            return jwt.getSubject();
        }

        return authentication.getName();
    }

    private boolean ehAdministrador(Usuario usuario) {
        return usuario.getTipo() != null
                && "ADMIN".equalsIgnoreCase(usuario.getTipo().getCodigo());
    }
}
