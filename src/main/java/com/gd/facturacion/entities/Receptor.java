package com.gd.facturacion.entities;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class Receptor {

    private Long id;
    private String rfc;
    private String nombre;
    private String regimenFiscal;
    private String domicilioFiscal;
    @DynamoDbPartitionKey
  		public Long getId() {
  			return id;
  		}
}
