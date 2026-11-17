# VetCare360 backend - limpieza

## Cambios
- Se retiró InformeController (SNS corresponde al BFF en Vercel).
- Se retiró VetController con endpoints de datos ficticios.
- GET /api/health y GET /health quedan en HealthController.
- Se unificaron paquetes de productos en com.vetcare360.backend.products.
- Se quitó la dependencia web duplicada y AWS SNS no utilizada.
- Variables de entorno para base de datos y Cognito, sin credenciales reales.
- CORS limitado a FRONTEND_ORIGIN (localhost por defecto).
- Dockerfile documenta puerto 8080.
- Se omitió el workflow de despliegue automático hasta revisar secretos, puertos y credenciales EC2.

## Importante
- No se ha comprobado compilación ni ejecución en AWS.
- Necesitas configurar Cognito, DB_URL, DB_USERNAME, DB_PASSWORD y FRONTEND_ORIGIN.
- El CRUD de productos se mantiene y requiere tabla `products` compatible.
- GET /api/productos sigue requiriendo autenticación; POST permite Admin/Veterinario y DELETE Admin.
- El frontend todavía usa Supabase directamente: hay que coordinar la migración de llamadas.
- Si .env o application.properties con secretos estuvieron en Git, cambia las claves y revisa el historial remoto. Ignorar archivos no borra versiones anteriores.
- Revisa la política de despliegue antes de volver a habilitar GitHub Actions.
