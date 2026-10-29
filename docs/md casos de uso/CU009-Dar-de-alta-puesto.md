# Caso de Uso N.º 009: Dar de alta Puesto o función

- **Nombre:** Dar de alta Puesto o función
- **Nro.:** 009
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Crear un nuevo puesto o función en el sistema.

---

## Precondiciones
- El actor debe estar autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos del nuevo puesto quedan registrados en el sistema.

### Fracaso:
- La operación no se puede concretar.

---

## Flujo Principal
1. El sistema presenta la pantalla de alta de Puestos con los siguientes datos:
   - Código.
   - Nombre del puesto.
   - Descripción.
   - Empresa.
   - Características del puesto, conformadas por un conjunto de competencias y la ponderación deseada para cada una en dicho puesto.
   - Además, presenta un botón «Aceptar» y otro «Cancelar».
2. El actor ingresa todos los datos solicitados y presiona el botón «Aceptar».
3. El sistema registra el puesto y presenta el mensaje: *«El puesto Nombre_Puesto se ha creado correctamente. ¿Desea cargar otro?»*.
4. El sistema muestra dos botones: «Sí» y «No».
5. El actor selecciona la opción «No».
6. El caso de uso finaliza.

---

## Flujos Alternativos

### 2.A. Datos obligatorios incompletos
- **2.A.1.** El actor no ingresa todos los datos y presiona el botón «Aceptar».
- **2.A.2.** El sistema muestra un mensaje de error que detalla todas las omisiones en las que se ha incurrido, sin ocultar los campos ni los botones.
- **2.A.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 2.B. Código o nombre existente
- **2.B.1.** El código o el nombre ingresado ya existe en el sistema.
- **2.B.2.** El sistema informa al actor sobre la situación.
- **2.B.3.** El caso de uso vuelve al paso 2 del flujo principal.

### 5.A. Cargar otro puesto
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
- Cada competencia puede existir una sola vez para cada puesto.
- Las ponderaciones deben ser valores entre 0 y 10.