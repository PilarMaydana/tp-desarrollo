# Metodología de Desarrollo E2E con Agentes de IA

Esta guía define el estándar de trabajo para desarrollar casos de uso de punta a punta (Frontend y Backend) utilizando agentes de codificación (IA), garantizando aislamiento, bajo acoplamiento y una integración continua sin fricciones.

---

## 1. Planificación: Grafo de Dependencias
Antes de tirar la primera línea de código, se debe mapear la arquitectura funcional.

*   **Mapeo de Casos de Uso (CU):** Diseñar un grafo con los 14 CU para identificar dependencias lógicas.
*   **Identificar el Core:** Se debe comenzar por el nodo central del dominio (ej. Alta de Puesto), aquel que menos dependencias hacia atrás tenga.
*   **Evitar la Trampa del Login:** **NO** empezar por sistemas de autenticación o bienvenida. El login es infraestructura; el desarrollo debe priorizar el núcleo del modelo de negocio para aportar valor real rápidamente.

---

## 2. Organización del Código: Separación por Proyecto y Feature Slices
Para evitar que el agente de IA sufra pérdida de contexto ("Context Drift") o modifique/rompa código ajeno, el proyecto mantiene **separados los directorios de Backend y Frontend**, pero dentro de cada uno se organiza el código por **Casos de Uso (Features)** en lugar de dispersar la lógica únicamente en capas tecnológicas globales.

No se mezclan frontend y backend dentro de un mismo directorio; cada proyecto vive en su raíz independiente (`api/` y `frontend/`):

```text
tp-desarrollo/
 ├── api/                                          # Proyecto Backend (Spring Boot)
 │    └── src/main/java/.../
 │         └── features/                           # (O paquetes por caso de uso)
 │              └── [nombre-caso-de-uso]/
 │                   ├── controller/               # Endpoints REST del CU
 │                   ├── service/                  # Lógica de negocio del CU
 │                   ├── dao/                      # Repositorios / Acceso a datos del CU
 │                   └── dto/                      # DTOs de entrada y salida del CU
 │
 └── frontend/                                     # Proyecto Frontend (Next.js / React)
      └── [app o features]/
           └── [nombre-caso-de-uso]/
                ├── components/                    # Pantallas y componentes del CU
                ├── services/                      # Llamadas a los endpoints de la API del CU
                ├── hooks/                         # Lógica y estado de la UI del CU
                └── types/                         # Interfaces y tipos TypeScript del CU
```

*Regla para la IA:*
* **Aislamiento por proyecto y caso de uso:** Al solicitarle una tarea al agente de IA, solo se le debe proporcionar el contexto y alcance de la carpeta del caso de uso sobre el que se esté trabajando en ese momento (en `api/` para tareas de backend, o en `frontend/` para tareas de frontend).
* **Desarrollo E2E delimitado:** Si se aborda el flujo completo de un caso de uso, se trabaja en fases delimitadas (primero backend en `api/`, luego frontend en `frontend/`), respetando la separación entre proyectos y sin generar acoplamiento indebido en la estructura de carpetas.

---

## 3. Desarrollo Aislado (CDD y Sandboxing)
Cada caso de uso se desarrolla de forma independiente (Component-Driven Development) antes de unirse al sistema.

### Frontend: La Ruta Sandbox
*   Se crea una ruta de desarrollo oculta (ej. `http://localhost:3000/dev/sandbox`).
*   La pantalla actual se monta temporalmente aquí.
*   Permite probar la UI y sus estados sin depender de la navegación del sistema ni de menús previos.

### Backend: Pruebas sin UI
*   El backend del caso de uso se desarrolla y prueba **sin usar el frontend**.
*   Se utiliza **Swagger** (`springdoc-openapi-ui` en Spring Boot) o archivos **`.http`** en el editor.
*   Se valida que el endpoint cumpla estrictamente con el **Contrato (DTO)** pactado.

---

## 4. Desacoplamiento: Inversión de Dependencias (Stubs)
Cuando un CU necesita datos de otro que aún no existe (ej. saber qué usuario está logueado), **NUNCA** se hardcodean valores directamente en la lógica de la pantalla o del servicio principal. Se aplica la "D" de SOLID.

Se crean "Costuras" o Interfaces intermedias (Stubs):

*   **En Backend:** Crear una interfaz (`ISecurityContext`) y una implementación falsa (`MockSecurityContext` que retorne `ID: 1`). El servicio principal solo depende de la interfaz.
*   **En Frontend:** Crear un servicio de abstracción (`AuthService.getCurrentUser()`) que devuelva un objeto mockeado.
*   **Beneficio:** Cuando la feature real (Login) se desarrolle en el futuro, solo se cambia la implementación de la interfaz/servicio, dejando intactas las pantallas y casos de uso ya terminados.

---

## 5. Integración Iterativa (Evitar el "Big Bang")
Es un antipatrón acumular múltiples pantallas en el Sandbox para integrarlas todas al final (Big Bang Integration). El proceso debe ser estrictamente iterativo utilizando la **Capa de Enrutamiento (Router SPA)**.

**Ciclo de Trabajo:**
1.  **Desarrollar:** Crear la Pantalla A en el `/dev/sandbox`.
2.  **Probar E2E Aislado:** Conectar front y back en el sandbox y verificar que los datos persisten.
3.  **Integrar:** Mover la Pantalla A a su ruta oficial en producción (ej. `/puestos/nuevo`) dentro de `AppRouter.tsx`.
4.  **Limpiar:** Vaciar el sandbox.
5.  **Conectar:** Cuando se cree la Pantalla B, simplemente se invoca una función del router (`navigate('/ruta-pantalla-a')`) para unirlas, basándose enteramente en URLs y no en instanciación directa de componentes.