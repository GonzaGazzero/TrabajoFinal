# CuartaPala — PadelConnect

Plataforma web para coordinar partidos de pádel: publicar partidos, buscar por zona/fecha/nivel/género, sumarse a un partido con cupos disponibles y gestionar las solicitudes de ingreso como organizador.

Trabajo Final — Tecnicatura Universitaria en Programación.

## Stack técnico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 3.3.2 (Web, Data JPA, Security, Validation) |
| Autenticación | JWT (JJWT 0.12.6) + BCrypt |
| Base de datos | H2 en memoria (por defecto) o MySQL / PostgreSQL (opcional) |
| Frontend | HTML + CSS + JavaScript vanilla (sin frameworks ni build step) |
| Geocodificación | API pública Georef (`apis.datos.gob.ar`) |
| Tests | JUnit 5 + Spring Boot Test (8 tests, todos verdes) |

## Estructura del repositorio

```
Trabajo final/
├── Back/                  # API REST (Spring Boot)
│   └── src/main/java/com/padelconnect/
│       ├── controller/    # Endpoints REST
│       ├── service/       # Lógica de negocio
│       ├── repository/    # Acceso a datos (Spring Data JPA)
│       ├── entity/        # Entidades JPA
│       ├── dto/           # Objetos de transferencia
│       ├── config/        # Seguridad, JWT, CORS, i18n
│       └── exception/     # Manejo centralizado de errores
├── Front/                 # Cliente web (SPA sin build)
│   ├── index.html
│   ├── css/styles.css
│   └── js/{api.js, app.js, mockData.js}
└── docs/                  # Documentación del proyecto
    ├── ARQUITECTURA.md
    ├── API.md
    ├── BASE_DE_DATOS.md
    ├── DIAGRAMAS.md
    ├── INSTALACION.md
    └── CAMBIOS_REALIZADOS.md
```

## Puesta en marcha rápida

```bash
# 1) Backend (usa H2 en memoria por defecto, no requiere instalar nada)
cd Back
mvn spring-boot:run

# 2) Frontend (cualquier servidor estático; por ejemplo)
cd Front
python3 -m http.server 5500
# abrir http://localhost:5500
```

Ver [docs/INSTALACION.md](docs/INSTALACION.md) para el detalle completo, incluyendo cómo usar MySQL en vez de H2.

## Documentación

- [Arquitectura](docs/ARQUITECTURA.md) — capas, seguridad, flujo de datos.
- [API REST](docs/API.md) — referencia completa de endpoints.
- [Modelo de datos](docs/BASE_DE_DATOS.md) — entidades, relaciones y reglas de negocio.
- [Diagramas](docs/DIAGRAMAS.md) — diagrama de clases y de casos de uso.
- [Instalación y ejecución](docs/INSTALACION.md) — guía paso a paso.
- [Revisión y cambios realizados](docs/CAMBIOS_REALIZADOS.md) — auditoría de código y fixes aplicados.

## Estado del proyecto

✅ 8/8 tests automatizados pasando · ✅ Compila sin errores · ✅ Flujo completo (registro, login, crear partido, unirse, aceptar/rechazar solicitud) verificado de punta a punta contra el backend real.
