# Caso de Uso N.º 001: Autenticar usuario

- **Nombre:** Autenticar usuario
- **Nro.:** 001
- **Actores:** Usuario No Autenticado
- **Versión:** 1.1

---

## Objetivo
Realizar la autenticación del usuario y, en función de su perfil, autorizar el acceso a las funcionalidades del sistema que tiene permitidas.

---

## Precondiciones
- Ninguna.

---

## Postcondiciones

### Éxito:
- Aparece la pantalla de trabajo.

### Fracaso:
- Imposibilidad de trabajar en el sistema.

---

## Flujo Principal
1. El actor desea trabajar en el sistema.
2. El sistema presenta la pantalla para autenticar al usuario, solicitando que seleccione, en primer término, el tipo de acceso:
   - Ingresar como usuario consultor.
   - Realizar cuestionario.
3. El actor selecciona la opción «Ingresar como usuario».
4. El usuario ingresa los siguientes datos:
   - Nombre de usuario.
   - Contraseña.
5. El sistema valida que el nombre de usuario y la contraseña sean correctos, comprobando dicha información en el LDAP de la organización.
6. El sistema presenta la pantalla principal y activa todas las opciones correspondientes al perfil Consultor.
7. El caso de uso finaliza.

---

## Flujos Alternativos

### 3.A. Realizar cuestionario
- **3.A.1.** El actor selecciona la opción «Realizar cuestionario».
- **3.A.2.** El usuario ingresa los siguientes datos:
  - Tipo de documento.
  - Número de documento.
  - Clave.
- **3.A.3.** El sistema comprueba que exista un candidato para el tipo y número de documento ingresados, que tenga asociado un cuestionario activo o en proceso y que la clave ingresada corresponda al cuestionario en cuestión.
  - **3.A.3.1. Datos inválidos o cuestionario inexistente:**
    - **3.A.3.1.1.** No existe el candidato, no hay un cuestionario activo o en proceso asociado, o la clave ingresada no corresponde al cuestionario.
    - **3.A.3.1.2.** El sistema muestra el mensaje de error: *«Los datos ingresados no son válidos o no existe un cuestionario para el Candidato»*.
    - **3.A.3.1.3.** El actor cierra la pantalla de error.
    - **3.A.3.1.4.** El sistema blanquea los campos.
    - **3.A.3.1.5.** El caso de uso continúa en el paso 3.A.2.
- **3.A.4.** El sistema presenta la pantalla principal e invoca al *CU026 – Completar Cuestionario*.
- **3.A.5.** El caso de uso continúa en el paso 7 del flujo principal.

### 5.A. Usuario o contraseña inválidos
- **5.A.1.** El usuario o la contraseña ingresados son inválidos.
- **5.A.2.** El sistema muestra el mensaje de error: *«El usuario o la contraseña no son válidos»*.
- **5.A.3.** El actor cierra la pantalla de error.
- **5.A.4.** El sistema blanquea los campos.
- **5.A.5.** El caso de uso continúa en el paso 4 del flujo principal.

---

## Asociaciones

- **Asociaciones de inclusión:** No posee.
- **Asociaciones de extensión:** No posee.

---

## Requerimientos Especiales
- La combinación de teclas `Shift + Tab` permite retroceder al campo anterior.
- Independientemente del estado de la tecla `Bloq Mayús`, el ingreso del identificador de usuario se realiza siempre en mayúsculas.
- La contraseña y la clave se visualizan con asteriscos (`*`).
- El LDAP tiene definida una longitud máxima de 15 caracteres para el identificador de usuario y de 20 caracteres para la contraseña.

---

## Observaciones
- En cualquier flujo del caso de uso, el actor puede presionar el botón «Salir».
- En cualquier flujo del caso de uso, el actor debe poder volver a seleccionar si desea autenticarse como usuario o realizar un cuestionario.
- Cualquier funcionalidad del sistema debe percibirse como una extensión de este caso de uso. Por razones de practicidad, esta situación no ha sido consignada en el diagrama.