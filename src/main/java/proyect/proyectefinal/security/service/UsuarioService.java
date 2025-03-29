package proyect.proyectefinal.security.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.repository.UsuarioRepository;

import org.springframework.lang.NonNull;


import java.util.Optional;

@Service
@Transactional //Mantiene la coherencia de la BD si hay varios accesos de escritura concurrentes
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public Optional<UsuarioDb> getByNickname(String nickname){
        return usuarioRepository.findByNickname(nickname);
    }

    public boolean existsByNickname(String nickname){
        return usuarioRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public void save(@NonNull UsuarioDb usuario){
        usuarioRepository.save(usuario);
    }
}