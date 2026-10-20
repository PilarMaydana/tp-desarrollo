# Backend - Capit@l Humano API

API REST desarrollada con Spring Boot y base de datos H2 en memoria.

## Cómo levantar

Para iniciar la aplicación:

En Linux / macOS:
```bash
./mvnw spring-boot:run
```

En Windows:
```cmd
mvnw.cmd spring-boot:run
```

### Base de datos

- La base de datos es **H2 en memoria**, por lo que los datos se recrean en cada arranque ejecutando automáticamente `schema.sql` y `seed.sql` ubicados en `src/main/resources/db/`.
- La consola web de H2 está habilitada en: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL:** `jdbc:h2:mem:capitalhumano`
  - **Usuario:** `sa`
  - **Contraseña:** *(vacía / sin contraseña)*

### Documentación de la API

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Endpoint de verificación (Ping):** [http://localhost:8080/api/ping](http://localhost:8080/api/ping)
