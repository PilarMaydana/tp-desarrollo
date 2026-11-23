-- Datos iniciales / semillas (H2 en modo PostgreSQL)

INSERT INTO competencia (codigo, nombre, descripcion) VALUES
    ('COM-01', 'COMUNICACIÓN', 'CAPACIDAD PARA TRANSMITIR IDEAS E INFORMACIÓN DE MANERA CLARA Y EFECTIVA.'),
    ('LID-01', 'LIDERAZGO', 'CAPACIDAD PARA GUIAR, MOTIVAR Y DIRIGIR EQUIPOS DE TRABAJO HACIA EL LOGRO DE OBJETIVOS.'),
    ('TRAB-01', 'TRABAJO EN EQUIPO', 'CAPACIDAD PARA COLABORAR DE MANERA EFICAZ CON OTRAS PERSONAS PARA EL LOGRO DE METAS COMUNES.'),
    ('RESOL-01', 'RESOLUCIÓN DE PROBLEMAS', 'CAPACIDAD PARA IDENTIFICAR PROBLEMAS COMPLEJOS Y EVALUAR OPCIONES DE SOLUCIÓN.'),
    ('TEC-01', 'COMPETENCIAS TÉCNICAS', 'CAPACIDAD PARA APLICAR CONOCIMIENTOS TÉCNICOS EN EL DESARROLLO DE SOFTWARE.');

INSERT INTO factor (codigo, nombre, descripcion, nro_orden, competencia_id, eliminado) VALUES
    ('FAC-01', 'COMUNICACIÓN ORAL', 'CAPACIDAD PARA TRANSMITIR INFORMACIÓN DE MANERA VERBAL CLARA.', 1, 1, FALSE),
    ('FAC-02', 'COMUNICACIÓN ESCRITA', 'CAPACIDAD PARA REDACTAR DOCUMENTOS DE MANERA ESTRUCTURADA.', 2, 1, FALSE),
    ('FAC-03', 'DESARROLLO DE SOFTWARE', 'CAPACIDAD PARA DISEÑAR Y PROGRAMAR SOLUCIONES DE SOFTWARE.', 1, 5, FALSE),
    ('FAC-04', 'COLABORACIÓN EN EQUIPO', 'CAPACIDAD PARA COOPERAR Y COORDINAR TAREAS CON OTROS INTEGRANTES.', 1, 3, FALSE),
    ('FAC-05', 'ANÁLISIS DE CAUSA RAÍZ', 'CAPACIDAD PARA DIAGNOSTICAR EL ORIGEN DE PROBLEMAS COMPLEJOS.', 1, 4, FALSE),
    ('FAC-06', 'GESTIÓN DE EQUIPOS', 'CAPACIDAD PARA COORDINAR Y MOTIVAR PERSONAS HACIA METAS COMUNES.', 1, 2, FALSE),
    ('FAC-07', 'ARQUITECTURA EN LA NUBE', 'CAPACIDAD PARA DISEÑAR SISTEMAS DISTRIBUIDOS Y ESCALABLES.', 2, 5, FALSE),
    ('FAC-08', 'VISIÓN ESTRATÉGICA', 'CAPACIDAD PARA ALINEAR DECISIONES TÉCNICAS CON OBJETIVOS DE NEGOCIO (1 SOLA PREGUNTA PARA PROBAR EXCLUSIÓN EN CU 026).', 2, 2, FALSE);

INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('DEV-01', 'DESARROLLADOR SENIOR', 'DESARROLLO Y ARQUITECTURA DE SOFTWARE.', 'TECH SOLUTIONS', FALSE),
    ('QA-01', 'ANALISTA QA', 'CONTROL Y ASEGURAMIENTO DE CALIDAD DE SOFTWARE.', 'TECH SOLUTIONS', FALSE),
    ('ARQ-01', 'ARQUITECTO DE SOFTWARE', 'DISEÑO DE ARQUITECTURAS ESCALABLES Y LIDERAZGO TÉCNICO.', 'INNOVATECH SA', FALSE);

INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    (1, 1, 8.0),
    (1, 3, 9.0),
    (2, 1, 7.5),
    (2, 4, 8.5),
    (3, 5, 9.5),
    (3, 2, 8.0);

INSERT INTO pregunta (nombre, enunciado, descripcion, factor_id, eliminado) VALUES
    ('PREGUNTA POO', '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?', 'CONCEPTOS BÁSICOS', 3, FALSE),
    ('PATRONES DE DISEÑO', '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?', 'PREGUNTA DE ARQUITECTURA', 3, FALSE),
    ('EXPRESIÓN VERBAL', '¿CON QUÉ FRECUENCIA EXPRESA SUS IDEAS CON CLARIDAD EN REUNIONES DE EQUIPO?', 'EVALÚA CLARIDAD EN PRESENTACIONES ORALES', 1, FALSE),
    ('ESCUCHA ACTIVA', '¿ESCUCHA ATENTAMENTE Y CONFIRMA COMPRENSIÓN ANTES DE RESPONDER?', 'EVALÚA CAPACIDAD DE ESCUCHA EN DIÁLOGOS', 1, FALSE),
    ('REDACCIÓN TÉCNICA', '¿REDACTA DOCUMENTACIÓN TÉCNICA CLARA Y SIN AMBIGÜEDADES?', 'EVALÚA CLARIDAD EN TEXTOS TÉCNICOS', 2, FALSE),
    ('SÍNTESIS ESCRITA', '¿RESUME INFORMACIÓN COMPLEJA DE MANERA ESTRUCTURADA EN INFORMES?', 'EVALÚA CAPACIDAD DE SÍNTESIS ESCRITA', 2, FALSE),
    ('PRINCIPIOS SOLID', '¿CUÁL ES EL OBJETIVO DEL PRINCIPIO DE RESPONSABILIDAD ÚNICA?', 'PREGUNTA DE DISEÑO LIMPIO', 3, FALSE),
    ('COOPERACIÓN INTERÁREA', '¿COLABORA PROACTIVAMENTE CON OTROS INTEGRANTES ANTE BLOQUEOS DEL EQUIPO?', 'EVALÚA PREDISPOSICIÓN A COLABORAR', 4, FALSE),
    ('RESPUESTA A FEEDBACK', '¿INCORPORA SUGERENCIAS DEL EQUIPO DE MANERA CONSTRUCTIVA?', 'EVALÚA APERTURA AL FEEDBACK', 4, FALSE),
    ('DISTRIBUCIÓN DE TAREAS', '¿COORDINA CON SUS PARES PARA EQUILIBRAR LA CARGA DE TRABAJO?', 'EVALÚA COORDINACIÓN GRUPAL', 4, FALSE),
    ('DIAGNÓSTICO DE INCIDENTES', '¿IDENTIFICA LA CAUSA RAÍZ ANTES DE APLICAR UN PARCHE TEMPORAL?', 'ÚNICA PREGUNTA DEL FACTOR PARA PROBAR FLUJO 9.A', 5, FALSE),
    ('DELEGACIÓN EFECTIVA', '¿DELEGA RESPONSABILIDADES CON OBJETIVOS Y PLAZOS CLAROS?', 'EVALÚA CAPACIDAD DE DELEGACIÓN', 6, FALSE),
    ('MOTIVACIÓN DE EQUIPO', '¿RECONOCE LOGROS Y FOMENTA EL DESARROLLO PROFESIONAL DEL EQUIPO?', 'EVALÚA LIDERAZGO POSITIVO', 6, FALSE),
    ('ARQUITECTURA MICROSERVICIOS', '¿CUÁNDO CONVIENE SEPARAR UN SISTEMA EN MICROSERVICIOS INDEPENDIENTES?', 'EVALÚA CRITERIO DE ARQUITECTURA DISTRIBUIDA', 7, FALSE),
    ('TOLERANCIA A FALLOS', '¿CÓMO DISEÑA UN SERVICIO PARA CONTINUAR OPERANDO ANTE FALLOS PARCIALES?', 'EVALÚA RESILIENCIA EN SISTEMAS EN LA NUBE', 7, FALSE),
    ('PLANIFICACIÓN TECNOLÓGICA', '¿ALINEA EL ROADMAP TÉCNICO CON LAS PRIORIDADES ESTRATÉGICAS DEL NEGOCIO?', 'ÚNICA PREGUNTA DEL FACTOR 8 PARA PROBAR EXCLUSIÓN EN CU 026', 8, FALSE);

INSERT INTO pregunta_opcion_respuesta (pregunta_id, nombre, descripcion, nro_orden, ponderacion) VALUES
    (1, 'OPCION A', 'UN PARADIGMA BASADO EN OBJETOS', 1, 10.0),
    (1, 'OPCION B', 'UN LENGUAJE DE PROGRAMACIÓN', 2, 0.0),
    (2, 'INSTANCIA ÚNICA', 'GARANTIZA UNA ÚNICA INSTANCIA', 1, 10.0),
    (2, 'MÚLTIPLES INSTANCIAS', 'CREA MUCHAS INSTANCIAS', 2, 0.0),
    (3, 'NUNCA', 'NUNCA', 1, 0.0),
    (3, 'A VECES', 'A VECES', 2, 4.0),
    (3, 'SIEMPRE', 'SIEMPRE', 3, 6.0),
    (4, 'EN DESACUERDO', 'EN DESACUERDO', 1, 1.0),
    (4, 'NEUTRAL', 'NEUTRAL', 2, 3.0),
    (4, 'DE ACUERDO', 'DE ACUERDO', 3, 6.0),
    (5, 'NUNCA', 'NUNCA', 1, 0.0),
    (5, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (6, 'NUNCA', 'NUNCA', 1, 0.0),
    (6, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (7, 'UNA ÚNICA RAZÓN DE CAMBIO', 'UNA CLASE DEBE TENER UNA SOLA RESPONSABILIDAD', 1, 10.0),
    (7, 'HERENCIA MÚLTIPLE', 'PERMITE HEREDAR DE VARIAS CLASES', 2, 0.0),
    (8, 'NUNCA', 'NUNCA', 1, 0.0),
    (8, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (9, 'NUNCA', 'NUNCA', 1, 0.0),
    (9, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (10, 'NUNCA', 'NUNCA', 1, 0.0),
    (10, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (11, 'NUNCA', 'NUNCA', 1, 0.0),
    (11, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (12, 'NUNCA', 'NUNCA', 1, 0.0),
    (12, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (13, 'NUNCA', 'NUNCA', 1, 0.0),
    (13, 'SIEMPRE', 'SIEMPRE', 2, 10.0),
    (14, 'SIEMPRE DESDE EL INICIO', 'SEPARAR TODO EN MICROSERVICIOS DESDE EL PRIMER DÍA', 1, 0.0),
    (14, 'SEGÚN DOMINIO Y ESCALA', 'CUANDO LOS LÍMITES DE DOMINIO Y ESCALABILIDAD LO JUSTIFICAN', 2, 6.0),
    (14, 'EVALUANDO COSTO-BENEFICIO', 'CON ANÁLISIS DE COMPLEJIDAD OPERATIVA Y EQUIPOS', 3, 4.0),
    (15, 'SIN CONTROLES', 'ESPERAR QUE EL SERVICIO SE RECUPERE MANUALMENTE', 1, 1.0),
    (15, 'REINTENTOS BÁSICOS', 'APLICAR REINTENTOS SIMPLES SIN LÍMITES', 2, 3.0),
    (15, 'CIRCUIT BREAKER Y FALLBACK', 'APLICAR TIMEOUTS, CIRCUIT BREAKER Y DEGRADACIÓN CONTROLADA', 3, 6.0),
    (16, 'NUNCA', 'NUNCA', 1, 0.0),
    (16, 'SIEMPRE', 'SIEMPRE', 2, 10.0);

INSERT INTO opcion_respuesta (nombre, descripcion) VALUES
    ('FRECUENCIA', 'ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO'),
    ('ACUERDO', 'ESCALA DE GRADO DE ACUERDO CON LA AFIRMACIÓN'),
    ('FRECUENCIA SEMANAL', 'ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS'),
    ('SATISFACCIÓN', 'ESCALA DE NIVEL DE SATISFACCIÓN GENERAL');

INSERT INTO item_opcion_respuesta (opcion_respuesta_id, orden, descripcion) VALUES
    (1, 1, 'NUNCA'),
    (1, 2, 'A VECES'),
    (1, 3, 'SIEMPRE'),
    (2, 1, 'EN DESACUERDO'),
    (2, 2, 'NEUTRAL'),
    (2, 3, 'DE ACUERDO'),
    (3, 1, 'NINGUNA VEZ'),
    (3, 2, 'UNA A DOS VECES'),
    (3, 3, 'TRES O MÁS VECES'),
    (4, 1, 'INSATISFECHO'),
    (4, 2, 'NEUTRO'),
    (4, 3, 'SATISFECHO'),
    (4, 4, 'MUY SATISFECHO');

INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    ('1', 'DNI', '12345678', 'PÉREZ', 'JUAN', 'juan.perez@example.com', FALSE),
    ('2', 'DNI', '87654321', 'GÓMEZ', 'MARÍA', 'maria.gomez@example.com', FALSE),
    ('3', 'DNI', '11223344', 'LÓPEZ', 'CARLOS', 'carlos.lopez@example.com', FALSE),
    ('4', 'DNI', '30456789', 'FERNÁNDEZ', 'ANA', 'ana.fernandez@example.com', FALSE),
    ('5', 'DNI', '32987654', 'MARTÍNEZ', 'LUCAS', 'lucas.martinez@example.com', FALSE),
    ('6', 'PP', '40112233', 'RODRÍGUEZ', 'SOFÍA', 'sofia.rodriguez@example.com', FALSE),
    ('7', 'DNI', '28999111', 'SÁNCHEZ', 'DIEGO', 'diego.sanchez@example.com', FALSE);

INSERT INTO evaluacion (puesto_id, fecha_creacion, consultor_id, tiempo_maximo_minutos, cantidad_preguntas_por_bloque) VALUES
    (1, CURRENT_TIMESTAMP, 1, 60, 5),
    (3, DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 1, 60, 3);

INSERT INTO cuestionario (candidato_id, puesto_id, evaluacion_id, estado, clave_acceso, fecha_creacion, fecha_cambio_estado, fecha_ultimo_ingreso, cantidad_accesos, tiempo_maximo_minutos, cantidad_preguntas_por_bloque, tiempo_restante_segundos, eliminado) VALUES
    (1, 1, 1, 'ACTIVO', 'CLAVE123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0, 60, 5, 3600, FALSE),
    -- Caso (g) CU 026: Cuestionario perteneciente a OTRO candidato (candidato 2 -> 404 por pertenencia para candidato 1)
    (2, 1, 1, 'EN_PROCESO', 'TEST4567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 60, 5, 2400, FALSE),
    (3, 1, 1, 'COMPLETO', 'COMP1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 60, 5, 0, FALSE),
    (6, 1, 1, 'INCOMPLETO', 'INCO1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 60, 5, 0, FALSE),
    (7, 1, 1, 'SIN_CONTESTAR', 'SINC1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0, 60, 5, 3600, FALSE);

-- ============================================================================
-- Cuestionarios de prueba para CU 026 (los cuestionarios reales los crea CU 025)
-- Todos pertenecen al candidato 1 (JUAN PÉREZ) sobre el puesto 3 (ARQUITECTO DE SOFTWARE):
--   Competencias: TEC-01 (FAC-03 con 3 preg, FAC-07 con 2 preg) y LID-01 (FAC-06 con 2 preg, FAC-08 con 1 preg -> excluido)
--   ID 6  -> (a) ACTIVO vigente con bloques ya generados por CU 025 (2 bloques de 3 preguntas, excluye FAC-08)
--   ID 7  -> (b) EN_PROCESO vigente con bloque 1 respondido (y respuestas guardadas) y bloque 2 pendiente (reingreso 1.C)
--   ID 8  -> (c) ACTIVO vencido (fecha_creacion > 72h atrás -> flujo 1.B)
--   ID 9  -> (d) EN_PROCESO vencido (fecha_inicio > 60 min atrás -> flujo 1.A)
--   ID 10 -> (e) ACTIVO con cantidad_accesos = maxAccesos (3 -> límite de accesos)
--   ID 11 -> (f) COMPLETO (estado final no disponible)
--   ID 2  -> (g) Cuestionario de candidato 2 (ajeno -> 404)
--   ID 12 -> (h) EN_PROCESO vigente con UN solo bloque pendiente (6 preguntas: 2 por factor válido) para probar cierre y cálculo
-- ============================================================================
INSERT INTO cuestionario (candidato_id, puesto_id, evaluacion_id, estado, clave_acceso, fecha_creacion, fecha_cambio_estado, fecha_inicio, fecha_ultimo_ingreso, fecha_finalizacion, cantidad_accesos, tiempo_maximo_minutos, cantidad_preguntas_por_bloque, tiempo_restante_segundos, ultimo_bloque_respondido, eliminado) VALUES
    (1, 3, 2, 'ACTIVO', 'CU26ACT1', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', -2, CURRENT_TIMESTAMP), NULL, NULL, NULL, 0, 60, 3, 3600, NULL, FALSE),
    (1, 3, 2, 'EN_PROCESO', 'CU26PRO1', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), NULL, 1, 60, 3, 2700, 1, FALSE),
    (1, 3, 2, 'ACTIVO', 'CU26VEN1', DATEADD('HOUR', -80, CURRENT_TIMESTAMP), DATEADD('HOUR', -80, CURRENT_TIMESTAMP), NULL, NULL, NULL, 0, 60, 3, 3600, NULL, FALSE),
    (1, 3, 2, 'EN_PROCESO', 'CU26VPR1', DATEADD('HOUR', -5, CURRENT_TIMESTAMP), DATEADD('MINUTE', -90, CURRENT_TIMESTAMP), DATEADD('MINUTE', -90, CURRENT_TIMESTAMP), DATEADD('MINUTE', -90, CURRENT_TIMESTAMP), NULL, 1, 60, 3, 0, NULL, FALSE),
    (1, 3, 2, 'ACTIVO', 'CU26ACC1', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', -1, CURRENT_TIMESTAMP), NULL, NULL, NULL, 3, 60, 3, 3600, NULL, FALSE),
    (1, 3, 2, 'COMPLETO', 'CU26CMP1', DATEADD('HOUR', -10, CURRENT_TIMESTAMP), DATEADD('HOUR', -8, CURRENT_TIMESTAMP), DATEADD('HOUR', -9, CURRENT_TIMESTAMP), DATEADD('HOUR', -9, CURRENT_TIMESTAMP), DATEADD('HOUR', -8, CURRENT_TIMESTAMP), 1, 60, 3, 0, 2, FALSE),
    (1, 3, 2, 'EN_PROCESO', 'CU26CIE1', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), NULL, 1, 60, 6, 3000, NULL, FALSE);

-- Bloques pregenerados (como los crea CU 025) para los cuestionarios de prueba ID 6 (ACTIVO), ID 7 (EN_PROCESO) e ID 12 (EN_PROCESO con 1 bloque)
INSERT INTO bloque_cuestionario (cuestionario_id, nro_bloque, respondido, fecha_respuesta) VALUES
    (6, 1, FALSE, NULL),
    (6, 2, FALSE, NULL),
    (7, 1, TRUE, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)),
    (7, 2, FALSE, NULL),
    (12, 1, FALSE, NULL);

-- Preguntas asignadas (2 por factor válido: FAC-03 -> preg 1,2; FAC-07 -> preg 14,15; FAC-06 -> preg 12,13; excluye FAC-08 -> preg 16)
INSERT INTO pregunta_asignada (bloque_cuestionario_id, pregunta_id, nro_orden) VALUES
    -- Cuestionario ID 6 (ACTIVO vigente), bloque 1 (ID 1) y bloque 2 (ID 2)
    (1, 1, 1),
    (1, 14, 2),
    (1, 12, 3),
    (2, 2, 4),
    (2, 15, 5),
    (2, 13, 6),
    -- Cuestionario ID 7 (EN_PROCESO vigente), bloque 1 respondido (ID 3) y bloque 2 pendiente (ID 4)
    (3, 1, 1),
    (3, 12, 2),
    (3, 14, 3),
    (4, 2, 4),
    (4, 13, 5),
    (4, 15, 6),
    -- Cuestionario ID 12 (h: EN_PROCESO vigente con 1 bloque pendiente, ID 5)
    -- Preguntas asignadas IDs 13..18: FAC-03 (preg 1,2), FAC-07 (preg 14,15), FAC-06 (preg 12,13)
    (5, 1, 1),
    (5, 2, 2),
    (5, 14, 3),
    (5, 15, 4),
    (5, 12, 5),
    (5, 13, 6);

-- Respuestas ya guardadas para el bloque 1 del cuestionario ID 7 (preguntas asignadas IDs 7, 8, 9)
INSERT INTO respuesta_candidato (pregunta_asignada_id, opcion_respuesta_id, fecha_respuesta) VALUES
    (7, 1, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)),
    (8, 26, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)),
    (9, 30, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP));

-- ============================================================================
-- datos de prueba del CU 28 (Emitir Orden de mérito)
-- ============================================================================
-- Puestos (IDs 4 y 5): comparten la palabra 'INGENIERO' y tienen empresas distintas.
--   Puesto 4 (Puesto A): INGENIERO DE DATOS (DATA CORP SA) -> 2 evaluaciones (1 de 2 completas, 10 cuestionarios).
--     Competencias requeridas: COM-01 (id 1) >= 7.0 y TEC-01 (id 5) >= 8.0.
--   Puesto 5 (Puesto B): INGENIERO DE PLATAFORMA CLOUD (NUBE SUR SRL) -> 0 evaluaciones (flujo 6.A).
INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('ING-DATA-01', 'INGENIERO DE DATOS', 'DISEÑO Y CONSTRUCCIÓN DE PIPELINES DE DATOS Y ANALÍTICA.', 'DATA CORP SA', FALSE),
    ('ING-CLOUD-01', 'INGENIERO DE PLATAFORMA CLOUD', 'ADMINISTRACIÓN DE INFRAESTRUCTURA Y AUTOMATIZACIÓN EN LA NUBE.', 'NUBE SUR SRL', FALSE);

-- Características de Puesto A (IDs 7 y 8) y Puesto B (IDs 9 y 10)
INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    (4, 1, 7.0),
    (4, 5, 8.0),
    (5, 5, 8.5),
    (5, 2, 7.0);

-- Candidatos nuevos del CU 28 (IDs 8..17; nro_candidato no numérico para no alterar MAX+1 de CU 006)
INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    ('CU28-01', 'DNI', '35100001', 'ALVAREZ', 'VALENTINA', 'valentina.alvarez@example.com', FALSE),
    ('CU28-02', 'DNI', '35100002', 'BENITEZ', 'MARTIN', 'martin.benitez@example.com', FALSE),
    ('CU28-03', 'DNI', '35100003', 'CASTRO', 'JULIAN', 'julian.castro@example.com', FALSE),
    ('CU28-04', 'DNI', '35100004', 'DIAZ', 'CAMILA', 'camila.diaz@example.com', FALSE),
    ('CU28-05', 'DNI', '35100005', 'ESPINOZA', 'TOMAS', 'tomas.espinoza@example.com', FALSE),
    ('CU28-06', 'LE', '24100006', 'FLORES', 'PAULA', 'paula.flores@example.com', FALSE),
    ('CU28-07', 'DNI', '35100007', 'GARCIA', 'NICOLAS', 'nicolas.garcia@example.com', FALSE),
    ('CU28-08', 'PP', 'AR800008', 'HERRERA', 'FLORENCIA', 'florencia.herrera@example.com', FALSE),
    ('CU28-09', 'DNI', '35100009', 'IBARRA', 'MATEO', 'mateo.ibarra@example.com', FALSE),
    ('CU28-10', 'DNI', '35100010', 'JUAREZ', 'AGUSTINA', 'agustina.juarez@example.com', FALSE);

-- Evaluaciones del Puesto A (IDs 3 y 4):
--   Evaluación 3 (más antigua): todos sus cuestionarios COMPLETO (3 de 3 -> evaluación completa).
--   Evaluación 4 (más reciente): mezcla de estados (3 COMPLETO + 1 INCOMPLETO + 1 EN_PROCESO + 1 SIN_CONTESTAR + 1 ACTIVO).
INSERT INTO evaluacion (puesto_id, fecha_creacion, consultor_id, tiempo_maximo_minutos, cantidad_preguntas_por_bloque) VALUES
    (4, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 1, 60, 5),
    (4, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 1, 60, 5);

-- Cuestionarios del Puesto A (IDs 13..22):
--   ID 13 (cand 8,  eval 3): COMPLETO - puntaje 9.50 (alcanza todos los mínimos; posición 1)
--   ID 14 (cand 9,  eval 3): COMPLETO - puntaje 8.50 (alcanza todos; empata puntaje con ID 15 -> desempate BENITEZ < CASTRO)
--   ID 15 (cand 10, eval 3): COMPLETO - puntaje 8.50 (alcanza todos; empata puntaje con ID 14 -> posición 3)
--   ID 16 (cand 11, eval 4): COMPLETO - puntaje 7.50 (borde exacto >= en ambas competencias: COM 7.00 = 7.0, TEC 8.00 = 8.0)
--   ID 17 (cand 12, eval 4): COMPLETO - puntaje 7.50 (no alcanza 1 competencia: TEC-01 6.50 < 8.0)
--   ID 18 (cand 13, eval 4): COMPLETO - puntaje 5.75 (no alcanza 2 competencias: COM-01 5.50 < 7.0 y TEC-01 6.00 < 8.0)
--   ID 19 (cand 14, eval 4): INCOMPLETO (fechaInicio, fechaUltimoIngreso, 2 accesos)
--   ID 20 (cand 15, eval 4): EN_PROCESO (fechaInicio, fechaUltimoIngreso, 1 acceso)
--   ID 21 (cand 16, eval 4): SIN_CONTESTAR (sin fechaInicio ni fechaUltimoIngreso, 0 accesos)
--   ID 22 (cand 17, eval 4): ACTIVO (sin fechaInicio ni fechaUltimoIngreso, 0 accesos)
INSERT INTO cuestionario (candidato_id, puesto_id, evaluacion_id, estado, clave_acceso, fecha_creacion, fecha_cambio_estado, fecha_inicio, fecha_ultimo_ingreso, fecha_finalizacion, cantidad_accesos, tiempo_maximo_minutos, cantidad_preguntas_por_bloque, tiempo_restante_segundos, ultimo_bloque_respondido, eliminado) VALUES
    (8,  4, 3, 'COMPLETO',      'CU28CMP1', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', 35, DATEADD('DAY', -9, CURRENT_TIMESTAMP)), DATEADD('DAY', -9, CURRENT_TIMESTAMP),  DATEADD('DAY', -9, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 35, DATEADD('DAY', -9, CURRENT_TIMESTAMP)), 1, 60, 5, 0, 2, FALSE),
    (9,  4, 3, 'COMPLETO',      'CU28CMP2', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', 45, DATEADD('DAY', -8, CURRENT_TIMESTAMP)), DATEADD('DAY', -8, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 15, DATEADD('DAY', -8, CURRENT_TIMESTAMP)), DATEADD('MINUTE', 45, DATEADD('DAY', -8, CURRENT_TIMESTAMP)), 2, 60, 5, 0, 2, FALSE),
    (10, 4, 3, 'COMPLETO',      'CU28CMP3', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', 40, DATEADD('DAY', -7, CURRENT_TIMESTAMP)), DATEADD('DAY', -7, CURRENT_TIMESTAMP),  DATEADD('DAY', -7, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 40, DATEADD('DAY', -7, CURRENT_TIMESTAMP)), 1, 60, 5, 0, 2, FALSE),
    (11, 4, 4, 'COMPLETO',      'CU28CMP4', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 50, DATEADD('HOUR', -40, CURRENT_TIMESTAMP)), DATEADD('HOUR', -40, CURRENT_TIMESTAMP), DATEADD('HOUR', -40, CURRENT_TIMESTAMP), DATEADD('MINUTE', 50, DATEADD('HOUR', -40, CURRENT_TIMESTAMP)), 1, 60, 5, 0, 2, FALSE),
    (12, 4, 4, 'COMPLETO',      'CU28CMP5', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 42, DATEADD('HOUR', -38, CURRENT_TIMESTAMP)), DATEADD('HOUR', -38, CURRENT_TIMESTAMP), DATEADD('MINUTE', 10, DATEADD('HOUR', -38, CURRENT_TIMESTAMP)), DATEADD('MINUTE', 42, DATEADD('HOUR', -38, CURRENT_TIMESTAMP)), 2, 60, 5, 0, 2, FALSE),
    (13, 4, 4, 'COMPLETO',      'CU28CMP6', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('MINUTE', 30, DATEADD('HOUR', -37, CURRENT_TIMESTAMP)), DATEADD('HOUR', -37, CURRENT_TIMESTAMP), DATEADD('HOUR', -37, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('HOUR', -37, CURRENT_TIMESTAMP)), 1, 60, 5, 0, 2, FALSE),
    (14, 4, 4, 'INCOMPLETO',    'CU28INC1', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('HOUR', -34, CURRENT_TIMESTAMP), DATEADD('HOUR', -36, CURRENT_TIMESTAMP), DATEADD('HOUR', -35, CURRENT_TIMESTAMP), NULL, 2, 60, 5, 0, 1, FALSE),
    (15, 4, 4, 'EN_PROCESO',    'CU28PRO1', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), NULL, 1, 60, 5, 2400, 1, FALSE),
    (16, 4, 4, 'SIN_CONTESTAR', 'CU28SIN1', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('HOUR', -12, CURRENT_TIMESTAMP), NULL, NULL, NULL, 0, 60, 5, 3600, NULL, FALSE),
    (17, 4, 4, 'ACTIVO',        'CU28ACT1', DATEADD('DAY', -2, CURRENT_TIMESTAMP),  DATEADD('DAY', -2, CURRENT_TIMESTAMP), NULL, NULL, NULL, 0, 60, 5, 3600, NULL, FALSE);

-- Resultados de los 6 cuestionarios COMPLETO del Puesto A (IDs 1..6)
INSERT INTO resultado (cuestionario_id, fecha_calculo, puntaje_obtenido) VALUES
    (13, DATEADD('MINUTE', 35, DATEADD('DAY', -9, CURRENT_TIMESTAMP)), 9.50),
    (14, DATEADD('MINUTE', 45, DATEADD('DAY', -8, CURRENT_TIMESTAMP)), 8.50),
    (15, DATEADD('MINUTE', 40, DATEADD('DAY', -7, CURRENT_TIMESTAMP)), 8.50),
    (16, DATEADD('MINUTE', 50, DATEADD('HOUR', -40, CURRENT_TIMESTAMP)), 7.50),
    (17, DATEADD('MINUTE', 42, DATEADD('HOUR', -38, CURRENT_TIMESTAMP)), 7.50),
    (18, DATEADD('MINUTE', 30, DATEADD('HOUR', -37, CURRENT_TIMESTAMP)), 5.75);

-- Resultados por competencia (IDs 1..12): COM-01 (id 1, caract 7 -> req 7.0) y TEC-01 (id 5, caract 8 -> req 8.0)
INSERT INTO resultado_competencia (resultado_id, competencia_id, caracteristica_puesto_id, total_competencia) VALUES
    -- Resultado 1 (cuestionario 13, ALVAREZ): COM 9.50, TEC 9.50 -> promedio 9.50
    (1, 1, 7, 9.50),
    (1, 5, 8, 9.50),
    -- Resultado 2 (cuestionario 14, BENITEZ): COM 8.50, TEC 8.50 -> promedio 8.50
    (2, 1, 7, 8.50),
    (2, 5, 8, 8.50),
    -- Resultado 3 (cuestionario 15, CASTRO): COM 8.00, TEC 9.00 -> promedio 8.50
    (3, 1, 7, 8.00),
    (3, 5, 8, 9.00),
    -- Resultado 4 (cuestionario 16, DIAZ - borde exacto >=): COM 7.00, TEC 8.00 -> promedio 7.50
    (4, 1, 7, 7.00),
    (4, 5, 8, 8.00),
    -- Resultado 5 (cuestionario 17, ESPINOZA - falla TEC-01): COM 8.50, TEC 6.50 -> promedio 7.50
    (5, 1, 7, 8.50),
    (5, 5, 8, 6.50),
    -- Resultado 6 (cuestionario 18, FLORES - falla ambas): COM 5.50, TEC 6.00 -> promedio 5.75
    (6, 1, 7, 5.50),
    (6, 5, 8, 6.00);

-- Resultados por factor (IDs 1..24): FAC-01 (1) y FAC-02 (2) para COM-01; FAC-03 (3) y FAC-07 (7) para TEC-01
INSERT INTO resultado_factor (resultado_competencia_id, factor_id, total_factor) VALUES
    (1,  1, 10.00), (1,  2, 9.00),
    (2,  3, 10.00), (2,  7, 9.00),
    (3,  1, 8.00),  (3,  2, 9.00),
    (4,  3, 9.00),  (4,  7, 8.00),
    (5,  1, 8.00),  (5,  2, 8.00),
    (6,  3, 9.50),  (6,  7, 8.50),
    (7,  1, 7.00),  (7,  2, 7.00),
    (8,  3, 8.00),  (8,  7, 8.00),
    (9,  1, 8.50),  (9,  2, 8.50),
    (10, 3, 7.00),  (10, 7, 6.00),
    (11, 1, 5.00),  (11, 2, 6.00),
    (12, 3, 6.00),  (12, 7, 6.00);


