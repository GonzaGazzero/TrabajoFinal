# Revisión de código y cambios realizados

Este documento resume la auditoría hecha sobre el proyecto (backend + frontend) y los problemas corregidos. Se incluye a propósito, para que quede registro de qué se encontró, por qué era un problema real y cómo se verificó la corrección — útil como respaldo ante una defensa del trabajo final.

## Metodología

No se corrigió nada "a ojo": cada bug se confirmó primero levantando el backend real y reproduciéndolo con `curl` o desde el navegador (Chrome DevTools / requests reales), y cada corrección se volvió a probar de la misma forma — incluyendo un flujo end-to-end completo por clicks reales en la interfaz (registro de dos usuarios, creación de partido, solicitud de unión, aceptación por el organizador).

## Bugs críticos corregidos

### 1. El registro de usuarios reales estaba completamente roto

`Front/js/api.js` nunca enviaba el campo `confirmPassword` al backend, pero `RegisterRequest` lo exige (`@NotBlank`). **Resultado: ningún registro contra el backend real podía funcionar** — siempre devolvía `400 Bad Request`. Se detectó recién al probar el flujo completo en el navegador, no en la lectura estática del código.

**Fix:** `ApiService.register()` ahora incluye `confirmPassword` en el body.

### 2. Aceptar / rechazar solicitudes de unión no funcionaba

El frontend llamaba a `POST /api/partidos/{id}/responder-solicitud` (endpoint inexistente); el backend expone `POST /api/partidos/{id}/inscripciones/{inscripcionId}/aceptar` y `/rechazar`. Verificado con `curl`: la llamada del frontend devolvía `500`, y como no chequeaba `response.ok`, silenciosamente caía a un modo local (`localStorage`) que nunca tenía los partidos reales. **El organizador nunca podía aprobar ni rechazar una solicitud real.**

**Fix:**
- `PartidoService`/`PartidoController` sin cambios (ya estaban bien).
- `ApiService.responderSolicitud()` reescrito para llamar al endpoint correcto con el `inscripcionId` real.
- Se agregó `ApiService.normalizeMatch()`, que adapta la forma `solicitudesPendientes` (anidada, tal como la devuelve el backend) a un array plano `solicitudes` que usa el resto del frontend, para que la UI no tenga que preocuparse por el origen de los datos (backend real vs. modo offline).
- `app.js` actualizado para pasar el `inscripcionId` correcto al aceptar/rechazar.

Verificado end-to-end por clicks reales: solicitud → aparece "Solicitudes (1)" en la lista → detalle → Aceptar → el jugador pasa a "Jugadores Confirmados".

### 3. La vista de listado nunca sabía quién era el usuario logueado

`PartidoController.listarPartidos` no recibía `@AuthenticationPrincipal Usuario currentUser`, así que `estadoInscripcionUsuario` siempre volvía `"NINGUNA"` en el listado (aunque el detalle de un partido sí lo calculaba bien). Esto se descubrió durante la prueba end-to-end: el botón "Mi Partido" no aparecía en la lista aunque el usuario fuera el organizador.

**Fix:** se agregó el parámetro `currentUser` al endpoint y se propagó a `PartidoService.listarPartidos(...)`.

### 4. Filtro por Nivel y por Categoría/Género no filtraba nada

`PartidoController` solo aceptaba `zona`, `fecha`, `lat`, `lng`, `orden`. Los parámetros `nivel` y `genero` que mandaba el frontend eran ignorados en silencio por Spring (sin error, simplemente no filtraba). Confirmado con `curl` pidiendo `nivel=8va&genero=Femenino` y recibiendo la lista completa sin filtrar.

**Fix:** se agregaron `nivel` y `genero` como parámetros del endpoint y del filtrado en `PartidoService.listarPartidos`.

### 5. El campo "Categoría/Género" no existía en el backend

Ni la entidad `Partido`, ni `PartidoRequestDTO`/`PartidoResponseDTO` tenían un campo `genero`. El selector de la UI se enviaba y Jackson lo descartaba en silencio; el resultado es que **todo partido creado contra el backend real se mostraba siempre como "Mixto"**, sin importar lo elegido.

**Fix:** se agregó `genero` a la entidad `Partido` (columna nueva, creada automáticamente por Hibernate), a ambos DTOs, y se propagó en `PartidoService` (persistencia y lectura).

## Mejoras de configuración / robustez

### 6. El proyecto no arrancaba sin una base MySQL preexistente

`application.yml` tenía `spring.profiles.active: mysql` fijo, con URL y credenciales (`root`/`pass`) hardcodeadas apuntando a `localhost:3306/padelconnect_db`. Si quien evalúa el proyecto no tiene exactamente esa base creada, la aplicación no arranca.

**Fix:** el perfil activo por defecto ahora queda vacío (usa H2 en memoria, sin requerir nada instalado), y el perfil `mysql` sigue disponible como opción explícita (`-Dspring-boot.run.profiles=mysql`). Ver [INSTALACION.md](INSTALACION.md).

### 7. Proceso de backend desactualizado corriendo en segundo plano

Durante la revisión se encontró un proceso Java previo, ya corriendo en el puerto 8080, con una versión más vieja del código (sin el flujo de solicitudes pendientes). Se dejó de lado para no pisar trabajo en curso, pero es una buena práctica verificar (`lsof -i :8080`) que no quede un backend viejo escuchando antes de probar cambios nuevos.

## Mejoras menores

- **Errores de validación más legibles**: `GlobalExceptionHandler` devolvía el `Map` de errores como `toString()` (`{fecha=El campo es obligatorio}`); ahora arma un mensaje `"campo: error; campo2: error2"` más prolijo de mostrar en la UI.
- **`GeocodingService`** creaba un `RestTemplate` nuevo en cada llamada; ahora se reutiliza una sola instancia.
- **Listado de partidos más rápido**: el frontend volvía a geocodificar cada partido devuelto por el backend contra la API externa de Georef, aunque el backend ya calcula `latitud`/`longitud`/`distanciaKm`. Se eliminó esa duplicación — el listado ahora confía directamente en los datos que ya vienen calculados del backend, sin llamadas de red extra por partido.

## Ajustes visuales (a pedido, durante la verificación en navegador)

- El botón "Sumarme al partido" tenía un ícono de pelota de fútbol (⚽); se quitó.
- El logo del header se veía duplicado: la imagen (`Gemini_Generated_Image_...jpg`) ya traía el texto "CuartaPala" dibujado adentro, y el HTML volvía a mostrar el mismo texto al lado. Se recortó la imagen a solo el ícono de la paleta (`images/cuartapala-icon.png`) y se dejó el texto como wordmark HTML (permite además que tome el color del tema).
- La paleta de colores (`css/styles.css`, variables `--primary`, `--bg-main`, etc.) se ajustó al verde lima y azul marino del logo, en vez del amarillo/dorado que tenía antes.

## Qué quedó sin tocar (a propósito)

- El flujo de "modo offline" (`localStorage`) se mantiene como demostración visual cuando no hay backend disponible — no es parte del backend real, y no se le pidió corrección específica.
- No se agregaron tests nuevos para los endpoints de controller (aceptar/rechazar, filtros) — los 8 tests existentes siguen pasando, pero sería una buena adición a futuro cubrir `PartidoController` con `@WebMvcTest` o tests de integración con `MockMvc`.

## Segunda ronda: geolocalización, UX de sesión y preparación para hosting

### Geolocalización y distancia

Se verificó de punta a punta con `curl` (Belgrano, Rosario, Mar del Plata sin coordenadas enviadas por el front, y una zona no cacheada que fuerza una llamada real a la API de Georef) y con geolocalización simulada en el navegador: los cálculos de distancia (Haversine) y el orden por cercanía son correctos. No había un bug de cálculo, pero sí de comunicación: cuando el usuario **negaba** el permiso de ubicación, el toggle "Ordenar por cercanía" igual decía estar ordenando, cuando en realidad no tenía coordenadas con qué hacerlo. Ahora, si falla la geolocalización, el toggle se revierte y se muestra un mensaje específico según la causa real (permiso denegado, ubicación no disponible, o timeout) en vez de un mensaje genérico.

### Acciones sin haber iniciado sesión

Se puede explorar el listado de partidos sin cuenta (es público a propósito), pero crear un partido, sumarse, aceptar/rechazar solicitudes o ver "Mis Partidos" requieren sesión. El problema: al intentar esas acciones sin estar logueado, el backend devuelve `403` **sin cuerpo**, y el frontend intentaba parsear ese cuerpo vacío como JSON, mostrando un error críptico (`Unexpected end of JSON input`) en vez de pedir el login. Se corrigió en dos niveles:
- La navegación a "Crear Partido" y "Mis Partidos" ahora pide iniciar sesión directamente si no hay usuario logueado, en vez de dejar que la acción falle después.
- Los botones "Sumarme al partido" (en la lista y en el detalle) muestran "🔐 Iniciar sesión para sumarte" cuando no hay sesión.
- Como red de contención general, el parseo de respuestas de error ahora tolera cuerpos vacíos en todos los métodos de `api.js`.

### Bug de doble ícono en los toasts (encontrado por el usuario)

Casi todos los toasts de la app ya incluían su propio emoji en el mensaje (✅ ❌ ⚠️ ⌛), pero el sistema de notificaciones *además* agregaba un ícono automático aparte — resultando en dos íconos superpuestos (ej: "⚠️⚠️ No pudimos..."). Se sacó el ícono automático y ahora el color del borde del toast indica el tipo (verde/rojo/celeste), sin duplicar el emoji del mensaje.

### Preparado para subir a hosting

- El frontend ya no tiene la URL del backend fija a `localhost:8080`: en local sigue apuntando ahí, pero en cualquier otro dominio asume que el backend está en el mismo origen (`/api`), pensado para un despliegue con reverse proxy o el propio backend sirviendo el frontend.
- El backend ahora lee el puerto desde la variable de entorno `PORT` (con default `8080`), y las credenciales de MySQL (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) y la clave JWT (`JWT_SECRET`) se pueden sobreescribir por variable de entorno sin tocar código — importante porque antes la clave JWT y las credenciales de MySQL quedaban fijas en el repo.
- Se agregó `.gitignore` (target/, .DS_Store, IDE, etc.) y una sección "Despliegue a producción" en [INSTALACION.md](INSTALACION.md).

### Ajustes de estética (a pedido)

- Los inputs de fecha y hora tenían el ícono nativo del navegador invisible sobre el fondo oscuro (color por defecto, sin contraste); se corrigió con un filtro para que se vea, y se agregaron emojis (📅 🕐) a las etiquetas que faltaban en el formulario de "Crear Partido".
- Los `<select>` usaban la flecha nativa del navegador; ahora tienen una flecha propia en verde lima, consistente con el resto del tema.
- La tarjeta de filtros ganó un borde superior de acento y los inputs tienen un estado hover más claro.

## Tercera ronda: despliegue

Se definió el hosting concreto: frontend en Netlify, backend en un servicio que corra Java de forma persistente (Vercel se descartó de entrada porque solo corre funciones serverless, no procesos Java que quedan escuchando en un puerto).

- Se agregó `Back/Dockerfile` (build multi-stage: Maven → JRE liviano), detectable automáticamente por Render, Railway o cualquier plataforma basada en Docker. Se verificó localmente que `mvn clean package` genera exactamente el `.jar` que el Dockerfile espera, y que el jar arranca correctamente respetando la variable `PORT` (no se pudo probar el build de Docker en sí por no haber Docker instalado en este entorno, pero el proceso de build y el jar resultante están verificados).
- Se agregó `netlify.toml` en la raíz para que Netlify publique la carpeta `Front/` sin tener que configurarlo a mano en el dashboard.
- `API_BASE_URL` en `Front/js/api.js` ahora es una constante explícita (`BACKEND_URL`) para completar con la URL real del backend una vez desplegado, ya que frontend y backend quedan en dominios distintos (no se puede asumir mismo origen).

### Cuarta ronda: de Railway/MySQL a Render/Supabase (por disponibilidad real de free tier)

La idea original era Railway para backend + MySQL. Al chequear en vivo, Railway ya no tiene un plan gratuito real (da un crédito de prueba chico y después cobra), así que se buscaron alternativas gratuitas verificando cada una en el momento (no de memoria):

- **Clever Cloud** (MySQL): su página de precios ya no muestra un plan gratuito — parece haberlo discontinuado.
- **db4free.net** (MySQL): al navegar al sitio, redirigió a un dominio completamente distinto con contenido inapropiado/spam — el dominio original parece comprometido o abandonado. **No usar.**
- **Render** (backend): confirmado plan gratuito real vigente (con el conocido trade-off de que el servicio "duerme" tras ~15 min sin tráfico).
- **Supabase** (PostgreSQL): confirmado plan gratuito real vigente (proyectos se pausan tras 1 semana sin actividad, pero no se borran).

En base a eso se cambió la base de datos de producción de MySQL a PostgreSQL:

- Se agregó `Back/src/main/resources/application-postgres.yml` (perfil `postgres`), análogo al de MySQL, usando el mismo contrato de variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) ya establecido. El perfil `mysql` se dejó intacto para quien quiera seguir usándolo en desarrollo local.
- Se instaló PostgreSQL localmente y se corrió el flujo completo (registro de dos usuarios, crear partido, unirse, aceptar solicitud) contra una base Postgres real para confirmar que Hibernate crea el esquema correctamente y que el comportamiento es idéntico al de MySQL/H2. Los 8 tests automatizados se volvieron a correr después del cambio y siguen pasando.
- Se actualizó `docs/INSTALACION.md` con el paso a paso concreto para Render (backend) + Supabase (base) + Netlify (frontend), reemplazando la guía anterior de Railway.

## Quinta ronda: despliegue real, limpieza de código y ajustes de mobile

- Se desplegaron los tres servicios y se dejaron funcionando en producción: backend en Render (Docker), base en Supabase (PostgreSQL) y frontend en Netlify. Se depuró en el camino un `DB_URL` mal formado (le faltaba el `//` después de `jdbc:postgresql:`, lo que hacía que el driver intentara conectarse a `localhost` en vez del host real de Supabase) y un despliegue que fallaba por tener cambios locales (`Dockerfile`, perfil `postgres`, etc.) sin commitear ni pushear al repo.
- Se sacaron todos los comentarios del código (frontend y backend) a pedido explícito.
- Se revisó la app en varios anchos de pantalla (320px, 375px, 768px, desktop). Se encontró y corrigió un bug real: el botón flotante "+" (redundante con la pestaña "Crear" del nav inferior en mobile) quedaba tapando el link "Aviso Legal" del footer al hacer scroll hasta el final en pantallas chicas; ahora ese botón solo se muestra en desktop.
- Se reemplazaron los datos de ejemplo que exponían el nombre real del autor (placeholder del campo "Nombre y Apellido" en el registro, y el usuario mock del modo offline) por valores genéricos.

## Sexta ronda: diagramas y documentación completa

- Se agregó el diagrama de clases (modelo de dominio orientado a objetos: `Usuario`, `Partido`, `Inscripcion`, `EstadoInscripcion`, con atributos, métodos relevantes y multiplicidad de cada asociación) y el diagrama de casos de uso (actor único `Usuario`, 9 casos de uso principales y las relaciones `«include»` hacia "Iniciar sesión" y entre "Gestionar solicitudes" y "Aceptar/Rechazar solicitud"). Ambos se incorporaron en dos formatos: Mermaid en [DIAGRAMAS.md](DIAGRAMAS.md) (se renderiza nativo en GitHub) y como imágenes en la documentación Word.
- Se sincronizó la sección de instalación del documento Word con `INSTALACION.md`: tenía una referencia desactualizada a una constante `API_BASE_URL` fija y no mencionaba el despliegue real a Render/Supabase/Netlify. Se corrigió y se agregó la sección "Despliegue a producción" con la tabla de variables de entorno.
