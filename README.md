# ⚙️ VetCare 360 - API REST Backend & Seguridad (Spring Boot + AWS + Supabase)

## 📌 Contexto General del Proyecto
**VetCare 360** es un sistema de gestión veterinaria desacoplado. El módulo backend actúa como un **OAuth2 / OIDC Resource Server** encargado de procesar la lógica de negocio, persistir información en PostgreSQL (Supabase), interactuar con la infraestructura AWS (EC2 + API Gateway) y aplicar control de acceso basado en roles (**RBAC**).

---

## 🛠️ Tecnologías Utilizadas
* **Lenguaje & Framework:** Java 21 + Spring Boot 3
* **Persistencia & ORM:** Spring Data JPA + Hibernate + PostgreSQL Driver
* **Base de Datos:** Supabase PostgreSQL (`products` table)
* **Seguridad:** Spring Security (OAuth2 Resource Server + JWT Authentication + Method Security)[cite: 1, 4]
* **Cloud & Hosting:** AWS EC2 + AWS HTTP API Gateway (`ANY /{proxy+}`)
* **Proveedor de Identidad (IdP):** AWS Cognito User Pool (`us-east-1_Gz8tIv6dd`)[cite: 10]

---

## 🔍 Lo que hicimos en el Backend (Paso a Paso)

### 1. Modelo de Datos y Persistencia (`com.vetcare360.products`)
* **Entidad JPA (`ProductEntity.java`):** Mapeo de la tabla `products` con campos `id`, `name`, `description`, `price` y `stock`.[cite: 7]
* **Repositorio (`ProductRepository.java`):** Interfaz extendida de `JpaRepository` para operaciones CRUD directas sobre Supabase.[cite: 5]
* **Capa de Servicio (`ProductService.java`):** Implementación de la lógica de negocio para consulta (`findAll`), creación (`create`) y eliminación (`deleteById`).[cite: 6]

### 2. Construcción de Endpoints (`ProductController.java` & `VetController.java`)
Diseñamos e implementamos la API REST estructurada con anotaciones de seguridad a nivel de método (`@PreAuthorize`):[cite: 4]
* `GET /api/health`: Endpoint público de verificación de estado (Health Check).[cite: 1, 2]
* `GET /api/productos`: Consulta del catálogo completo (requiere token autenticado).[cite: 1, 4]
* `POST /api/productos`: Creación de insumos (restringido a `ROLE_Veterinario` y `ROLE_Admin`).[cite: 1, 4]
* `DELETE /api/productos/{id}`: Eliminación física de registros en la base de datos (restringido a `ROLE_Admin`).[cite: 4]
* `GET /api/veterinario/fichas`: Consulta de fichas médicas (restringido a `Veterinario` y `Admin`).[cite: 1, 2]

### 3. Configuración de Seguridad (`SecurityConfig.java`)
* **Mapeo de Grupos de Cognito:** `JwtAuthenticationConverter` personalizado que extrae la lista de grupos desde el claim `cognito:groups` del JWT y los convierte en autoridades Spring `ROLE_<GRUPO>`.[cite: 1]
* **Compatibilidad de Rutas API Gateway:** Permitimos explícitamente los patrones de salud con o sin prefijos de stage (`/api/health`, `/prod/api/health`, `/**/api/health`).[cite: 1]
* **CORS Global:** Configuración permisiva para soportar solicitudes preflight `OPTIONS` y encabezados `Authorization` enviados desde el cliente web.[cite: 1]

---

## 🔗 Conexión con la Infraestructura y el Frontend
1. **API Gateway (`ANY /{proxy+}`):** Toda solicitud enviada a API Gateway en la etapa `/prod` se transfiere mediante un proxy transparente hacia la aplicación Spring Boot alojada en la EC2.
2. **Validación JWT:** Spring Security intercepta el token Bearer, valida su firma criptográfica con el JWKS de Cognito y autoriza la ejecución si las autoridades coinciden con las reglas definidas.[cite: 1]

---

## 🧪 Pruebas y Resultados
* **Health Check:** Estado `200 OK` respondiendo `{"status":"UP"}` a través de la IP directa y de API Gateway.[cite: 2]
* **Persistencia PostgreSQL:** Inserción, lectura y borrado de registros confirmados en la tabla `products` de Supabase.[cite: 6, 7]
* **RBAC Estricto:** Retorno exitoso de datos en peticiones autorizadas y respuestas `403 Forbidden` al intentar ejecutar acciones no permitidas según el grupo de Cognito del usuario.[cite: 1, 4]
