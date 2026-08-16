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

## 5. Despliegue a producción (hosting)

El proyecto está preparado para desplegarse sin tocar código, usando variables de entorno.

### Backend

1. Generar el `.jar` ejecutable:
   ```bash
   cd Back
   mvn clean package
   # genera target/padel-connect-backend-1.0.0.jar
   ```
2. Subirlo a cualquier hosting que corra Java 21 (Render, Railway, un VPS propio, etc.) y ejecutarlo con:
   ```bash
   java -jar padel-connect-backend-1.0.0.jar
   ```
3. Configurar estas variables de entorno según el hosting (todas son opcionales — sin ellas usa los valores de desarrollo):

   | Variable | Para qué sirve | Default |
   |---|---|---|
   | `PORT` | Puerto donde escucha el backend (varias plataformas lo asignan solas) | `8080` |
   | `SPRING_PROFILES_ACTIVE` | Poner `mysql` para usar una base persistente en vez de H2 en memoria | *(vacío → H2)* |
   | `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Conexión a la base MySQL (solo si `SPRING_PROFILES_ACTIVE=mysql`) | conexión local |
   | `JWT_SECRET` | Clave para firmar los tokens — **cambiarla en producción** | clave de desarrollo incluida en el repo |

   > ⚠️ El valor de `JWT_SECRET` que trae el repo es solo para desarrollo. En producción conviene definir uno propio y secreto vía variable de entorno — si alguien conoce la clave puede firmar tokens válidos para cualquier usuario.

### Frontend

Al ser HTML/CSS/JS estático, se puede desplegar en cualquier hosting de archivos estáticos (Netlify, Vercel, GitHub Pages, Nginx, o el propio backend sirviéndolo desde `src/main/resources/static`). No requiere build.

- Si el frontend y el backend quedan bajo el **mismo dominio** (recomendado, por ejemplo con un reverse proxy que enrute `/api` al backend), no hace falta tocar nada: `API_BASE_URL` lo detecta solo.
- Si quedan en **dominios distintos**, hay que:
  1. Reemplazar `API_BASE_URL` en `Front/js/api.js` por la URL pública del backend.
  2. Verificar que el backend permita ese origen (`CorsConfig` ya acepta cualquier origen por defecto, así que no debería requerir cambios).
