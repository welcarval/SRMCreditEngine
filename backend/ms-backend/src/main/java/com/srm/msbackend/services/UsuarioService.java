package com.srm.msbackend.services;

import com.srm.msbackend.entities.TipoUsuario;
import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.UsuarioModel;
import com.srm.msbackend.repositories.TipoUsuarioRepository;
import com.srm.msbackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TipoUsuarioRepository tipoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario salvar(UsuarioModel model) {
        TipoUsuario tipo = buscarTipo(model.tipoId());
        Usuario usuario = new Usuario(model.nome(), model.email(), tipo);
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> atualizar(Long id, UsuarioModel model) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(model.nome());
            usuario.setEmail(model.email());
            usuario.setTipo(buscarTipo(model.tipoId()));
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

    private TipoUsuario buscarTipo(Long tipoId) {
        return tipoUsuarioRepository.findById(tipoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de usuário não encontrado: " + tipoId));
    }
}
