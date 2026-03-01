# Requisitos no funcionales

---

# 📚 NF-01 – Rendimiento  
## Tiempo de respuesta eficiente en consultas clínicas

### Descripción  
El sistema debe proporcionar respuestas rápidas y fluidas para garantizar su utilidad en entornos clínicos donde el tiempo es un factor crítico. El chatbot debe iniciar rápidamente y mantener un rendimiento estable incluso durante múltiples consultas consecutivas.

### 🔎 Criterios de aceptación
- El sistema responde a las consultas en un tiempo ≤ 3 segundos en al menos el 95 % de los casos.  
- La aplicación inicia completamente en ≤ 5 segundos en dispositivos compatibles.  
- El sistema permite múltiples consultas consecutivas sin degradación perceptible del rendimiento.  

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
- El sistema puede diferenciar niveles de urgencia mediante palabras clave.  
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
El sistema debe garantizar la protección de la información ingresada por el usuario, almacenándola únicamente de forma local y evitando cualquier transmisión externa, además de informar claramente que el sistema no sustituye el criterio médico profesional.

### 🔎 Criterios de aceptación
- La información ingresada se almacena únicamente de forma local.  
- El sistema no transmite datos a servidores externos.  
- El sistema muestra advertencias sobre el uso clínico.  
- El acceso a los datos está protegido por el sistema operativo.  

### 📌 Prioridad  
P0  

### 📖 Notas de implementación  
- Uso de almacenamiento seguro local.  
- No uso de APIs externas.  
- Uso de mecanismos de seguridad de Android.  

> **Puntos:**  
5  

---

# 📚 NF-05 – Confiabilidad  
## Funcionamiento estable y manejo de errores

### Descripción  
El sistema debe garantizar un funcionamiento continuo y estable, manejando errores de forma controlada sin provocar cierres inesperados y proporcionando mensajes claros al usuario.

### 🔎 Criterios de aceptación
- El sistema no se cierra inesperadamente.  
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
- El sistema optimiza el uso del CPU.  

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
- El código está documentado.  
- El sistema permite actualización del conocimiento.  

### 📌 Prioridad  
P1  

### 📖 Notas de implementación  
- Separación en módulos Frontend, Backend e IA.  
- Uso de documentación técnica.  
- Uso de control de versiones.  

> **Puntos:**  
5  

---
