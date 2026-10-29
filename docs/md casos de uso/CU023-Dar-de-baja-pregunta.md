# Caso de Uso N.º 023: Dar de baja una Pregunta

- **Nombre:** Dar de baja una Pregunta
- **Nro.:** 023
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Eliminar una pregunta del sistema.

---

## Precondiciones
- Actor autenticado en el sistema.
- La pregunta ya existe en el sistema.

---

## Postcondiciones

### Éxito:
- Los datos de la pregunta son eliminados del sistema.

### Fracaso:
- Los datos de la pregunta quedan como estaban en el sistema.

---

## Flujo Principal
1. El actor desea eliminar una pregunta del sistema y presiona el botón «Eliminar».
2. El sistema presenta el mensaje: *«Los datos de la pregunta Nombre_Pregunta serán eliminados del sistema»* y dos botones: «ELIMINAR» y «CANCELAR».
3. El actor presiona el botón «ELIMINAR». El sistema elimina lógicamente la pregunta y presenta el cartel: *«Los datos de la pregunta Nombre_Pregunta han sido eliminados del sistema.»*.
4. El actor presiona cualquier tecla.
5. El caso de uso finaliza.

---

## Flujos Alternativos

### 3.A. Cancelar la eliminación
- **3.A.1.** El actor presiona el botón «CANCELAR».
- **3.A.2.** El caso de uso finaliza.

---

## Asociaciones

- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales
- Se debe generar un registro de auditoría indicando el usuario que eliminó la pregunta, junto con la fecha y hora de dicha eliminación.

---

## Observaciones
- No se especifican observaciones adicionales.