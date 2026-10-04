# TP Desarrollo

## Flujo de trabajo con Git

### Ramas

| Rama | Propósito |
|------|-----------|
| `main` | Código estable. **Solo se mergea vía PR con aprobación.** |
| `feat/<nombre>` | Nueva funcionalidad (ej: `feat/login-usuario`) |
| `fix/<nombre>` | Corrección de bug |
| `test/<nombre>` | Agregado o mejora de tests |
| `docs/<nombre>` | Cambios en documentación |
| `chore/<nombre>` | Tareas de mantenimiento (configs, deps, CI) |

### Convenciones de commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>: <descripción corta>
```

**Tipos válidos:**

| Tipo | Cuándo usarlo |
|------|---------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `test` | Agregar o actualizar tests |
| `docs` | Cambios en documentación |
| `chore` | Mantenimiento, dependencias, configs |
| `refactor` | Reestructuración sin cambiar comportamiento |

**Ejemplos:**
```
feat: agregar endpoint de registro de usuario
fix: corregir validación de email duplicado
test: agregar tests para servicio de autenticación
docs: documentar endpoints de la API
chore: actualizar dependencias de Spring Boot
```

### Workflow para cada cambio

```bash
# 1. Crear rama desde main actualizado
git checkout main
git pull origin main
git checkout -b feat/mi-feature

# 2. Trabajar y commitear
git add .
git commit -m "feat: descripción del cambio"

# 3. Pushear la rama
git push -u origin feat/mi-feature

# 4. Crear Pull Request en GitHub → pedir review → squash merge
```

> ⚠️ **Nunca pushear directo a `main`.** Todo cambio entra por PR con al menos 1 aprobación.

## Estructura del proyecto

```
├── api/          # Backend (Spring Boot)
├── frontend/     # Frontend (Next.js)
├── db/           # Scripts de base de datos / seeds
├── docs/         # Documentación del proyecto
└── .github/      # Templates de PR y CI
```
