package com.gd.facturacion.entities.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data
public class EntFailResponse {

	 	@Schema(description = "Codigo de la respuesta.", example = "CodigoEstadoHTTP")
	    private String codigo;
	    @Schema(description = "Descripci�n de la respuesta..", example = "Mensaje")
	    private String mensaje;
	    @Schema(description = "Numero de folio de la operacion.", example = "001-29782-590772-1")
	    private String folio;
	    @Schema(description = "Rutas de ayuda para atender la respuesta",example = "htts:1.1.1/help")
	    private String info;
	    @Schema(description = "Detalles de la operacion", example = "[]")
	    private List<Object> detalles = new ArrayList<>();
	    
	    public EntFailResponse() {
	        this.folio = LocalDateTime.now().getNano()+"";

	    }
   
}
