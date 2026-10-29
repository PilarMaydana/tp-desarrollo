# Caso de Uso N.º 025: Evaluar a Candidato

- **Nombre:** Evaluar a Candidato
- **Nro.:** 025
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Evaluar a candidatos asignándoles cuestionarios según una función o puesto específico.

---

## Precondiciones
- Actor autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- Candidatos evaluados (cuestionarios generados en estado «activo» con claves aleatorias).

### Fracaso:
- No se pueden evaluar candidatos.

---

## Flujo Principal
1. El actor desea evaluar a candidatos y selecciona del menú la opción «Evaluar Candidatos».
2. El sistema presenta la pantalla de Búsqueda de Candidatos con los siguientes filtros:
   - Apellido.
   - Nombre.
   - Número de Candidato.
3. El actor completa el/los filtros y el sistema completa la grilla con los candidatos que cumplan con los criterios de búsqueda.
4. El actor selecciona uno o más candidatos y presiona el botón «Agregar».
5. El sistema agrega el/los candidatos en la grilla «Candidatos a evaluar».
6. El actor presiona el botón «Siguiente».
7. El sistema presenta la pantalla de Evaluación de Candidatos.
8. El actor selecciona la función o puesto en la cual desea evaluar a los candidatos seleccionados, mostrando el nombre de la función o puesto y el nombre de la empresa.
9. El sistema presenta a modo informativo la lista de competencias que tiene asociadas la función seleccionada, junto a la ponderación requerida en cada una de ellas.
10. El actor presiona el botón «Siguiente».
11. El sistema presenta la lista de «Candidatos a evaluar» con los siguientes datos:
    - Apellido.
    - Nombre.
    - Tipo documento.
    - Nro. documento.
    - Clave de ingreso.
12. El actor presiona el botón «Finalizar».
13. El sistema guarda la información ingresada y presenta un mensaje al usuario consultando si desea exportar la lista de candidatos con sus claves a un archivo Excel.
14. El actor selecciona que desea exportar los datos de candidatos con sus claves a un archivo Excel.
15. El sistema presenta una pantalla donde el usuario selecciona en qué carpeta del sistema operativo desea guardar el archivo Excel.
16. El actor selecciona la carpeta y presiona el botón «Guardar».
17. El sistema guarda el archivo.
18. El caso de uso finaliza y retorna a la pantalla principal del aplicativo.

---

## Flujos Alternativos

### 4.A. No se han seleccionado candidatos
- **4.A.1.** El sistema presenta un mensaje indicando que se debe seleccionar uno o más candidatos.
- **4.A.2.** El sistema retorna al paso 4 del flujo principal.

### 5.A. Quitar candidatos de la grilla de evaluación
- **5.A.1.** El actor selecciona uno o más candidatos de la grilla de «Candidatos a evaluar» y presiona el botón «Quitar».
- **5.A.2.** El sistema quita los candidatos de la grilla «Candidatos a evaluar».
- **5.A.3.** El sistema retorna al paso 4 del flujo principal.

### 6.A. Candidatos con cuestionarios activos o en proceso
- **6.A.1.** Existen uno o más candidatos que tienen cuestionarios activos o en proceso.
- **6.A.2.** El sistema presenta un mensaje indicando quién o quiénes poseen cuestionarios activos o en proceso y que no pueden ser evaluados nuevamente.
- **6.A.3.** El sistema retorna al paso 4 del flujo principal.

### 9.A. Competencia sin factores válidos para evaluar
- **9.A.1.** Alguna de las competencias que posee la función o puesto no posee al menos un factor que cumpla las condiciones para ser evaluado.
- **9.A.2.** El sistema muestra el/los nombres de las competencias que no pueden ser evaluadas.
- **9.A.3.** El actor presiona «Aceptar».
- **9.A.4.** El caso de uso finaliza y vuelve a la pantalla principal.

### 10.A. No se seleccionó ninguna función
- **10.A.1.** El actor no selecciona ninguna función o puesto.
- **10.A.2.** El sistema presenta un mensaje indicando que se debe seleccionar una función.
- **10.A.3.** El sistema retorna al paso 8 del flujo principal.

### 12.A. Cancelar operación en el paso de confirmación
- **12.A.1.** El actor presiona el botón «Cancelar».
- **12.A.2.** El sistema retorna a la pantalla principal.
- **12.A.3.** El caso de uso finaliza.

### 14.A. No exportar archivo Excel
- **14.A.1.** El actor selecciona que no desea exportar los datos de candidatos con sus claves a un archivo Excel.
- **14.A.2.** El caso de uso finaliza.

### 16.A. Cancelar guardado del archivo Excel
- **16.A.1.** El actor presiona el botón «Cancelar».
- **16.A.2.** El caso de uso continúa en el paso 14 del flujo principal.

---

## Asociaciones

- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales
- Las claves de ingreso se generarán aleatoriamente y deberán contener letras y dígitos, con una longitud de 8 caracteres.
- El formato de nombre del archivo Excel a guardar debe ser: `aaaa-mm-dd-hh-mm-NombreFunción.xls`.
- Al momento de generarse la evaluación, se debe garantizar que los cuestionarios generados para los candidatos se basen en el mismo modelo (competencias, factores, preguntas y opciones de respuesta).
- Cuando se genera la evaluación, se deberán crear los cuestionarios en estado «activo» para cada candidato.

---

## Observaciones
- No se especifican observaciones adicionales.