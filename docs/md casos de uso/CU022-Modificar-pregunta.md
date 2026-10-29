# Caso de Uso N.º 022: Modificar Pregunta

- **Nombre:** Modificar Pregunta
- **Nro.:** 022
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Modificar datos de una pregunta existente en el sistema.

---

## Precondiciones
- Actor autenticado en el sistema.
- La pregunta ya existe en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos de la pregunta quedan modificados en el sistema.

### Fracaso:
- Los datos de la pregunta quedan como estaban en el sistema.

---

## Flujo Principal
1. El actor desea modificar los datos de una pregunta. El sistema presenta la pantalla con los datos precargados de la pregunta a modificar:
   - Competencia (solo lectura).
   - Factor (solo lectura).
   - Nombre de Pregunta.
   - Pregunta.
   - Descripción (opcional).
   - Opción de Respuesta (selección).
   - Además, presenta un botón «Aceptar» y otro «Cancelar».
2. El actor modifica los datos que desea cambiar y presiona el botón «Aceptar».
3. El sistema actualiza la pregunta y presenta el mensaje: *«La operación ha culminado con éxito»*.
4. El caso de uso finaliza.

---

## Flujos Alternativos

### 2.A. Datos obligatorios incompletos o blanqueados
- **2.A.1.** El actor blanquea uno o más datos obligatorios y presiona el botón «Aceptar».
- **2.A.2.** El sistema muestra un mensaje de error detallando todas las omisiones en las que se ha incurrido, sin tapar campos ni botones.
- **2.A.3.** El caso de uso vuelve al punto 2 del flujo principal.

### 2.B. Campos con exceso de longitud máxima
- **2.B.1.** Alguno de los campos excede su longitud máxima permitida.
- **2.B.2.** El sistema muestra un mensaje de error detallando todos los campos observados, sin tapar campos ni botones.
- **2.B.3.** El caso de uso vuelve al punto 2 del flujo principal.

### 2.C. Pregunta ya existente
- **2.C.1.** La pregunta ya existe definida para esa competencia y factor (tanto en el nombre como en el texto de la pregunta).
- **2.C.2.** El sistema avisa de la situación clarificando cuál es la validación no superada.
- **2.C.3.** El caso de uso vuelve al punto 2 del flujo principal con foco en el campo «Nombre de Pregunta».

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
- Las ponderaciones de las opciones de respuesta deben sumar 10 puntos en total y deben ser valores mayores o iguales a cero (0 o positivas).
- Cuando se selecciona una Competencia, el sistema carga todos los Factores correspondientes a dicha competencia.
- Al momento de seleccionar la Opción de Respuesta, se debe mostrar una grilla de Opciones con tres columnas:
  - **Orden** (solo lectura).
  - **Descripción** (solo lectura).
  - **Ponderación** (editable), ordenada por orden.