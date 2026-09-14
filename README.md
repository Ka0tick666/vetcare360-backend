# ⚙️ VetCare 360 - API REST Backend & Seguridad (Spring Boot + AWS)

## 📌 Contexto General del Proyecto
**VetCare 360** es un sistema de gestión veterinaria desacoplado. El módulo backend actúa como un **OAuth2 / OIDC Resource Server** encargado de procesar la lógica de negocio, interactuar con la infraestructura en AWS (EC2 + API Gateway) y aplicar control de acceso basado en roles (**RBAC**).

---

## 🛠️ Tecnologías Utilizadas
* **Lenguaje & Framework:** Java 17 + Spring Boot 3
* **Seguridad:** Spring Security (OAuth2 Resource Server + JWT Authentication)
* **Cloud & Hosting:** AWS EC2 (Servidor donde corre la API) + AWS HTTP API Gateway
* **Proveedor de Identidad (IdP):** AWS Cognito User Pool

---

## 🔍 Lo que hicimos en el Backend (Paso a Paso)

### 1. Construcción de Endpoints (`VetController.java`)
Diseñamos e implementamos la API estructurada según los requerimientos de acceso:
* `GET /api/health`: Endpoint público para monitoreo de estado.
* `GET /api/productos`: Endpoint para consultar catálogo de productos (accesible por cualquier usuario autenticado).
* `GET /api/veterinario/fichas`: Consulta de fichas médicas (restringido a `Veterinario` y `Admin`).
* `GET /api/admin/dashboard`: Métricas del sistema (exclusivo para `Admin`).

### 2. Configuración de Seguridad (`SecurityConfig.java`)
* **Mapeo de Grupos de Cognito:** Configuramos un `JwtAuthenticationConverter` personalizado para leer el claim `cognito:groups` del token JWT enviado por AWS Cognito y transformarlo en autoridades `ROLE_<GRUPO>` dentro del contexto de Spring Security.
* **CORS Global:** Habilitamos políticas CORS permitiendo encabezados `Authorization` y solicitudes preflight `OPTIONS` provenientes de API Gateway/Frontend.
* **Reglas de Autorización:** Definimos la jerarquía de accesos en el `SecurityFilterChain` exigiendo roles específicos por ruta.

---

## 🔗 Conexión con la Infraestructura y el Frontend
1. **API Gateway (Proxy Reverse):** La API expuesta en la EC2 (`http://3.88.142.71`) se integró con AWS HTTP API Gateway bajo la ruta `/api/{proxy+}`.
2. **Validación JWT:** Cuando el cliente en React realiza una petición enviando el `id_token` Bearer en los headers, Spring Security intercepta el JWT, valida su firma con la clave pública del User Pool de Cognito, extrae el rol y permite o deniega el paso (retornando `403 Forbidden` si no cuenta con los permisos).

---

## 🧪 Pruebas y Resultados
* **Salud del Sistema:** Respuesta `200 OK` en `/api/health`.
* **Rol Cliente:** Acceso exitoso a `/api/productos` y respuesta **403 Forbidden** al intentar acceder a `/api/veterinario/fichas`.
* **Rol Veterinario:** Acceso exitoso a `/api/productos` y `/api/veterinario/fichas`.