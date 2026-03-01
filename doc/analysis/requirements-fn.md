# Requisitos Funcionales

## ARRO

---

## RF-01 – Ingreso de consulta obstétrica por texto

**Descripción**
La aplicación debe permitir a la usuaria ingresar una consulta obstétrica mediante un campo de texto libre.

**Criterios de aceptación**

```gherkin
Feature: Ingreso de consulta por texto

Scenario: Envío de consulta escrita
  DADO que la aplicación está abierta
  CUANDO la usuaria escribe una consulta válida
  Y presiona el botón enviar
  ENTONCES el sistema recibe la consulta para su procesamiento
```

**Prioridad:** P0
**Puntos :** 2

---

## RF-02 – Validación de campo vacío

**Descripción**
El sistema no debe permitir el envío de consultas vacías.

**Criterios de aceptación**

```gherkin
Feature: Validación de campo obligatorio

Scenario: Intento de envío sin texto
  DADO que el campo de texto está vacío
  CUANDO la usuaria presiona enviar
  ENTONCES el sistema bloquea el envío
  Y muestra el mensaje "Debe ingresar una consulta"
```

**Prioridad:** P0
**Puntos :** 1

---

## RF-03 – Validación de longitud máxima de consulta

**Descripción**
La consulta no debe exceder los 500 caracteres.

**Criterios de aceptación**

```gherkin
Feature: Validación de longitud máxima

Scenario: Consulta supera el límite permitido
  DADO que la usuaria escribe más de 500 caracteres
  CUANDO intenta enviar la consulta
  ENTONCES el sistema impide el envío
  Y muestra un mensaje indicando que se superó el límite permitido
```

**Prioridad:** P1
**Puntos :** 1

---

## RF-04 – Captura de consulta mediante voz

**Descripción**
La aplicación debe permitir capturar una consulta mediante grabación de audio utilizando el micrófono del dispositivo.

**Criterios de aceptación**

```gherkin
Feature: Captura de audio

Scenario: Inicio de grabación
  DADO que el micrófono está habilitado
  CUANDO la usuaria presiona el botón de grabación
  ENTONCES el sistema inicia la captura de audio
```

**Prioridad:** P1
**Puntos :** 3

---

## RF-05 – Transcripción local de audio

**Descripción**
El sistema debe transcribir el audio capturado a texto utilizando procesamiento local sin conexión a internet.

**Criterios de aceptación**

```gherkin
Feature: Transcripción offline

Scenario: Conversión de audio a texto
  DADO que existe una grabación finalizada
  CUANDO el sistema procesa el audio
  ENTONCES genera una transcripción en texto
  Y no realiza comunicaciones externas
```

**Prioridad:** P1
**Puntos :** 5

---

## RF-06 – Clasificación temática de la consulta

**Descripción**
El sistema debe clasificar si la consulta pertenece al dominio obstétrico.

**Criterios de aceptación**

```gherkin
Feature: Clasificación de dominio

Scenario: Consulta enviada
  DADO que la usuaria envía una consulta
  CUANDO el sistema la analiza
  ENTONCES determina si pertenece al dominio obstétrico
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-07 – Rechazo de consulta fuera del dominio

**Descripción**
Si la consulta no pertenece al dominio obstétrico, el sistema debe rechazarla sin generar contenido clínico.

**Criterios de aceptación**

```gherkin
Feature: Rechazo fuera de alcance

Scenario: Consulta no obstétrica
  DADO que la consulta fue clasificada como fuera de dominio
  CUANDO el sistema responde
  ENTONCES muestra un mensaje indicando que está fuera del alcance de la aplicación
  Y no genera respuesta clínica
```

**Prioridad:** P0
**Puntos :** 2

---

## RF-08 – Búsqueda en base de casos clínicos predefinidos

**Descripción**
El sistema debe verificar si la consulta coincide con un caso clínico predefinido almacenado localmente.

**Criterios de aceptación**

```gherkin
Feature: Búsqueda en base predefinida

Scenario: Coincidencia encontrada
  DADO que la consulta es obstétrica
  CUANDO existe coincidencia en la base de casos clínicos
  ENTONCES el sistema selecciona la respuesta almacenada
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-09 – Generación de respuesta mediante modelo local

**Descripción**
Si no existe coincidencia en la base de casos, el sistema debe generar la respuesta utilizando el modelo de lenguaje local.

**Criterios de aceptación**

```gherkin
Feature: Ejecución de modelo local

Scenario: Sin coincidencia en base predefinida
  DADO que la consulta es obstétrica
  Y no existe coincidencia en la base de casos
  CUANDO el sistema procesa la consulta
  ENTONCES ejecuta el modelo de lenguaje local
  Y genera una respuesta clínica
```

**Prioridad:** P0
**Puntos :** 5

---

## RF-10 – Control de tiempo máximo de inferencia

**Descripción**
El sistema debe cancelar la generación de respuesta si el modelo excede el tiempo máximo definido.

**Criterios de aceptación**

```gherkin
Feature: Control de tiempo de procesamiento

Scenario: Modelo excede tiempo permitido
  DADO que el modelo está procesando una consulta
  CUANDO supera el tiempo máximo configurado
  ENTONCES el sistema cancela la ejecución
  Y muestra un mensaje de error controlado
```

**Prioridad:** P1
**Puntos :** 3

---

## RF-11 – Detección de emergencia obstétrica

**Descripción**
El sistema debe identificar si la consulta contiene indicadores de emergencia obstétrica.

**Criterios de aceptación**

```gherkin
Feature: Identificación de emergencia

Scenario: Consulta con indicadores de riesgo
  DADO que la consulta contiene términos asociados a riesgo vital
  CUANDO el sistema la analiza
  ENTONCES la clasifica como emergencia
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-12 – Etiquetado del nivel de urgencia

**Descripción**
El sistema debe incluir una etiqueta visible indicando el nivel de urgencia de la respuesta.

**Criterios de aceptación**

```gherkin
Feature: Etiquetado de urgencia

Scenario: Respuesta generada
  DADO que existe una clasificación de urgencia
  CUANDO el sistema muestra la respuesta
  ENTONCES incluye una etiqueta visible "URGENTE" o "RUTINA"
```

**Prioridad:** P0
**Puntos :** 2

---

## RF-13 – Inclusión de recomendaciones inmediatas en emergencias

**Descripción**
Cuando la consulta sea clasificada como emergencia, la respuesta debe incluir acciones inmediatas.

**Criterios de aceptación**

```gherkin
Feature: Respuesta prioritaria ante emergencia

Scenario: Emergencia confirmada
  DADO que la consulta fue clasificada como emergencia
  CUANDO el sistema genera la respuesta
  ENTONCES incluye recomendaciones de acción inmediata
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-14 – Presentación en lenguaje clínico claro

**Descripción**
Las respuestas deben presentarse en lenguaje clínico claro y conciso.

**Criterios de aceptación**

```gherkin
Feature: Lenguaje clínico claro

Scenario: Respuesta clínica generada
  DADO que el sistema genera una respuesta
  ENTONCES la información se presenta en lenguaje claro y preciso
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-15 – Respuesta sin expresiones conversacionales

**Descripción**
La respuesta no debe incluir saludos ni expresiones conversacionales.

**Criterios de aceptación**

```gherkin
Feature: Respuesta directa

Scenario: Generación de respuesta
  DADO que el sistema genera una respuesta
  ENTONCES no incluye saludos ni expresiones coloquiales
```

**Prioridad:** P1
**Puntos :** 2

---

## RF-16 – Respuesta en un único turno

**Descripción**
Cada consulta debe generar una única respuesta completa sin solicitar información adicional.

**Criterios de aceptación**

```gherkin
Feature: Respuesta única

Scenario: Respuesta generada
  DADO que la usuaria envía una consulta
  CUANDO el sistema responde
  ENTONCES entrega una única respuesta completa
  Y no solicita datos adicionales
```

**Prioridad:** P1
**Puntos :** 2

---

## RF-17 – Inclusión obligatoria de advertencia clínica

**Descripción**
Toda respuesta debe incluir una advertencia indicando que no sustituye el criterio médico profesional.

**Criterios de aceptación**

```gherkin
Feature: Advertencia obligatoria

Scenario: Mostrar advertencia
  DADO que el sistema genera una respuesta
  ENTONCES incluye una advertencia visible indicando que no sustituye el criterio profesional
```

**Prioridad:** P1
**Puntos :** 1

---

## RF-18 – No almacenamiento persistente de consultas

**Descripción**
El sistema no debe almacenar las consultas en almacenamiento persistente del dispositivo.

**Criterios de aceptación**

```gherkin
Feature: No persistencia de consultas

Scenario: Consulta procesada
  DADO que la usuaria realiza una consulta
  CUANDO el sistema la procesa
  ENTONCES no guarda la consulta en almacenamiento persistente
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-19 – No almacenamiento persistente de respuestas

**Descripción**
El sistema no debe almacenar las respuestas generadas en almacenamiento persistente.

**Criterios de aceptación**

```gherkin
Feature: No persistencia de respuestas

Scenario: Respuesta generada
  DADO que el sistema genera una respuesta
  ENTONCES no la almacena en almacenamiento persistente
```

**Prioridad:** P0
**Puntos :** 3

---

## RF-20 – Ausencia l visible

**Descripción**
La aplicación no debe mostrar historial de consultas ni respuestas anteriores.

**Criterios de aceptación**

```gherkin
Feature: Sin historial visible

Scenario: Reinicio de aplicación
  DADO que la aplicación se cierra y se vuelve a abrir
  ENTONCES la interfaz se muestra limpia
  Y no presenta información previa
```

**Prioridad:** P0
**Puntos :** 2

---

## RF-21 – Procesamiento independiente por consulta

**Descripción**
Cada consulta debe procesarse sin utilizar información de consultas anteriores.

**Criterios de aceptación**

```gherkin
Feature: Procesamiento independiente

Scenario: Nueva consulta
  DADO que existen consultas previas
  CUANDO la usuaria envía una nueva consulta
  ENTONCES el sistema no utiliza información anterior en la generación de respuesta
```

**Prioridad:** P1
**Puntos :** 2

---

## RF-22 – Indicador visual de procesamiento

**Descripción**
El sistema debe mostrar un indicador visual mientras procesa la consulta.

**Criterios de aceptación**

```gherkin
Feature: Indicador de procesamiento

Scenario: Consulta en procesamiento
  DADO que la usuaria envía una consulta
  CUANDO el sistema inicia el procesamiento
  ENTONCES muestra un indicador visual
  Y lo oculta cuando la respuesta está disponible
```

**Prioridad:** P1
**Puntos :** 1

---

## RF-23 – Acceso directo a la pantalla de consulta

**Descripción**
Al iniciar la aplicación, debe mostrarse directamente la pantalla de consulta.

**Criterios de aceptación**

```gherkin
Feature: Acceso directo

Scenario: Inicio de aplicación
  DADO que la usuaria abre la aplicación
  ENTONCES se muestra inmediatamente la pantalla de consulta
  Y no se presentan pantallas intermedias
```

**Prioridad:** P2
**Puntos :** 1

---

## RF-24 – Interfaz fija sin configuración

**Descripción**
La aplicación no debe ofrecer opciones de configuración ni personalización.

**Criterios de aceptación**

```gherkin
Feature: Interfaz sin configuración

Scenario: Uso normal de la aplicación
  DADO que la aplicación está en ejecución
  ENTONCES no existe menú de configuración
  Y no existen opciones de personalización
```

**Prioridad:** P2
**Puntos :** 1

---


