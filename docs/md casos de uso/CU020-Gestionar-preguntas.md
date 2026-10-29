# Caso de Uso N.º 020: Gestionar Preguntas

- **Nombre:** Gestionar Preguntas
- **Nro.:** 020
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Este caso de uso permite realizar la gestión de preguntas, buscándolas para modificarlas o eliminarlas, o bien creando nuevas preguntas.

---

## Precondiciones
- Actor autenticado en el sistema.
- Las opciones de respuestas deben haber sido previamente cargadas.

---

## Postcondiciones

### Éxito:
- Los datos de las preguntas quedan en el estado deseado.

### Fracaso:
- La operación no se puede concretar.

---

## Flujo Principal
1. El actor desea gestionar los datos de las preguntas y selecciona del menú la opción «Preguntas».
2. El sistema presenta la pantalla en blanco para cargar los criterios de búsqueda:
   - Competencia.
   - Factor.
   - Nombre de Pregunta.
3. El actor ingresa datos en al menos uno de los criterios de búsqueda y presiona el botón «Buscar».
4. El sistema presenta una lista de las preguntas que coinciden con los criterios de búsqueda ingresados. Esta lista contiene:
   - Competencia.
   - Factor.
   - Pregunta.
5. El actor selecciona una pregunta de la lista.
6. El caso de uso termina.

---

## Flujos Alternativos

### 3.A. El actor presiona el botón «Nuevo» sin haber buscado ninguna pregunta
- **3.A.1.** El sistema invoca al caso de uso *CU021 – Dar de alta Pregunta*.
- **3.A.2.** El sistema actualiza la grilla y continúa en el paso 3 del flujo normal.

### 5.A. El actor presiona el botón «Modificar»
- **5.A.1.** El sistema invoca al caso de uso *CU022 – Modificar Pregunta*.
- **5.A.2.** El sistema actualiza la grilla y continúa en el paso 4 del flujo normal.

### 5.B. El actor presiona el botón «Eliminar»
- **5.B.1.** El sistema invoca al caso de uso *CU023 – Dar de baja Pregunta*.
- **5.B.2.** El sistema actualiza la grilla y continúa en el paso 4 del flujo normal.

---

## Asociaciones

### Asociaciones de Inclusión:
- No posee.

### Asociaciones de Extensión:
| Caso de Uso | Condición |
| :--- | :--- |
| **CU021 – Dar de alta Pregunta** | El actor presiona el botón «Nuevo». |
| **CU022 – Modificar Pregunta** | El actor presiona el botón «Modificar». |
| **CU023 – Dar de baja Pregunta** | El actor presiona el botón «Eliminar». |

---

## Requerimientos Especiales
- El criterio de búsqueda es «contiene».

---

## Observaciones
- En cualquier flujo del caso de uso, el actor puede presionar el botón «Salir» sin guardar los cambios realizados.