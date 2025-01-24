package com.gd.facturacion.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
public class CypherUtil {
	private CypherUtil(){}

	    public  static PrivateKey loadPrivateKey(String filePath) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException  {
	        // Leer el archivo y eliminar encabezados
	        StringBuilder keyBuilder = new StringBuilder();
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (!line.startsWith("-----")) {
	                    keyBuilder.append(line);
	                }
	            }
	        }

	        // Decodificar base64
	        byte[] keyBytes = Base64.getDecoder().decode(keyBuilder.toString());

	        // Crear la clave privada desde el formato PKCS#8
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        return keyFactory.generatePrivate(keySpec);
	    
	}
	    
	    public static PrivateKey convertStringToPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException  {
	    	   // Eliminar cualquier encabezado o pie de página con un patrón genérico
	        key = key.replaceAll("-----.+?-----", "") // Eliminar cualquier línea de encabezado/pie
	                 .replaceAll("\\s+", "");       // Remover espacios y saltos de línea

	        // Decodificar Base64
	        byte[] keyBytes = Base64.getDecoder().decode(key);

	        // Crear una especificación de llave PKCS8
	        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

	        // Obtener el KeyFactory para RSA
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

	        // Generar el PrivateKey
	        return keyFactory.generatePrivate(spec);
	    }

}
