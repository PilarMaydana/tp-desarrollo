# Migración de Base de Datos: PostgreSQL a H2 en Memoria

**Rama:** `chore/api-h2`  
**Fecha:** 20 de Octubre de 2026  
**Proyecto:** TP Desarrollo – Capit@l Humano API  

---

## 1. Resumen de la Migración

Se migró la persistencia del backend (`api`) reemplazando PostgreSQL (y su dependencia de Docker) por **H2 en memoria**.  
- La base de datos se ejecuta en modo de compatibilidad PostgreSQL (`MODE=PostgreSQL`).
- Se recrea automáticamente en cada arranque de la aplicación a partir de scripts SQL (`schema.sql` y `seed.sql`).
- Se habilitó la consola web interactiva de H2 en `/h2-console` configurada con soporte para frames (`SameOrigin`).
- Se reactivó el test de contexto de Spring (`contextLoads`), logrando 100% de los tests en verde sin requerir ningún servicio externo levantado.

---

## 2. Historial de Commits (Conventional Commits)

Los cambios se organizaron de forma atómica en 5 commits sobre la rama `chore/api-h2`:

| Hash | Mensaje | Descripción |
|---|---|---|
| `e2d1548` | `build(api): reemplaza driver PostgreSQL por H2` | Reemplazo del driver en `pom.xml`. |
| `b805060` | `feat(api): configura H2 y scripts schema.sql y seed.sql` | Configuración de H2 en `application.yml` y creación de `schema.sql` y `seed.sql`. |
| `3b6220f` | `fix(api): habilita consola H2 en SecurityConfig` | Permisos en `SecurityConfig` para `/h2-console/**` y headers de frames. |
| `fcb7468` | `test(api): rehabilita contextLoads` | Eliminación de `@Disabled` en `CapitalhumanoApiApplicationTests`. |
| `1ebfc96` | `docs: actualiza README y elimina referencias a Docker/PostgreSQL` | Creación de `api/README.md`, actualización de `README.md` y borrado de `db/`. |

---

## 3. Detalle de Archivos Modificados, Creados y Eliminados

### 3.1. `api/pom.xml` (Modificado)
- **Cambio:** Se eliminó la dependencia de `org.postgresql:postgresql` y se agregó `com.h2database:h2` con scope `runtime` (sin versión explícita, delegando su gestión al parent starter de Spring Boot).
```diff
-        <!-- Driver PostgreSQL -->
-        <dependency>
-            <groupId>org.postgresql</groupId>
-            <artifactId>postgresql</artifactId>
-            <scope>runtime</scope>
-        </dependency>
+        <!-- Driver H2 (base de datos en memoria) -->
+        <dependency>
+            <groupId>com.h2database</groupId>
+            <artifactId>h2</artifactId>
+            <scope>runtime</scope>
+        </dependency>
```

---

### 3.2. `api/src/main/resources/application.yml` (Modificado)
- **Cambio:**
  - Se eliminaron las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.
  - Se configuró la URL JDBC: `jdbc:h2:mem:capitalhumano;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`.
  - Se configuró usuario `sa` y contraseña vacía.
  - Se mantuvo `jpa.hibernate.ddl-auto: validate` y el `time_zone: America/Argentina/Buenos_Aires`.
  - Se agregaron las directivas `spring.sql.init` para ejecutar `schema.sql` y `seed.sql` en modo `always`.
  - Se activó la consola web en `spring.h2.console.path: /h2-console`.
  - Se preservó la configuración de LDAP embebido (`port: 0`) y Swagger UI.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:capitalhumano;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        jdbc:
          time_zone: America/Argentina/Buenos_Aires

  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/seed.sql

  h2:
    console:
      enabled: true
      path: /h2-console

  # Deshabilitar autoconfiguración de LDAP embebido hasta que se configure en el CU 01
  ldap:
    embedded:
      port: 0

springdoc:
  swagger-ui:
    enabled: true
```

---

### 3.3. `api/src/main/resources/db/schema.sql` (Creado)
- **Cambio:** Archivo para el esquema DDL. Inicializado con comentario de cabecera y sentencia neutra estándar:
```sql
-- Esquema de base de datos (H2 en modo PostgreSQL)
SELECT 1;
```

---

### 3.4. `api/src/main/resources/db/seed.sql` (Creado)
- **Cambio:** Archivo para los datos y semillas DML iniciales:
```sql
-- Datos iniciales / semillas (H2 en modo PostgreSQL)
SELECT 1;
```

---

### 3.5. `api/src/main/java/ar/edu/utn/frsf/g9/capitalhumano/config/SecurityConfig.java` (Modificado)
- **Cambio:** Se permitió el acceso a `/h2-console/**` y se habilitó la visualización de los frames internos de la consola H2 mediante `frameOptions.sameOrigin()`.
```diff
         // TODO: reemplazar por autenticación LDAP al implementar el CU 01
         http
                 .csrf(csrf -> csrf.disable())
+                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                 .authorizeHttpRequests(auth -> auth
+                        .requestMatchers("/h2-console/**").permitAll()
                         .anyRequest().permitAll()
                 );
```

---

### 3.6. `api/src/test/java/ar/edu/utn/frsf/g9/capitalhumano/CapitalhumanoApiApplicationTests.java` (Modificado)
- **Cambio:** Se removió la anotación `@Disabled` y se actualizó el javadoc para verificar la carga del contexto de Spring Boot utilizando la base de datos H2 en memoria.
```diff
-import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;
 import org.springframework.boot.test.context.SpringBootTest;
 
 /**
  * Test de carga del contexto de Spring.
  *
- * <p>Deshabilitado porque requiere una base de datos PostgreSQL activa.
- * Se rehabilita cuando exista el docker-compose con la base de datos.</p>
+ * <p>Verifica que el contexto levante correctamente con la base de datos H2 en memoria.</p>
  */
 @SpringBootTest
-@Disabled("Requiere base de datos PostgreSQL — rehabilitar cuando exista docker-compose")
 class CapitalhumanoApiApplicationTests {
```

---

### 3.7. `api/README.md` (Creado)
- **Cambio:** Documentación técnica del backend:
  - Comandos para levantar (`./mvnw spring-boot:run` y `mvnw.cmd spring-boot:run`).
  - Información de H2 en memoria, ciclo de vida (`schema.sql` y `seed.sql`).
  - URL de acceso a la consola H2 (`http://localhost:8080/h2-console`), JDBC URL (`jdbc:h2:mem:capitalhumano`), usuario (`sa`) y sin contraseña.
  - Enlaces a Swagger UI y Ping.

---

### 3.8. `README.md` (Raíz - Modificado)
- **Cambio:** Se actualizó el diagrama de árbol de directorios quitando `db/` y aclarando el backend con base H2 embebida.

---

### 3.9. `db/` (Eliminado)
- **Cambio:** Se eliminó la carpeta `db/` de la raíz (que contenía únicamente `db/.gitkeep`), dado que los scripts de base de datos ahora pertenecen a `api/src/main/resources/db/`.

---

## 4. Relevamiento Previo de Docker y PostgreSQL

Antes de eliminar o modificar componentes se verificó:
- **`docker-compose.yml`:** No existía en el repositorio.
- **Archivos de variables de entorno (`.env`):** No existían en el repositorio.
- **Referencias en código:**
  - `pom.xml`: driver PostgreSQL (reemplazado por H2).
  - `application.yml`: conexión a PostgreSQL localhost:5432 (reemplazada por URL H2 en memoria).
  - `CapitalhumanoApiApplicationTests`: anotación `@Disabled` y comentario sobre PostgreSQL/docker (rehabilitado).
  - `db/.gitkeep`: carpeta vacía obsoleta (eliminada).

---

## 5. Resultados de la Verificación

### 5.1. Ejecución de Tests (`mvnw.cmd test`)
Se ejecutaron satisfactoriamente los 8 tests unitarios y de integración:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in CapitalhumanoApiApplicationTests (contextLoads)
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in GlobalExceptionHandlerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 5.2. Arranque del Backend (`mvnw.cmd spring-boot:run`)
```
H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:capitalhumano'
Tomcat started on port 8080 (http) with context path '/'
Started CapitalhumanoApiApplication in 5.863 seconds
```

### 5.3. Verificación de Endpoints
- **Health check:** `GET http://localhost:8080/api/ping` → `200 OK` (`{"status":"ok"}`).
- **Documentación OpenAPI:** `GET http://localhost:8080/swagger-ui.html` → `200 OK`.
- **Consola H2:**
  - Acceso web: `GET http://localhost:8080/h2-console` → `200 OK`.
  - Autenticación: `POST /h2-console/login.do` con JDBC URL `jdbc:h2:mem:capitalhumano` y usuario `sa` → `200 OK`.
  - Consulta de prueba: `SELECT 1 AS RESULT;` → `200 OK`.
