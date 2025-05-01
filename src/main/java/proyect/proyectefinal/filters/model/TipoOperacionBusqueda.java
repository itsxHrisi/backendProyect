package proyect.proyectefinal.filters.model;

public enum TipoOperacionBusqueda {
    IGUAL("="),
    CONTIENE("LIKE"),
    MAYOR_QUE(">"),
    MENOR_QUE("<"),
    EMPIEZA("^");

    private final String simbolo;

    TipoOperacionBusqueda(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public static TipoOperacionBusqueda deSimbolo(String simbolo) {
        for (TipoOperacionBusqueda operacion : values()) {
            if (operacion.simbolo.equals(simbolo)) {
                return operacion;
            }
        }
        throw new IllegalArgumentException("Símbolo de operación no válido: " + simbolo);
    }
}
