# ms-facturacion

## Prerequisitos

- Java (17)
- Maven(4.5+)
- LOOMBOK
- AWS SERVERLESS CONTAINER
- AWS DYNAMODB
## Configuracion

El proyecto esta desarrollado en arquitectura de microservicios implementa `Spring boot 3.3.5`  `spring-boot-starter-web` y  `Core 6.1.1`

## Estructura de Proyecto

| Modulo                                         | Contenido                                                                                                                                                                               |
| ---------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Application.java**				 | Clase principal de arranque y escaneo del proyecto
| **SwaggerConfig.java**				 | Clase de configuracion para la ui y documentacion de servicios con de swagger http://localhost:8080/ms-facturacion/swagger-ui/index.html
| **pom.xml**                                    | Dependencias, Compilacion y empaquetado                                                                                                                                                                           
**TransaccionExceptionHandler.java**                          | Clase manejadora de excepciones


## Dependencias con otros aplicativos
| Satelite		                       | RECURSO                 | Tipo de recurso                                   |Estructura de la solicitud       |
| ------------------------------------ | -----------------------| --------------------------------------------------| --------------------------------|
| **POR DEFINIR**             	   |http://test    | SERVICIO REST  						     			    | JSON REQUEST|   
| **POR DEFINIR**             	   |https://tes    | SERVICIO REST                  |JSON REQUEST|						                                                          




## Despliegue de aplicacion

Se desplega en AMAZON LAMBDA.

| Instancia             | Puerto | Build                                     | Run                                             |
| --------------------- | ------ | ----------------------------------------- | ----------------------------------------------- |
| **MS-FACTURACION**| 8080   | ./mvn clean install       | tomcat embebido                            |


[![AGPL License](https://img.shields.io/badge/license-AGPL-blue.svg)](https://www.dirsio.mx/)

![Logo](https://web-dirsio.s3.us-west-1.amazonaws.com/favicon.ico)
