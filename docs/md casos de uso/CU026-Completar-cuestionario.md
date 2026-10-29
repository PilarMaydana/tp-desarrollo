# Caso de Uso N.º 026: Completar Cuestionario

- **Nombre:** Completar Cuestionario
- **Nro.:** 026
- **Actores:** Candidato
- **Versión:** 1.1

---

## Objetivo
Realizar o completar el cuestionario creado para un candidato en un puesto dado.

---

## Precondiciones
- Actor autenticado en el sistema.
- Cuestionario existente para el candidato en estado «activo» o «en proceso».
- No se debe haber superado la cantidad máxima de accesos permitidos al usuario. Si es así, el sistema deberá presentar un mensaje indicando la situación y colocar el cuestionario en estado «incompleto».

---

## Postcondiciones

### Éxito:
- El cuestionario ha sido respondido (pasa a estado «completo»).

### Fracaso:
- Imposibilidad de concluir la realización del cuestionario.

---

## Flujo Principal
1. El cuestionario se encuentra en estado «activo». El sistema presenta la pantalla «Cuestionario – Instrucciones», junto con un botón «Aceptar» y otro «Cancelar».
2. El actor lee las instrucciones y presiona el botón «Aceptar».
3. El sistema recupera el cuestionario y las competencias que serán evaluadas:
   - Por cada competencia, recupera los factores asociados.
   - Por cada factor (que disponga de al menos dos preguntas), recupera las preguntas con sus respectivas opciones de respuesta.
   - El sistema asocia al cuestionario las preguntas elegidas al azar (dos por factor) y las agrupa en bloques por pasos.
   - Guarda el cuestionario en la base de datos para garantizar persistencia idéntica en reingresos y registra fecha/hora de inicio.
   - Actualiza el estado del cuestionario a «En Proceso».
4. El sistema presenta la pantalla de Cuestionario (wizard), posicionándose en el bloque (paso del wizard) que aún no ha sido respondido.
5. El actor contesta todas las preguntas que se le presentan en pantalla y presiona el botón «Siguiente» (o «Finalizar» si es el último paso).
6. El sistema valida que el actor haya respondido a todas las preguntas del bloque.
7. El sistema guarda en la base de datos el bloque del cuestionario contestado.
8. Al ser el último paso del wizard:
   - El sistema calcula y guarda los resultados del cuestionario.
   - Presenta un mensaje indicando que el cuestionario ha sido completado con éxito.
   - Actualiza el estado del cuestionario a «Completo».
9. El caso de uso finaliza.

---

## Flujos Alternativos

### 1.A. Tiempo máximo de llenado superado
- **1.A.1.** El sistema detecta que se superó el tiempo máximo permitido para completar el cuestionario desde su inicio.
- **1.A.2.** El sistema cambia el cuestionario a estado «incompleto» y actualiza fecha y hora de la marca.
- **1.A.3.** El sistema informa al usuario sobre la situación.
- **1.A.4.** El caso de uso finaliza.

### 1.B. Tiempo máximo en estado activo superado
- **1.B.1.** El cuestionario superó el tiempo máximo permitido para permanecer en estado «activo» sin ser iniciado.
- **1.B.2.** El sistema coloca el cuestionario en estado «Sin Contestar» y registra fecha y hora.
- **1.B.3.** El sistema informa al usuario sobre la situación.
- **1.B.4.** El caso de uso finaliza.

### 1.C. Reingreso a cuestionario previamente iniciado
- **1.C.1.** El sistema detecta que el actor ya había ingresado previamente y recupera el cuestionario en proceso.
- **1.C.2.** El sistema se posiciona directamente en el bloque que aún no ha sido respondido.
- **1.C.3.** El caso de uso continúa en el paso 5 del flujo principal.

### 2.A. Cancelar en pantalla de instrucciones
- **2.A.1.** El actor presiona el botón «Cancelar».
- **2.A.2.** El caso de uso finaliza.

### 6.A. Preguntas sin responder en el bloque
- **6.A.1.** El actor no respondió a la totalidad de las preguntas del bloque actual.
- **6.A.2.** El sistema muestra un mensaje de error indicando cuáles preguntas no han sido respondidas.
- **6.A.3.** El caso de uso vuelve al paso 5 manteniendo el bloque visible y las respuestas ya marcadas.

### 8.A. Existencia de más bloques de preguntas
- **8.A.1.** El sistema presenta la siguiente pantalla del wizard con un nuevo bloque de preguntas a responder.
- **8.A.2.** El caso de uso continúa en el paso 5 del flujo principal.

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
- **Configuración aleatoria de preguntas:** Cada cuestionario toma aleatoriamente dos preguntas por factor de cada competencia a evaluar. Las preguntas se mezclan aleatoriamente y se distribuyen en bloques por pasos según los parámetros del sistema.
- **Cálculo de tiempo límite:** Se toma la fecha y hora actual y se le resta la fecha y hora del primer ingreso; si la diferencia es menor o igual al tiempo máximo estipulado, el candidato puede continuar respondiendo.
- **Algoritmo de cálculo de puntajes:**
  - *Por cada factor:* Sumar todas las ponderaciones de las opciones seleccionadas para sus preguntas y dividir el resultado por 2.
  - *Por cada competencia:* Sumar los totales obtenidos en sus factores y dividirlos por la cantidad de factores evaluados.
- Las instrucciones del cuestionario y los parámetros generales se obtienen de la configuración del sistema.