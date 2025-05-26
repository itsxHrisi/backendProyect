package proyect.proyectefinal.model.dto;

import lombok.*;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresoRequest {
    @NotNull 
    private String  cantidad;
}