package com.gd.facturacion.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gd.facturacion.entities.Receptor;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ReceptorRepository {

    private final DynamoDbTable<Receptor> receptorTable;

    public ReceptorRepository(DynamoDbEnhancedClient enhancedClient) {
        this.receptorTable = enhancedClient.table("Receptor", TableSchema.fromBean(Receptor.class));
    }

    /**
     * Save or update a Receptor entity in DynamoDB.
     *
     * @param receptor the entity to save
     */
    public void save(Receptor receptor) {
        receptorTable.putItem(receptor);
    }

    /**
     * Retrieve a Receptor entity by its ID.
     *
     * @param id the ID of the Receptor
     * @return an Optional containing the Receptor if found, otherwise empty
     */
    public Optional<Receptor> findById(Long id) {
        Receptor receptor = receptorTable.getItem(r -> r.key(k -> k.partitionValue(id)));
        return Optional.ofNullable(receptor);
    }

    /**
     * Delete a Receptor entity by its ID.
     *
     * @param id the ID of the Receptor to delete
     */
    public void deleteById(Long id) {
        receptorTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }
}
