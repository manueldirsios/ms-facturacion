package com.gd.facturacion.repository;


import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gd.facturacion.entities.Emisor;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class EmisorRepository {

    private final DynamoDbTable<Emisor> emisorTable;
  
    public EmisorRepository(DynamoDbEnhancedClient enhancedClient) {
        this.emisorTable = enhancedClient.table("Emisor", TableSchema.fromBean(Emisor.class));
    }

    /**
     * Save or update an Emisor entity in DynamoDB.
     *
     * @param emisor the entity to save
     */
    public void save(Emisor emisor) {
        emisorTable.putItem(emisor);
    }

    /**
     * Retrieve an Emisor entity by its ID.
     *
     * @param id the ID of the Emisor
     * @return an Optional containing the Emisor if found, otherwise empty
     */
    public Optional<Emisor> findById(Long id) {
        Emisor emisor = emisorTable.getItem(r -> r.key(k -> k.partitionValue(id)));
        return Optional.ofNullable(emisor);
    }

    /**
     * Delete an Emisor entity by its ID.
     *
     * @param id the ID of the Emisor to delete
     */
    public void deleteById(Long id) {
        emisorTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }
}
