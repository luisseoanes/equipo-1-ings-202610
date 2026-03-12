# Requisitos no funcionales

---

# 📚 NF-01 – Rendimiento  
## Tiempo de respuesta eficiente en consultas clínicas

### Descripción  
El sistema debe proporcionar respuestas rápidas y fluidas para garantizar su utilidad en entornos clínicos donde el tiempo es un factor crítico. El chatbot debe iniciar rápidamente y mantener un rendimiento estable incluso durante múltiples consultas consecutivas.

### 🔎 Criterios de aceptación
- El sistema responde a las consultas en un tiempo ≤ 3 segundos en al menos el 95 % de los casos.  
- La aplicación inicia completamente en ≤ 5 segundos en dispositivos compatibles.  
- El sistema puede procesar al menos 10 consultas consecutivas sin degradación significativa del rendimiento.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Uso de modelos optimizados para ejecución local.  
- Precarga del modelo durante el inicio de la aplicación.  
- Uso eficiente de memoria y CPU.  

> **Puntos:**  
5  

---

# 📚 NF-02 – Usabilidad  
## Interacción rápida e intuitiva para personal clínico

### Descripción  
La interfaz del sistema debe ser fácil de usar y permitir que el personal de salud realice consultas rápidamente sin necesidad de capacitación previa, utilizando texto o voz, y recibiendo respuestas claras y comprensibles.

### 🔎 Criterios de aceptación
- El usuario puede realizar una consulta en un máximo de tres interacciones.  
- El sistema presenta respuestas claras y comprensibles.  
- El sistema permite interacción mediante voz.  
- El sistema puede ser utilizado sin capacitación previa.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Uso de interfaces simples basadas en Material Design.  
- Botón dedicado para entrada por voz.  
- Uso de tipografía clara y legible.  

> **Puntos:**  
5  

---

# 📚 NF-03 – Portabilidad  
## Funcionamiento en dispositivos Android sin conexión

### Descripción  
El sistema debe funcionar completamente de forma local sin requerir conexión a internet y ser compatible con dispositivos móviles Android modernos y distintos tamaños de pantalla.

### 🔎 Criterios de aceptación
- El sistema funciona completamente sin conexión a internet.  
- El sistema es compatible con Android 10.0 o superior.  
- La interfaz se adapta correctamente a diferentes tamaños de pantalla.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Uso de almacenamiento local.  
- Uso de layouts responsivos en Android.  
- Pruebas en múltiples resoluciones.  

> **Puntos:**  
5  

---

# 📚 NF-04 – Seguridad  
## Protección de la información clínica del usuario

### Descripción  
El sistema debe garantizar la protección de la información ingresada por el usuario. La información se almacena **únicamente de forma local** para permitir funcionalidades como el historial, evitando estrictamente cualquier transmisión a servidores externos o nubes de datos, y comunicando claramente que el sistema no sustituye el criterio médico profesional.

### 🔎 Criterios de aceptación
- La información ingresada y el historial se almacenan exclusivamente en el almacenamiento privado de la aplicación en el dispositivo.  
- El sistema no permite ni realiza transferencias de datos a servicios externos (APIs, telemetría, etc.).  
- El sistema muestra advertencias visibles sobre el alcance clínico de las respuestas.  
- El acceso a la base de datos local y a las preferencias del sistema está cifrado y protegido por los mecanismos del kernel de Android.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- **Android Keystore System**: Uso del sistema de claves de Android para gestionar las claves criptográficas de forma segura en hardware (TEE/StrongBox).
- **EncryptedSharedPreferences / SQLCipher**: Cifrado de la base de datos local (Room) y de los pares de clave-valor mediante AES-256.
- **App Sandboxing**: Aislamiento de procesos basado en el kernel de Linux para evitar que otras aplicaciones accedan a los datos de ObsIA.
- **BiometricPrompt**: Implementación opcional de autenticación biométrica para acceder a la pantalla de historial o configuraciones sensibles.
- **Scoped Storage**: Cumplimiento con las políticas de almacenamiento de Android para limitar el acceso al sistema de archivos solo a directorios específicos de la app.

> **Puntos:**  
5  

---

# 📚 NF-05 – Confiabilidad  
## Funcionamiento estable y manejo de errores

### Descripción  
El sistema debe garantizar un funcionamiento continuo y estable, manejando errores de forma controlada sin provocar cierres inesperados y proporcionando mensajes claros al usuario.

### 🔎 Criterios de aceptación
- El sistema mantiene una tasa de fallos menor al 1 % durante sesiones de uso normales.  
- El sistema muestra mensajes claros en caso de error.  
- El sistema permanece disponible mientras el dispositivo esté operativo.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Manejo de excepciones.  
- Validación de entradas.  
- Pruebas de estabilidad.  

> **Puntos:**  
5  

---

# 📚 NF-06 – Eficiencia en el uso de recursos  
## Optimización del almacenamiento y memoria

### Descripción  
El sistema debe utilizar eficientemente los recursos del dispositivo, manteniendo un tamaño adecuado de la aplicación, un consumo moderado de memoria RAM y un uso optimizado del procesador para evitar afectar el rendimiento del dispositivo.

### 🔎 Criterios de aceptación
- El tamaño total de la aplicación es ≤ 2.5 GB.  
- El consumo de memoria RAM es ≤ 1 GB durante la ejecución.  
- El uso promedio del CPU no supera el 60 % durante consultas normales.  

### 📌 Prioridad  
P1  

### 📖 Notas de implementación  
- Uso de modelos cuantizados.  
- Optimización de código nativo en C++.  
- Liberación de memoria no utilizada.  

> **Puntos:**  
5  

---

# 📚 NF-07 – Mantenibilidad  
## Facilidad de mantenimiento y actualización

### Descripción  
El sistema debe estar diseñado de forma modular y documentada para facilitar futuras modificaciones, mantenimiento y actualizaciones del conocimiento sin necesidad de reinstalar la aplicación completa.

### 🔎 Criterios de aceptación
- El sistema utiliza arquitectura modular.  
- El código fuente incluye documentación técnica en los módulos principales.  
- El sistema permite actualización del conocimiento sin modificar la aplicación principal.  

### 📌 Prioridad  
P2  

### 📖 Notas de implementación  
- Separación en módulos Frontend, Backend e IA.  
- Uso de documentación técnica.  
- Uso de control de versiones.  

> **Puntos:**  
5  

---

# 📚 NF-08 – Disponibilidad  
## Acceso inmediato al sistema en cualquier momento

### Descripción  
El sistema debe estar disponible para el usuario en cualquier momento en que el dispositivo esté operativo, permitiendo realizar consultas médicas sin interrupciones ni dependencias de servicios externos.

### 🔎 Criterios de aceptación
- El sistema puede utilizarse sin necesidad de conexión a internet.  
- La aplicación puede ejecutarse siempre que el dispositivo esté encendido y tenga recursos disponibles.  
- El sistema no depende de servicios externos para responder consultas.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Ejecución completamente local.  
- Eliminación de dependencias externas.  

> **Puntos:**  
5  

---

# 📚 NF-09 – Escalabilidad del conocimiento  
## Capacidad de ampliar el conocimiento clínico del sistema

### Descripción  
El sistema debe permitir ampliar o actualizar el conocimiento clínico del modelo utilizado para mejorar la calidad de las respuestas sin afectar la estabilidad de la aplicación.

### 🔎 Criterios de aceptación
- El sistema permite incorporar nuevas bases de conocimiento.  
- Las actualizaciones no afectan el funcionamiento de la aplicación existente.  
- El sistema mantiene compatibilidad con versiones anteriores de datos.  

### 📌 Prioridad  
P2  

### 📖 Notas de implementación  
- Uso de bases de conocimiento modulares.  
- Posibilidad de actualización mediante paquetes de datos.  

> **Puntos:**  
5  

---

# 📚 NF-10 – Compatibilidad  
## Funcionamiento en diferentes dispositivos Android

### Descripción  
El sistema debe ser compatible con diferentes dispositivos Android modernos, asegurando que la aplicación funcione correctamente en distintos fabricantes, resoluciones de pantalla y configuraciones de hardware.

### 🔎 Criterios de aceptación
- El sistema funciona en al menos tres modelos diferentes de dispositivos Android.  
- La aplicación mantiene funcionalidad completa en resoluciones de pantalla variadas.  
- No existen errores críticos asociados al hardware del dispositivo.  

### 📌 Prioridad  
P1  

### 📖 Notas de implementación  
- Pruebas en múltiples dispositivos físicos o emuladores.  
- Uso de componentes estándar del SDK de Android.  

> **Puntos:**  
5  