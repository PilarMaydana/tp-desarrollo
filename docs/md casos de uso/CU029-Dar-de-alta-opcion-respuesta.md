# Caso de Uso N.º 029: Dar de alta Opción de Respuesta

- **Nombre:** Dar de alta Opción de Respuesta
- **Nro.:** 029
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Crear una nueva opción de respuesta en el sistema.

---

## Precondiciones
- Actor autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos de la nueva opción de respuesta quedan registrados en el sistema.

### Fracaso:
- La operación no se puede concretar.

---

## Flujo Principal
1. El sistema presenta la pantalla de alta de Opción de Respuesta con los siguientes datos:
   - Nombre.
   - Descripción.
   - Lista de Opciones (orden de visualización, descripción).
   - Además, presenta un botón «Aceptar» y otro «Cancelar».
2. El actor ingresa todos los datos solicitados y presiona el botón «Aceptar».
3. El sistema registra la opción de respuesta y presenta el mensaje: *«La opción Nombre_Opción se ha creado correctamente. ¿Desea cargar otra?»*.
4. El sistema muestra dos botones: «Sí» y «No».
5. El actor selecciona la opción «No».
6. El caso de uso finaliza.

---

## Flujos Alternativos

### 2.A. Datos obligatorios incompletos
- **2.A.1.** El actor no ingresa todos los datos requeridos y presiona el botón «Aceptar».
- **2.A.2.** El sistema muestra un mensaje de error detallando todas las omisiones en las que se ha incurrido, sin tapar campos ni botones.
- **2.A.3.** El caso de uso vuelve al punto 2 del flujo principal.

### 2.B. Nombre existente
- **2.B.1.** El nombre ingresado ya existe en el sistema.
- **2.B.2.** El sistema informa al actor sobre la situación.
- **2.B.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 2.C. Orden de visualización no consecutivo
- **2.C.1.** El orden de visualización de las opciones no es consecutivo.
- **2.C.2.** El sistema informa al actor sobre la situación.
- **2.C.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 2.D. Descripción de opción duplicada
- **2.D.1.** La descripción de una opción está ingresada más de una vez dentro de la misma Opción de Respuesta.
- **2.D.2.** El sistema informa al actor sobre la situación.
- **2.D.3.** El caso de uso vuelve al punto 2 del flujo principal.

### 5.A. Cargar otra opción de respuesta
- **5.A.1.** El actor selecciona la opción «Sí».
- **5.A.2.** El caso de uso vuelve al punto 1 del flujo principal.

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
- No se especifican observaciones adicionales.