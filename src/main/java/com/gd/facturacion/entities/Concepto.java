package com.gd.facturacion.entities;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class Concepto {

	    private long id;
	    private String claveProdServ;
	    private String descripcion;
	    private String unidad;
	    private double cantidad;
	    private double valorUnitario;
	    private double importe;
	    
	    @DynamoDbPartitionKey
			public Long getId() {
				return id;
			}
}
