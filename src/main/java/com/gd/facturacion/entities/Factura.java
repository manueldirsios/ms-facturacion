package com.gd.facturacion.entities;

import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@XmlRootElement
@Data
@DynamoDbBean
public class Factura {
	    private Long id;

	    private Emisor emisor;

	    private Receptor receptor;

	    private List<Concepto> conceptos;

	    private String fecha;
	    private double total;
	    private String cfdiXml;

	    private String selloDigital;
	    
	    @DynamoDbPartitionKey
		public Long getId() {
			return id;
		}
	    
	   public Factura(long id){
	    	this.setId(id);
	    }
	   public Factura(){}
}
