package proyect.proyectefinal.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MensajeText {
    @JsonProperty("mensaje")
    private String mensage;
    
}
