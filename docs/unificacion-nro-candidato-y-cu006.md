# Unificación de `nro_candidato` y cambio de contrato en CU 006 (Importar Candidatos)

## 1. Contexto y problema original

Hasta este cambio existía una inconsistencia entre casos de uso y un riesgo de colisión al pasar a una base de datos persistente:

1. **Formato heterogéneo de `nro_candidato`:**
   - En los ejemplos, pruebas y carga CSV del **CU 006 (Importar Candidatos)** se aceptaban claves alfanuméricas arbitrarias como `"CAND-001"`.
   - En el **CU 025 (Evaluar a Candidato)** y en `seed.sql` el número de candidato se utilizaba como entero correlativo (`"1"`, `"2"`, ..., `"7"`).
2. **Riesgo de colisiones y sobreescritura en BD persistente:**
   - `ImportarCandidatosService` buscaba candidatos existentes únicamente por `nro_candidato`.
   - Si en un CSV futuro llegaba un candidato nuevo con un número ya utilizado en BD, se sobreescribían los datos personales de otro candidato existente o se violaban los constraints `UNIQUE` (`nro_candidato` y `uk_candidato_documento`).

---

## 2. Decisiones adoptadas

### 2.1. Formato unificado de `nro_candidato` (Opción 1: entero positivo canónico en `VARCHAR`)
- Se mantiene la columna `candidato.nro_candidato` como `VARCHAR(50) UNIQUE` en `schema.sql` (**sin modificar `schema.sql`** ni romper los contratos REST/DTOs existentes).
- Todos los números generados por el sistema siguen el formato de **entero positivo sin ceros a la izquierda** (`"1"`, `"2"`, `"3"`, ...).

### 2.2. Eliminación de `Nro candidato` del CSV del CU 006
- El archivo CSV de importación ya **no incluye la columna `Nro candidato`**.
- **Comportamiento estricto:** si un archivo CSV incluye el encabezado `Nro candidato` (o cualquiera de sus variantes: `Numero candidato`, `Nro de candidato`, `Numero de candidato`), el backend **rechaza el archivo con HTTP `400 Bad Request`** indicando que el número es asignado automáticamente por el sistema.
- Encabezados requeridos (10 columnas separadas por `;`):
  ```text
  Apellido;Nombre;Nro documento;Tipo documento;Fecha de nacimiento;Género;Nacionalidad;Email;Escolaridad;Eliminado
  ```

### 2.3. Clave de identidad y normalización en la importación: `(Tipo documento, Nro documento)`
- La identidad de negocio de un candidato durante la importación pasa a ser el par único **`(tipo_documento, nro_documento)`**, alineado con `CONSTRAINT uk_candidato_documento UNIQUE (tipo_documento, nro_documento)` de `schema.sql`.
- **Reglas de normalización en `CsvCandidatoParser.normalizarNroDocumento`:**
  - Para `DNI`, `LE` y `LC`: se pasa a mayúsculas, se eliminan puntos y espacios (ej. `"35.123.456"` → `"35123456"`) y se exige que contenga únicamente dígitos (`^\d+$`).
  - Para `PP` (Pasaporte): se pasa a mayúsculas y se eliminan espacios (ej. `"ab 123456"` → `"AB123456"`).
- **Control de duplicados intra-archivo:** se rechaza el archivo con HTTP `400 Bad Request` si dos filas contienen el mismo par `(Tipo documento, Nro documento)` luego de normalizar. Dos personas con igual número pero distinto `Tipo documento` (ej. `DNI 35123456` y `PP 35123456`) siguen siendo válidas y distintas.

### 2.4. Asignación automática `MAX + 1` dentro de la transacción (Opción B)
- En `ImportarCandidatosService.importarCandidatos` (dentro de `@Transactional`):
  1. Se consultan en bloque los candidatos existentes por `nro_documento` y se indexan por `(tipoDocumento, nroDocumentoNormalizado)`.
  2. Se calcula el máximo entero actual entre todos los `nro_candidato` no nulos en la tabla (`CandidatoImportRepository.findAllNrosCandidato()`).
     - El cálculo se realiza en Java filtrando valores estrictamente numéricos (`^\d+$`), por lo que es **100 % portable entre H2 y PostgreSQL** y **tolerante a eventuales datos heredados no numéricos** (no falla si existiera un registro legacy `"CAND-001"`).
  3. Al procesar cada fila:
     - **Candidato nuevo (no existe por `(Tipo, Nro documento)`):** el sistema asigna automáticamente `String.valueOf(++ultimoNroCandidato)` (tanto si ingresa activo `Eliminado=False` como si ingresa dado de baja `Eliminado=True`).
     - **Candidato existente (coincide por `(Tipo, Nro documento)`):** se actualizan sus datos personales y se aplica la matriz de estados (`activo` / `baja lógica` / `reactivación`), **conservando intacto su `nro_candidato` original**.

---

## 3. Impacto en el sistema

| Módulo / Archivo | Impacto |
|---|---|
| `api/.../importar_candidatos/dto/FilaCandidatoCsvDTO.java` | Se elimina el campo `nroCandidato`. |
| `api/.../importar_candidatos/service/CsvCandidatoParser.java` | Quita `Nro candidato` de columnas requeridas, lo rechaza si aparece en el encabezado, normaliza `Nro documento` según `Tipo documento` y controla duplicados intra-archivo por `(Tipo, Nro)`. |
| `api/.../importar_candidatos/dao/CandidatoImportRepository.java` | Agrega `findAllByNroDocumentoIn(...)` y `findAllNrosCandidato()`. |
| `api/.../importar_candidatos/service/ImportarCandidatosService.java` | Upsert por `(Tipo, Nro documento)` + cálculo transaccional `MAX + 1` para nuevos + preservación de `nro_candidato` en existentes. |
| `api/.../importar_candidatos/importar-candidatos.http` | Actualizado al formato de 10 columnas y caso de prueba de rechazo ante `Nro candidato`. |
| `api/src/test/resources/candidatos_ejemplo.csv` | Actualizado al formato de 10 columnas sin `Nro candidato`. |
| `api/src/test/.../importar_candidatos/**` | Tests unitarios actualizados + nuevo `ImportarCandidatosIntegrationTest` contra H2 con `seed.sql`. |
| `frontend/.../features/candidatos/components/ImportarCandidatosForm.tsx` | Actualiza `ENCABEZADOS_REQUERIDOS` (sin `Nro candidato`) y la guía desplegable de formatos por columna. |
| **CU 025 (Evaluar a Candidato)** | **Sin cambios necesarios:** sigue mostrando y buscando `nroCandidato` como entero en texto (`"1"`, `"2"`, ..., `"8"`, `"9"`). |
| **CU 001 (Autenticar Usuario - Candidato)** | **Sin cambios necesarios:** autentica por `(tipoDocumento, nroDocumento)` + clave de acceso; la normalización en importación evita inconsistencias por puntos/espacios en el DNI. |
| `api/src/main/resources/db/schema.sql` y `seed.sql` | **Sin cambios necesarios:** `schema.sql` mantiene `nro_candidato VARCHAR(50) UNIQUE` y `uk_candidato_documento`; `seed.sql` ya usa `'1'`..`'7'`. |

---

## 4. Nota para migración futura a alta concurrencia

La estrategia `MAX + 1` dentro de la transacción es simple, no modifica `schema.sql` y cubre sobradamente el escenario operativo normal de importaciones secuenciales por consultores. Si en el futuro el sistema requiriera importaciones concurrentes simultáneas de gran volumen en PostgreSQL, se puede reemplazar el cálculo `MAX + 1` por una `SEQUENCE` dedicada en BD (`nextval`) sin cambiar el contrato del CSV ni la lógica de negocio del resto del sistema.
