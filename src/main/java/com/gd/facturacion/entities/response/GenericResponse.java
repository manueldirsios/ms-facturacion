package com.gd.facturacion.entities.response;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GenericResponse {

	@Schema(description = "Objecto de la respuesta.", example = "{}")
	private Object transaccion;
	@Schema(description = "Mensaje de la respuesta.", example = "Operacion Exitosa")
	private String mensaje;
	@Schema(description = "Codigo de la respuesta.", example = "200")
	private int codigoRespuesta;
	
	public GenericResponse(){
		this.setCodigoRespuesta(0);
		this.setMensaje("Transaccion Exitosa");
	}
	
}
