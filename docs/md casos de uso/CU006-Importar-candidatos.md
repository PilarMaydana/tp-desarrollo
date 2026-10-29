# Caso de Uso N.º 006: Importar Candidatos

- **Nombre:** Importar Candidatos
- **Nro.:** 006
- **Actores:** Consultor
- **Versión:** 1.1

---

## Objetivo
Permitir la importación de candidatos desde el sistema de gestión de candidatos y CV.

---

## Precondiciones
- El actor debe estar autenticado en el sistema.

---

## Postcondiciones

### Éxito:
- La importación de candidatos se realiza correctamente.

### Fracaso:
- Imposibilidad de importar candidatos.

---

## Flujo Principal
1. El actor desea importar candidatos desde el sistema de gestión de candidatos y CV.
2. El sistema presenta una pantalla donde el usuario puede seleccionar la carpeta del sistema operativo en la que se encuentra el archivo CSV que desea importar.
3. El actor selecciona el archivo CSV que desea importar y presiona el botón «Importar».
4. El sistema realiza la importación de los candidatos y, al finalizar, muestra un mensaje de éxito con las siguientes estadísticas:
   - Cantidad de candidatos creados.
   - Cantidad de candidatos actualizados.
   - Cantidad de candidatos eliminados.
5. El actor presiona el botón «Aceptar».
6. El caso de uso finaliza y el sistema retorna a la pantalla principal de la aplicación.

---

## Flujos Alternativos
- No se especifican flujos alternativos en el caso de uso original.

---

## Asociaciones
- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales
- El archivo CSV que se genere debe contener, en la primera fila, los nombres de las columnas que se van a importar.
- El archivo CSV debe contener las siguientes columnas, separadas por punto y coma (`;`):
  - Nro. candidato.
  - Apellido.
  - Nombre.
  - Nro. de documento.
  - Tipo de documento (`DNI`, `LE`, `LC`, `PP`).
  - Fecha de nacimiento (`AAAA-MM-DD`).
  - Género (`H`/`M`).
  - Nacionalidad.
  - Email.
  - Escolaridad.
  - Eliminado (`True`/`False`).
- Dado que la importación puede tardar algunos segundos, se debe mostrar un reloj o una barra de progreso mientras dure la operación.

---

## Observaciones
- En cualquier paso del caso de uso, el actor puede cancelar la operación y retornar a la pantalla principal.
- El texto original de las observaciones se encuentra incompleto, por lo que no es posible recuperar el resto de su contenido.