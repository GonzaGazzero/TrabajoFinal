# Instalación y ejecución

## Requisitos

- **Java 21** (JDK)
- **Maven 3.9+** (o usar el wrapper si el proyecto lo incluye)
- Un navegador moderno
- MySQL 8+ — **solo si** se quiere usar el perfil `mysql` en vez de la base H2 por defecto

## 1. Backend

```bash
cd Back
mvn spring-boot:run
```

Por defecto arranca **sin configuración adicional**, usando una base **H2 en memoria** (se recrea vacía en cada reinicio — no hay datos precargados). Queda escuchando en `http://localhost:8080`.

Para verificar que levantó bien:
```bash
curl http://localhost:8080/api/partidos
# debería responder: []
```

### Usar MySQL en vez de H2 (persistencia real)

1. Crear la base y el usuario (ajustar credenciales según corresponda):
   ```sql
   CREATE DATABASE padelconnect_db;
   ```
2. Revisar/ajustar `Back/src/main/resources/application-mysql.yml` (usuario, contraseña, host).
3. Levantar el backend con el perfil `mysql` activo:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
   Hibernate crea las tablas automáticamente (`ddl-auto: update`) en el primer arranque.

> **Importante para quien evalúe el proyecto:** si solo se corre `mvn spring-boot:run` sin más, **no hace falta tener MySQL instalado** — el backend funciona igual con H2. El perfil `mysql` es opcional y pensado para desarrollo con datos persistentes.

## 2. Frontend

El frontend es HTML/CSS/JS puro, sin build step ni `npm install`. Alcanza con servirlo como archivos estáticos (abrirlo con doble clic como `file://` puede fallar por CORS/`fetch`, así que se recomienda un servidor mínimo):

```bash
cd Front
python3 -m http.server 5500
# abrir http://localhost:5500
```

También funciona con la extensión "Live Server" de VS Code, `npx serve`, o cualquier servidor estático equivalente.

La URL del backend se resuelve automáticamente en `Front/js/api.js` (`API_BASE_URL`): en `localhost` apunta a `http://localhost:8080/api`, y en cualquier otro dominio asume que el backend está en el mismo origen (`/api`), pensado para cuando el backend sirve también el frontend o hay un reverse proxy delante de ambos. Si el backend queda en un dominio completamente distinto al del frontend, hay que reemplazar esa función por la URL fija correspondiente.

## 3. Verificación rápida (smoke test manual)

1. Abrir el frontend → **Iniciar Sesión → Registro** → crear una cuenta.
2. **Crear Partido** → completar el formulario → publicar.
3. Cerrar sesión, registrar un segundo usuario, y desde **Explorar Partidos** apretar **Sumarme al partido**.
4. Volver a loguearse con el primer usuario (organizador) → **Mis Partidos** o el botón "Solicitudes (1)" en la tarjeta → **Aceptar**.
5. El jugador debería pasar a la lista de confirmados y el contador de cupos actualizarse.

## 4. Tests automatizados

```bash
cd Back
mvn test
```

Corre 8 tests (JUnit 5 + Spring Boot Test) contra una base H2 en memoria independiente: reglas de cupos, flujo de solicitudes pendientes/aceptación, cálculo de distancia (Haversine) e internacionalización de mensajes de validación.

## Puertos usados

| Servicio | Puerto por defecto |
|---|---|
| Backend (Spring Boot) | `8080` |
| Consola H2 (`/h2-console`) | mismo puerto, `8080` |
| Frontend (servidor estático de ejemplo) | `5500` (o el que se elija) |

Si el puerto 8080 ya está ocupado por una ejecución anterior que quedó colgada, liberarlo antes de levantar el backend de nuevo (`lsof -i :8080` en macOS/Linux) para evitar estar probando contra una versión vieja del código sin darse cuenta.

## 5. Despliegue a producción: Netlify (frontend) + Render (backend) + Supabase (PostgreSQL)

Esta es la combinación elegida para este proyecto, con las tres patas gratuitas verificadas (sin tarjeta):

- **Netlify** — hosting estático del frontend.
- **Render** — corre el backend Java (free tier: se "duerme" tras ~15 min sin uso; la siguiente consulta tarda ~30-50s en responder mientras despierta).
- **Supabase** — base de datos PostgreSQL gestionada (free tier: 500MB, se pausa —no se borra— tras 1 semana sin actividad).

> Se descartaron Vercel y Railway para el backend (no ofrecen procesos Java persistentes gratis de forma confiable), y MySQL gestionado gratis no tiene hoy una opción estable — por eso el backend usa el perfil `postgres` en vez de `mysql` para producción (ambos perfiles conviven en el proyecto; `mysql` sigue sirviendo para desarrollo local si se prefiere).

### 5.1 Base de datos en Supabase

1. Crear cuenta en [supabase.com](https://supabase.com) → "New Project".
2. Al crear el proyecto, Supabase pide una contraseña para la base — guardarla, hace falta después.
3. Ir a **Project Settings → Database** y copiar los datos de conexión (host, puerto, nombre de la base, usuario). Ahí mismo Supabase suele mostrar directamente una URL de conexión con formato `postgresql://...` — solo hay que ajustarla al formato JDBC (ver siguiente paso).

### 5.2 Backend en Render

1. Crear cuenta en [render.com](https://render.com) → "New" → "Web Service" → conectar el repositorio de GitHub (subir el proyecto a un repo si todavía no está).
2. En la configuración del servicio:
   - **Root Directory**: `Back`
   - **Runtime**: Docker (Render detecta el `Back/Dockerfile` automáticamente)
   - **Instance Type**: Free
3. Agregar las variables de entorno (Environment):

   | Variable | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `postgres` |
   | `DB_URL` | `jdbc:postgresql://<host-de-supabase>:5432/postgres` |
   | `DB_USERNAME` | `postgres` (o el usuario que muestre Supabase) |
   | `DB_PASSWORD` | la contraseña que definiste al crear el proyecto en Supabase |
   | `JWT_SECRET` | una clave propia, larga y secreta (no la que trae el repo) |

   Render define `PORT` solo — no hace falta agregarla.
4. Desplegar. Al terminar, Render da una URL pública (`https://tu-backend.onrender.com`). Verificar que responde: `https://tu-backend.onrender.com/api/partidos` debería devolver `[]` (puede tardar la primera vez mientras el servicio "despierta").

> ⚠️ El `JWT_SECRET` que trae el repo es solo para desarrollo. En producción hay que definir uno propio vía variable de entorno — si alguien lo conoce puede firmar tokens válidos para cualquier usuario.

### 5.3 Frontend en Netlify

1. Antes de desplegar, editar `Front/js/api.js` y reemplazar `BACKEND_URL` por la URL real que generó Render en el paso anterior (con `/api` al final):
   ```js
   const BACKEND_URL = "https://tu-backend.onrender.com/api";
   ```
2. En [netlify.com](https://netlify.com) → "Add new site" → "Import an existing project" → conectar el mismo repositorio. El archivo `netlify.toml` en la raíz ya le indica a Netlify que la carpeta a publicar es `Front/`, así que no hace falta tocar nada más.
3. Al terminar, Netlify da una URL pública (`https://tu-sitio.netlify.app`). Abrirla y probar el flujo completo: registro, crear partido, unirse, aceptar solicitud.

No hace falta tocar `CorsConfig` en el backend — ya acepta pedidos desde cualquier origen, así que Netlify y Render en dominios distintos funcionan sin configuración extra.

### 5.4 Alternativa sin base de datos externa

Para probar el despliegue rápido sin conectar Supabase, alcanza con no definir `SPRING_PROFILES_ACTIVE` (o dejarlo vacío) en Render: el backend arranca igual con H2 en memoria. Los datos se pierden cada vez que Render "duerme" el servicio por inactividad y lo vuelve a levantar, pero sirve para confirmar que el resto del despliegue funciona.
