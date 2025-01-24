package com.gd.facturacion.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gd.facturacion.entities.Factura;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class FacturaRepository {

    private final DynamoDbTable<Factura> facturaTable;

    public FacturaRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.facturaTable = enhancedClient.table("Factura", TableSchema.fromBean(Factura.class));
    }

    public void save(Factura pago) {
    	facturaTable.putItem(pago);
    }

    public Optional<Factura> findById(Long id) {
        return Optional.ofNullable(facturaTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

    public void deleteById(Long id) {
    	facturaTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public List<Factura> findAll() {
        return facturaTable.scan().items().stream().toList();
    }
    
    public List<Factura> findByReferenciaPago(String referenciaPago) {
        // Ejecutar la consulta en el índice secundario
        SdkIterable<Page<Factura>> results = facturaTable.index("ReferenciaPagoIndex")
                .query(QueryConditional.keyEqualTo(k -> k.partitionValue(referenciaPago)));

        // Procesar las páginas y extraer los elementos
        List<Factura> pagos = new ArrayList<>();
        results.forEach(page -> pagos.addAll(page.items()));

        return pagos;
    }
}