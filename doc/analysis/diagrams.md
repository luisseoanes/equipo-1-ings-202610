# Diagramas de Comportamiento y Diseño

En esta sección se documenta el comportamiento y diseño del producto de software utilizando diagramas UML y Ad-Hoc.

## 1. Diagrama Ad-Hoc

Este diagrama muestra el flujo general de la aplicación desde que el usuario interactúa hasta que recibe una respuesta.

```mermaid
flowchart TD
    A([Usuaria]) -->|Ingresa consulta| B(Pantalla Home)
    B --> C{Es audio o texto?}
    C -->|Audio| D[Transcripción Local Offline]
    C -->|Texto| E[Recepción Directa]
    D --> E
    E --> F{¿Dominio obstétrico?}
    F -->|No| G[Rechazar consulta y avisar]
    F -->|Sí| H[Procesamiento Local]
    H --> I{¿Emergencia Médica?}
    I -->|Sí| J[Detectar Riesgo Vital]
    J --> K[Generar Recomendación de Acción Inmediata]
    I -->|No| L[Generar Respuesta Clínica de Rutina]
    K --> M[Formatear Respuesta con Etiqueta URGENTE]
    L --> N[Formatear Respuesta con Etiqueta RUTINA]
    M --> O[Mostrar Respuesta y Advertencia Restrictiva]
    N --> O
    O --> P[(Guardar en Historial Local)]
```

## 2. Diagrama de Casos de Uso

A continuación se modelan los casos de uso separando explícitamente a los actores ("Usuario Registrado", "Usuario No Registrado" y "Sistema"). Se abstraen los detalles técnicos de generación como funcionalidades incluidas.

```mermaid
flowchart LR
    %% Actores
    UR((Usuario \nRegistrado))
    UN((Usuario No \nRegistrado))
    SYS((Sistema))

    %% Casos de Uso Principales
    CU1([Ingresar consulta obstétrica])
    CU2([Revisar historial de conversaciones])
    
    %% Casos de Uso del Sistema
    CU3([Procesar consulta y clasificar dominio])
    CU4([Generar respuesta clínica])
    CU5([Validar y etiquetar emergencia médica])

    %% Relaciones de los Usuarios
    UN --- CU1
    UR --- CU1
    UR --- CU2

    %% Dependencias entre casos de uso (Include)
    CU1 -.->|<<include>>| CU3
    CU1 -.->|<<include>>| CU4
    CU1 -.->|<<include>>| CU5

    %% Relaciones del Sistema
    SYS --- CU3
    SYS --- CU4
    SYS --- CU5

    classDef actor fill:#eaeaea,stroke:#333,stroke-width:2px;
    classDef usecase fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    
    class UR,UN,SYS actor;
    class CU1,CU2,CU3,CU4,CU5 usecase;
```

## 3. Diagrama de Secuencia - Caso 1 (Happy Path de Rutina)

Diagrama de secuencia simple (sin tecnicismos) ilustrando una consulta obstétrica válida que se responde normalmente.

```mermaid
sequenceDiagram
    actor U as Usuaria
    participant APP as Aplicación Móvil
    participant LOG as Lógica Interna
    
    U->>APP: Escribe consulta sobre síntomas leves
    APP->>LOG: Envía datos para análisis
    LOG->>LOG: Confirma que pertenece al dominio obstétrico
    LOG->>LOG: Analiza y genera la respuesta clínica
    LOG->>APP: Devuelve la respuesta y advertencia general
    APP->>U: Muestra respuesta al usuario y guarda historial
```

## 4. Diagrama de Secuencia - Caso 2 (Happy Path de Emergencia)

Diagrama de secuencia enfocado en la interacción de una emergencia médica donde se despliega una acción prioritaria.

```mermaid
sequenceDiagram
    actor U as Usuaria
    participant APP as Aplicación Móvil
    participant LOG as Lógica Interna
    
    U->>APP: Ingresa descripción de síntomas graves (ej. sangrado)
    APP->>LOG: Envía datos para análisis prioritario
    LOG->>LOG: Identifica posibles riesgos vitales
    LOG->>LOG: Prepara indicaciones de acción inmediata
    LOG->>APP: Devuelve alerta urgente con recomendaciones
    APP->>U: Muestra recomendación urgente en pantalla
```
