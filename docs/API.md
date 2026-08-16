# API REST — Referencia

Base URL: `http://localhost:8080/api`

Autenticación: header `Authorization: Bearer <token>` (JWT obtenido en login/registro). Los endpoints marcados **🔒** lo requieren; los marcados **🌐** son públicos (pero si se manda un token válido, el backend igual identifica al usuario para calcular su relación con el partido).

Todas las respuestas de error tienen este formato (`ErrorResponseDTO`):

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "descripción legible del error",
  "timestamp": "2026-08-15T20:00:00"
}
```

---

## Autenticación — `/api/auth`

### `POST /api/auth/register` 🌐

Crea una cuenta y devuelve el token de sesión.

**Body:**
```json
{
  "nombre": "Gonzalo Gazzero",
  "email": "gonzalo@ejemplo.com",
  "password": "123456",
  "confirmPassword": "123456",
  "telefono": "5491134567890",
  "nivel": "5ta Categoría"
}
```
`telefono` y `nivel` son opcionales (si se omiten, el backend asigna valores por defecto). `confirmPassword` es obligatorio y debe coincidir con `password`.

**201 Created:**
```json
{
  "token": "eyJhbGciOi...",
  "user": {
    "id": 1, "nombre": "Gonzalo Gazzero", "email": "gonzalo@ejemplo.com",
    "telefono": "5491134567890", "nivel": "5ta Categoría", "avatarIniciales": "GG"
  }
}
```

**400** si el email ya existe o las contraseñas no coinciden.

### `POST /api/auth/login` 🌐

**Body:** `{ "email": "...", "password": "..." }`
**200 OK:** mismo formato que el registro (`token` + `user`).
**401** si las credenciales son inválidas.

### `GET /api/auth/me` 🔒

Devuelve el perfil del usuario autenticado (`UserDto`).

---

## Partidos — `/api/partidos`

### `GET /api/partidos` 🌐

Lista partidos con filtros opcionales por query string.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `zona` | string | Filtra por zona/localidad exacta. |
| `fecha` | string (`yyyy-MM-dd`) | Filtra por fecha exacta. |
| `nivel` | string | Filtra por nivel de juego exacto. |
| `genero` | string | Filtra por categoría/género (`Mixto`, `Masculino`, `Femenino`). |
| `lat`, `lng` | double | Coordenadas del usuario; si se pasan, cada partido incluye `distanciaKm` (fórmula de Haversine). |
| `orden` | string | `cercania` ordena el resultado por `distanciaKm` ascendente. |

**200 OK:** array de `PartidoResponseDTO` (ver forma completa más abajo).

### `GET /api/partidos/{id}` 🌐

Detalle de un partido. Si se manda el JWT, incluye `estadoInscripcionUsuario` relativo a ese usuario.

### `GET /api/partidos/mis-partidos` 🔒

```json
{ "creados": [ /* PartidoResponseDTO[] organizados por el usuario */ ],
  "unidos":  [ /* PartidoResponseDTO[] a los que se unió, sin contar los propios */ ] }
```

### `POST /api/partidos` 🔒

Crea un partido. El organizador queda inscripto automáticamente con estado `ACEPTADO`.

**Body (`PartidoRequestDTO`):**
```json
{
  "fecha": "2026-09-05", "hora": "19:00",
  "cancha": "Club Belgrano", "direccion": "Av. Cabildo 2450",
  "zona": "Belgrano (CABA)", "nivel": "5ta Categoría", "genero": "Mixto",
  "jugadoresFaltantes": 1,
  "precioPersona": "$4.000", "tipoCancha": "Césped Sintético"
}
```
`latitud`/`longitud` son opcionales — si no se mandan, el backend geocodifica `zona` automáticamente. Los cupos totales se calculan como `jugadoresFaltantes + 1` (o `4` por defecto).

**201 Created:** `PartidoResponseDTO`.

### `POST /api/partidos/{id}/unirse` 🔒

El usuario solicita sumarse. Queda con estado `PENDIENTE` hasta que el organizador lo apruebe (salvo que el partido no tenga cupos, en cuyo caso responde `409 Conflict`).

### `POST /api/partidos/{id}/inscripciones/{inscripcionId}/aceptar` 🔒

Solo el organizador del partido puede aceptar. Pasa la inscripción a `ACEPTADO`. `400` si quien llama no es el organizador, `409` si ya no hay cupos.

### `POST /api/partidos/{id}/inscripciones/{inscripcionId}/rechazar` 🔒

Solo el organizador. Pasa la inscripción a `RECHAZADO`.

### `POST /api/partidos/{id}/salir` 🔒

El usuario elimina su propia inscripción (cualquier estado). `400` si no estaba anotado.

---

## Forma de `PartidoResponseDTO`

```json
{
  "id": 1,
  "fecha": "2026-09-05", "hora": "19:00",
  "cancha": "Club Belgrano", "direccion": "Av. Cabildo 2450",
  "zona": "Belgrano (CABA)", "latitud": -34.5601, "longitud": -58.456,
  "nivel": "5ta Categoría", "genero": "Mixto",
  "cuposTotales": 4, "cuposDisponibles": 2, "jugadoresFaltantes": 2,
  "distanciaKm": 3.2,
  "precioPersona": "$4.000", "tipoCancha": "Césped Sintético",
  "organizador": { "id": 1, "nombre": "...", "email": "...", "telefono": "...", "nivel": "...", "avatarIniciales": "GG" },
  "jugadores": [ { "id": 1, "nombre": "...", "iniciales": "GG", "rol": "Organizador" } ],
  "solicitudesPendientes": [
    { "id": 5, "jugador": { "...UserDto..." }, "estado": "PENDIENTE", "fechaInscripcion": "2026-08-15T20:00:00" }
  ],
  "estadoInscripcionUsuario": "ORGANIZADOR"
}
```

`estadoInscripcionUsuario` (relativo a quien hace la request) puede ser `ORGANIZADOR`, `ACEPTADO`, `PENDIENTE`, `RECHAZADO` o `NINGUNA`.

> **Nota de integración:** el frontend (`api.js`) adapta `solicitudesPendientes` a un array plano `solicitudes` antes de renderizar, para simplificar la UI. Ver `ApiService.normalizeMatch()`.

---

## Códigos de estado usados

| Código | Cuándo |
|---|---|
| `200 OK` | Operación exitosa (GET, o POST que no crea recurso). |
| `201 Created` | Registro o partido creado. |
| `400 Bad Request` | Validación fallida, contraseñas no coinciden, usuario no autorizado para la acción. |
| `401 Unauthorized` | Credenciales inválidas en login. |
| `404 Not Found` | Partido o inscripción inexistente. |
| `409 Conflict` | Sin cupos disponibles. |
| `500 Internal Server Error` | Error no controlado (`GlobalExceptionHandler` lo captura como fallback). |
