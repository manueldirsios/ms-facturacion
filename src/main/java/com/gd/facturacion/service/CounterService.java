package com.gd.facturacion.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Service
public class CounterService {

    private final DynamoDbClient dynamoDbClient;

    public CounterService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public long getNextId(String counterName) {
        String tableName = "Counters";

        // Clave de la tabla para el contador
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(counterName).build());

        // Valor por defecto si el contador no existe
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":increment", AttributeValue.builder().n("1").build());
        expressionAttributeValues.put(":start", AttributeValue.builder().n("1").build());

        // Actualiza el contador y devuelve el nuevo valor
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET counterValue = if_not_exists(counterValue, :start) + :increment")
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues("UPDATED_NEW")
                .build();

        // Ejecutar la actualización
        UpdateItemResponse response = dynamoDbClient.updateItem(request);

        // Devolver el nuevo valor del contador
        return Long.parseLong(response.attributes().get("counterValue").n());
    }
}