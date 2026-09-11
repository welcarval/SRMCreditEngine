package com.srm.msbackend.services;

import com.srm.msbackend.entities.Role;
import com.srm.msbackend.entities.Scope;
import com.srm.msbackend.entities.TipoUsuario;
import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.UsuarioModel;
import com.srm.msbackend.models.UsuarioAcessoModel;
import com.srm.msbackend.repositories.TipoUsuarioRepository;
import com.srm.msbackend.repositories.RoleRepository;
import com.srm.msbackend.repositories.ScopeRepository;
import com.srm.msbackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final ScopeRepository scopeRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RoleRepository roleRepository,
                          ScopeRepository scopeRepository, TipoUsuarioRepository tipoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.scopeRepository = scopeRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAcessoModel> listar() {
        return usuarioRepository.findAll().stream().map(this::toAcessoModel).toList();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario salvar(UsuarioModel model) {
        Usuario usuario = new Usuario(model.nome(), model.email(), buscarRoles(model));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> atualizar(Long id, UsuarioModel model) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(model.nome());
            usuario.setEmail(model.email());
            usuario.setRoles(buscarRoles(model));
            return usuarioRepository.save(usuario);
        });
    }

    public boolean deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            return false;
        }

            usuarioRepository.deleteById(id);
            return true;
        }

        @Transactional(readOnly = true)
        public UsuarioAcessoModel toAcessoModel(Usuario usuario) {
            return new UsuarioAcessoModel(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getRoles().stream().map(Role::getCodigo).sorted().toList(),
                    java.util.stream.Stream.concat(
                            usuario.getRoles().stream().flatMap(role -> role.getScopes().stream()),
                            usuario.getScopes().stream())
                            .map(Scope::getCodigo)
                            .distinct()
                            .sorted()
                            .toList(),
                    usuario.getFundos().stream().map(fundo -> fundo.getId()).toList(),
                    usuario.getScopes().stream().map(Scope::getCodigo).sorted().toList()
            );
        }

    private java.util.Set<Role> buscarRoles(UsuarioModel model) {
        if (model.roleIds() != null && !model.roleIds().isEmpty()) {
            if (roleRepository == null) {
                throw new IllegalStateException("Repositório de roles não configurado");
            }
            java.util.List<Role> roles = roleRepository.findAllById(model.roleIds());
            if (roles.size() != model.roleIds().stream().distinct().count()) {
                throw new IllegalArgumentException("Uma ou mais roles não foram encontradas");
            }
            return new java.util.HashSet<>(roles);
        }

        if (model.tipoId() != null && tipoUsuarioRepository != null) {
            TipoUsuario tipo = tipoUsuarioRepository.findById(model.tipoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Role não encontrada para o tipo legado: " + model.tipoId()));
            return new java.util.HashSet<>(java.util.Set.of(new Role(tipo.getCodigo(), tipo.getDescricao())));
        }

        return new java.util.HashSet<>();
    }

    @Transactional
    public Optional<UsuarioAcessoModel> atualizarScopes(Long id, List<Long> scopeIds) {
        List<Long> ids = scopeIds == null ? List.of() : scopeIds;
        List<Scope> scopes = scopeRepository.findAllById(ids);
        if (scopes.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("Uma ou mais permissões não foram encontradas");
        }
        return usuarioRepository.findById(id).map(usuario -> {
            if (usuario.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getCodigo()))) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "As permissões de administradores não podem ser alteradas");
            }
            usuario.setScopes(new java.util.HashSet<>(scopes));
            return toAcessoModel(usuarioRepository.save(usuario));
        });
    }
}
