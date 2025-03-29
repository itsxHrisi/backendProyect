package proyect.proyectefinal.security.service.impl;



import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.annotation.Nonnull;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.enums.RolNombre;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.security.service.RolService;





@Service
@Transactional
public class RolServiceImpl implements RolService {

    @Autowired
    RolRepository rolRepository;

    @Override
    public Optional<RolDb> getByRolNombre(RolNombre rolNombre) {
        return rolRepository.findByNombre(rolNombre);
    }

    @Override
    public void save(@Nonnull RolDb rol) {
        rolRepository.save(rol);
    }
    
}
