package proyect.proyectefinal.security.service;


import java.util.Optional;


import io.micrometer.common.lang.NonNull;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.enums.RolNombre;

public interface RolService {
    public Optional<RolDb> getByRolNombre(RolNombre rolNombre);
    public void save(@NonNull RolDb rol);
    
}
