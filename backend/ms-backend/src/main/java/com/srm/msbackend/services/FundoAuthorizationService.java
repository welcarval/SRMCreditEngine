package com.srm.msbackend.services;

import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.entities.Role;
import com.srm.msbackend.entities.Scope;
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
        return ehAdministrador(usuarioAtual(authentication));
    }

    @Transactional(readOnly = true)
    public boolean temScope(Authentication authentication, String scope) {
        Usuario usuario = usuarioAtual(authentication);
        return java.util.stream.Stream.concat(
                        usuario.getRoles().stream().flatMap(role -> role.getScopes().stream()),
                        usuario.getScopes().stream())
                .map(Scope::getCodigo)
                .anyMatch(scope::equalsIgnoreCase);
    }

    @Transactional(readOnly = true)
    public boolean temScopeEAcessoFundo(Authentication authentication, String scope, Long fundoId) {
        return temScope(authentication, scope) && podeAcessar(authentication, fundoId);
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

    public boolean ehAdministrador(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(Role::getCodigo)
                .anyMatch("ADMIN"::equalsIgnoreCase);
    }
}
