# Caso de Uso N.º 021: Dar de alta Pregunta

- **Nombre:** Dar de alta Pregunta
- **Nro.:** 021
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Crear una nueva pregunta en el sistema.

---

## Precondiciones
- Actor autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos de la nueva pregunta quedan registrados en el sistema.

### Fracaso:
- La operación no se puede concretar.

---

## Flujo Principal
1. El sistema presenta la pantalla de alta de Preguntas con los siguientes datos:
   - Competencia (selección).
   - Factor (selección).
   - Nombre de Pregunta.
   - Pregunta.
   - Descripción (opcional).
   - Opción de Respuesta (selección).
   - Además, se presentan tres botones: «Generar con IA», «Aceptar» y «Cancelar».
2. El actor ingresa todos los datos solicitados y presiona el botón «Aceptar».
3. El sistema registra la nueva pregunta y presenta el mensaje: *«La Pregunta Nombre_Pregunta se ha creado correctamente. ¿Desea cargar otra?»*.
4. El sistema muestra dos botones: «Sí» y «No».
5. El actor selecciona la opción «No».
6. El caso de uso finaliza.

---

## Flujos Alternativos

### 1.A. Acciones iniciales alternativas
- **1.A.1. Generar con IA:**
  - **1.A.1.1.** El actor presiona el botón «Generar con IA».
  - **1.A.1.2.** Se inicia el caso de uso *CU033 – Generar con IA*.
- **1.A.2. Cancelar:**
  - **1.A.2.1.** El actor presiona el botón «Cancelar».
  - **1.A.2.2.** El flujo redirige al caso de uso *CU020 – Gestionar Preguntas*.

### 2.A. Datos obligatorios incompletos
- **2.A.1.** El actor no ingresa todos los datos y presiona el botón «Aceptar».
- **2.A.2.** El sistema muestra un mensaje de error detallando todas las omisiones en las que se ha incurrido, sin tapar campos ni botones.
- **2.A.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 2.B. Campos con exceso de longitud máxima
- **2.B.1.** Alguno de los campos excede su longitud máxima permitida.
- **2.B.2.** El sistema muestra un mensaje de error detallando todos los campos observados, sin tapar campos ni botones.
- **2.B.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 2.C. Pregunta ya existente
- **2.C.1.** La pregunta ya existe definida para la competencia y factor seleccionados (tanto en el nombre como en la redacción de la pregunta).
- **2.C.2.** El sistema avisa de la situación clarificando cuál es la validación no superada.
- **2.C.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 5.A. Cargar otra pregunta
- **5.A.1.** El actor selecciona la opción «Sí».
- **5.A.2.** El caso de uso vuelve al paso 1 del flujo principal.

---

## Asociaciones

- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales
- La combinación de teclas `Shift + Tab` permite retroceder al campo anterior.
- El clic del mouse y la tecla `Enter` tienen el mismo efecto sobre un botón.
- Independientemente del estado de la tecla `Bloq Mayús`, el ingreso de datos literales se realiza siempre en mayúsculas.

---

## Observaciones
- La validación de existencia se realiza tanto en lo que respecta al nombre de la pregunta como al texto de la pregunta en cuestión.
- Cuando se selecciona una Competencia, el sistema carga dinámicamente todos los Factores correspondientes a dicha competencia.
- Al momento de seleccionar la Opción de Respuesta, se debe mostrar una grilla de Opciones con tres columnas:
  - **Orden** (solo lectura).
  - **Descripción** (solo lectura).
  - **Ponderación** (editable), ordenada por orden.
- Las ponderaciones de las opciones de respuesta deben sumar 10 puntos en total y deben ser valores mayores o iguales a cero (0 o positivas).