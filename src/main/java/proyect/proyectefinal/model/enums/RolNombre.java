package proyect.proyectefinal.model.enums;



public enum RolNombre {
    ROL_ADMIN("Administrador con acceso completo"), 
    ROL_USER("Rol de usuario"),
    ROL_PADRE("Rol de padre"),
    ROL_HIJO("Rol de hijo");
    private final String descripcion;

    RolNombre(String descripcion){
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
