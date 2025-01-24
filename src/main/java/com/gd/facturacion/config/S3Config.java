package com.gd.facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
        		.region(Region.US_WEST_1) // Especifica la región directamente
        	    .credentialsProvider(ProfileCredentialsProvider.create("default")) // Usa el perfil predeterminado
        	    .build();
    }
}