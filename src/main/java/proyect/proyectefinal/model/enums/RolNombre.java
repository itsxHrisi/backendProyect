package proyect.proyectefinal.model.enums;



public enum RolNombre {
    ROL_ADMIN("Administrador con acceso completo"), 
    ROL_USER("Usuario estandar"), 
    ROL_GUITATRRISTA("Rol de guitarrista"),
    ROL_BAJISTA("Rol de bajista"),
    ROL_CANTANTE("Rol de cantante"),
    ROL_BATERIA("Rol de bateria");
    private final String descripcion;

    RolNombre(String descripcion){
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
