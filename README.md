
## Desarrollo Backend — VetCare360

### 1. Descripción de mi trabajo

Mi responsabilidad en el proyecto VetCare360 fue trabajar en el backend, desarrollado con Java y Spring Boot. Esta capa se encarga de procesar las solicitudes, aplicar la lógica del sistema, interactuar con la base de datos y coordinar la generación de informes mediante servicios de AWS.

### 2. Tecnologías utilizadas

- **Java 21:** lenguaje utilizado para desarrollar la lógica del backend.
- **Spring Boot:** framework para construir la API REST.
- **Spring Web:** manejo de solicitudes HTTP y creación de endpoints.
- **Spring Data JPA:** acceso y persistencia de datos mediante entidades y repositorios.
- **Supabase:** servicio utilizado para alojar la base de datos.
- **AWS SNS:** servicio de mensajería utilizado para publicar notificaciones relacionadas con la generación de informes.
- **Gradle:** administración de dependencias, compilación y ejecución del proyecto.
- **Git y GitHub:** control de versiones y almacenamiento del código fuente.

### 3. Funcionamiento del backend

El backend recibe solicitudes HTTP provenientes de los clientes y las procesa mediante controladores REST. Según la operación solicitada, ejecuta la lógica correspondiente y utiliza los servicios y repositorios necesarios para acceder a los datos.

Las principales responsabilidades de mi trabajo son:

- **Gestión de productos:** implementación de componentes para consultar y administrar productos, utilizando controladores, servicios, repositorios y entidades.
- **Persistencia de datos:** integración de la capa de acceso a datos con Spring Data JPA y la base de datos configurada en Supabase.
- **Monitoreo del servicio:** endpoint `GET /api/health`, utilizado para comprobar el estado del backend.
- **Generación de informes:** endpoint `POST /api/informes/generar`, que valida el encabezado `X-API-SECRET` antes de publicar un mensaje en AWS SNS.
- **Configuración e integración:** organización de las propiedades de conexión y configuración de los servicios utilizados por la aplicación.

### 4. Flujo de generación de informes

1. El cliente envía una solicitud HTTP POST al endpoint `/api/informes/generar`.
2. La solicitud incluye el encabezado `X-API-SECRET`.
3. El controlador compara el valor recibido con el secreto configurado en el backend.
4. Si la validación falla, la solicitud se rechaza.
5. Si la validación es correcta, el backend utiliza AWS SNS para publicar el mensaje configurado.
6. El resultado de la operación se devuelve al cliente mediante una respuesta HTTP.

### 5. Ejecución y validación

Para compilar y ejecutar el backend desde la raíz del proyecto:

```bash
./gradlew clean build
./gradlew bootRun
```

La aplicación debe iniciar correctamente y quedar escuchando en el puerto configurado, habitualmente el 8080.

Para comprobar el estado del servicio:

```bash
curl -i http://localhost:8080/api/health
```

La respuesta esperada es HTTP 200 con un estado que indique que el servicio está activo, siempre que la aplicación haya iniciado correctamente.

### 6. Validación de compilación

Se ejecutó `./gradlew clean build` y se obtuvo el resultado `BUILD SUCCESSFUL`. También se corrigieron problemas relacionados con clases duplicadas, la anotación del controlador de informes y las referencias a propiedades de configuración.

### 7. Control de versiones

Los cambios del backend se registraron en Git y se publicaron en GitHub en la rama `main`, mediante el commit `93a2f38`.
