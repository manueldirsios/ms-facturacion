package com.gd.facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
* <h1>@Configuration()</h1>
* Clase de configuracion para swagger
* @param   Nada
* @return  Nada
* @author  Manuel Dirsio
* @version 1.0
* @since   31/05/2024
*/
@Configuration
public class SwaggerConfig {     
     @Bean
      public OpenAPI springOpenAPI() {

         
          return new OpenAPI()
                  .info(new Info().title("ms-facturacion")
                  .description("Microservicio para facturacion de ordenes")
                  .version("v0.0.1")
                  .license(new License().name("").url("https://dirsio.mx/")))
                  .externalDocs(new ExternalDocumentation()
                  .description("Sistemas")
                  .url("https://dirsio.mx/"));
      }
}