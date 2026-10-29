# Caso de Uso N.º 028: Emitir Orden de mérito

- **Nombre:** Emitir Orden de mérito
- **Nro.:** 028
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Emitir para un puesto o función particular el orden de mérito alcanzado por todos los candidatos evaluados.

---

## Precondiciones
- Actor autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- El orden de mérito se emite para el puesto seleccionado.

### Fracaso:
- Imposibilidad de emitir el orden de mérito.

---

## Flujo Principal
1. El actor desea emitir el orden de mérito y selecciona del menú la opción «Orden de Méritos».
2. El sistema presenta la pantalla en blanco para cargar los criterios de búsqueda del puesto para el cual se quiere emitir el orden de mérito:
   - Código.
   - Nombre del Puesto.
   - Empresa.
   - Además, presenta un botón «Buscar» y otro «Cancelar».
3. El actor ingresa datos en al menos uno de los criterios de búsqueda y presiona el botón «Buscar».
4. El sistema presenta una lista de los puestos que coinciden con los criterios de búsqueda ingresados. Esta lista contiene:
   - Código.
   - Nombre del puesto.
   - Empresa.
   - Candidatos.
   - Evaluaciones Completas.
5. El actor se posiciona en la grilla y selecciona un puesto pulsando en «Emitir el orden de mérito».
6. El sistema solicita seleccionar para qué evaluación o evaluaciones se desea emitir el orden de mérito, incluyendo la opción «TODAS».
7. El sistema emite el listado de los candidatos evaluados para el puesto:
   - En el reporte se muestran todos los candidatos asociados a las evaluaciones seleccionadas.
   - El **orden de mérito principal** está conformado por aquellos candidatos cuyos cuestionarios han sido completados y que hayan alcanzado las ponderaciones mínimas definidas para cada competencia en el puesto, ordenados en forma descendente por puntaje obtenido.
   - Se muestran también los candidatos que no hayan alcanzado las ponderaciones mínimas definidas para cada competencia.
   - El resto de los candidatos se agrupan por estado del cuestionario.
   - Por cada candidato se visualiza:
     - Tipo de documento.
     - Número de documento.
     - Apellido.
     - Nombre.
     - Puntaje Obtenido.
     - Datos Adicionales.
8. El actor selecciona «Imprimir».
9. El sistema envía el listado a la impresora.
10. El caso de uso finaliza.

---

## Flujos Alternativos

### 2.A. Cancelar la búsqueda inicial
- **2.A.1.** El actor presiona el botón «Cancelar».
- **2.A.2.** El caso de uso finaliza.

### 6.A. El puesto no posee evaluaciones
- **6.A.1.** El puesto seleccionado no posee evaluaciones asociadas.
- **6.A.2.** El sistema muestra un mensaje indicando dicha situación.
- **6.A.3.** El caso de uso continúa en el paso 4 del flujo principal.

---

## Asociaciones

- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales