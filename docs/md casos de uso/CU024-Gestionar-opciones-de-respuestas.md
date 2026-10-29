# Caso de Uso N.º 024: Gestionar Opciones de Respuestas

- **Nombre:** Gestionar Opciones de Respuestas
- **Nro.:** 024
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Este caso de uso permite realizar la gestión de las distintas opciones de respuestas que pueden ser vinculadas a las preguntas, buscándolas para modificarlas o eliminarlas, o bien creando nuevas.

---

## Precondiciones
- Actor autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos de las opciones de respuesta quedan en el estado deseado.

### Fracaso:
- La operación no se puede concretar.

---

## Flujo Principal
1. El actor desea gestionar los datos de las opciones de respuesta y selecciona del menú la opción «Opciones de Respuestas».
2. El sistema presenta la pantalla en blanco para cargar los criterios de búsqueda:
   - Nombre de la opción de respuesta.
3. El actor ingresa datos en el campo «Nombre» y presiona el botón «Buscar» (si presiona el botón «Buscar» sin ingresar datos en el campo nombre, el sistema retorna todas las opciones de respuesta existentes en la base de datos).
4. El sistema presenta una lista de las opciones de respuestas que coinciden con los criterios de búsqueda ingresados. Esta lista contiene:
   - Nombre.
   - Descripción.
5. El actor selecciona una opción de respuesta de la lista.
6. El caso de uso termina.

---

## Flujos Alternativos

### 3.A. El actor presiona el botón «Nuevo» sin haber buscado ninguna opción de respuesta
- **3.A.1.** El sistema invoca al caso de uso *CU029 – Dar de alta Opción de Respuesta*.
- **3.A.2.** El sistema actualiza la grilla y continúa en el paso 3 del flujo normal.

### 5.A. El actor presiona el botón «Modificar»
- **5.A.1.** El sistema invoca al caso de uso *CU030 – Modificar Opción de Respuesta*.
- **5.A.2.** El sistema actualiza la grilla y continúa en el paso 3 del flujo normal.

### 5.B. El actor presiona el botón «Eliminar»
- **5.B.1.** El sistema invoca al caso de uso *CU031 – Dar de Baja Opción de Respuesta*.
- **5.B.2.** El sistema actualiza la grilla y continúa en el paso 3 del flujo normal.

---

## Asociaciones

### Asociaciones de Inclusión:
- No posee.

### Asociaciones de Extensión:
| Caso de Uso | Condición |
| :--- | :--- |
| **CU029 – Dar de alta Opción de Respuesta** | El actor presiona el botón «Nuevo». |
| **CU030 – Modificar Opción de Respuesta** | El actor presiona el botón «Modificar». |
| **CU031 – Dar de baja Opción de Respuesta** | El actor presiona el botón «Eliminar». |

---

## Requerimientos Especiales
- El criterio de búsqueda es «contiene».

---

## Observaciones
- En cualquier flujo del caso de uso, el actor puede presionar el botón «Salir» sin guardar los cambios realizados.