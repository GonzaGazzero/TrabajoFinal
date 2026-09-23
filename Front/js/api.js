const BACKEND_URL = "https://trabajofinal-om9z.onrender.com/api";

const API_BASE_URL = (() => {
  const { hostname } = window.location;
  const esLocal = hostname === "localhost" || hostname === "127.0.0.1";
  return esLocal ? "http://localhost:8080/api" : BACKEND_URL;
})();

const ApiService = {
  cachedZonas: null,

  async parseJsonSafe(response) {
    try {
      return await response.json();
    } catch (e) {
      return {};
    }
  },

  normalizeMatch(match) {
    if (!match) return match;

    const solicitudes = (match.solicitudesPendientes || []).map(s => ({
      id: s.jugador ? s.jugador.id : null,
      inscripcionId: s.id,
      nombre: s.jugador ? s.jugador.nombre : '',
      iniciales: s.jugador ? s.jugador.avatarIniciales : 'JG',
      telefono: s.jugador ? s.jugador.telefono : '',
      nivel: s.jugador ? s.jugador.nivel : '',
      estado: s.estado,
      mensaje: s.mensaje || null
    }));

    return {
      ...match,
      genero: match.genero || 'Mixto',
      solicitudes
    };
  },

  async getZonasLocalidades() {
    if (this.cachedZonas) return this.cachedZonas;

    const baseDestacadas = [
      "Belgrano (CABA)",
      "Palermo (CABA)",
      "Caballito (CABA)",
      "Recoleta (CABA)",
      "San Isidro (Buenos Aires)",
      "Vicente López (Buenos Aires)",
      "Tigre (Buenos Aires)",
      "Ramos Mejía (Buenos Aires)",
      "Lomas de Zamora (Buenos Aires)",
      "La Plata (Buenos Aires)",
      "Mar del Plata (Buenos Aires)",
      "Rosario (Santa Fe)",
      "Córdoba Capital (Córdoba)",
      "Mendoza Capital (Mendoza)",
      "San Miguel de Tucumán (Tucumán)",
      "Salta Capital (Salta)",
      "Bariloche (Río Negro)",
      "Neuquén Capital (Neuquén)",
      "Montevideo (Uruguay)",
      "Punta del Este (Uruguay)",
      "Santiago (Chile)",
      "Asunción (Paraguay)",
      "Madrid (España)",
      "Barcelona (España)",
      "Miami (EE.UU.)"
    ];

    try {
      const response = await fetch("https://apis.datos.gob.ar/georef/api/localidades?max=1000&orden=nombre");
      if (response.ok) {
        const data = await response.json();
        if (data.localidades && data.localidades.length > 0) {
          const apiLocalidades = data.localidades
            .filter(l => l.nombre && l.nombre.trim().length > 2)
            .map(l => {
              const prov = l.provincia && l.provincia.nombre !== "Ciudad Autónoma de Buenos Aires" ? ` (${l.provincia.nombre})` : ' (CABA)';
              return `${l.nombre.trim()}${prov}`;
            });

          const setUnico = new Set([...baseDestacadas, ...apiLocalidades]);
          this.cachedZonas = Array.from(setUnico);
          return this.cachedZonas;
        }
      }
    } catch (err) {
      console.warn("No se pudo obtener la lista completa de Georef API, usando zonas principales:", err);
    }

    this.cachedZonas = baseDestacadas;
    return this.cachedZonas;
  },

  getToken() {
    return localStorage.getItem('padel_token');
  },

  getHeaders(includeAuth = true) {
    const headers = {
      'Content-Type': 'application/json'
    };
    const token = this.getToken();
    if (includeAuth && token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  },

  async getCurrentUser() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        method: 'GET',
        headers: this.getHeaders(true)
      });

      if (response.ok) {
        const user = await response.json();
        localStorage.setItem('padel_user', JSON.stringify(user));
        return user;
      }
    } catch (err) {
      console.warn("No se pudo obtener usuario de /auth/me:", err);
    }

    const storedUser = localStorage.getItem('padel_user');
    return storedUser ? JSON.parse(storedUser) : null;
  },

  async login(email, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: this.getHeaders(false),
      body: JSON.stringify({ email, password })
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "Credenciales incorrectas");
    }

    if (data.token) localStorage.setItem('padel_token', data.token);
    if (data.user) localStorage.setItem('padel_user', JSON.stringify(data.user));
    return data.user;
  },

  async register(userData) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: this.getHeaders(false),
      body: JSON.stringify({
        nombre: userData.nombre,
        email: userData.email,
        password: userData.password,
        confirmPassword: userData.confirmPassword || userData.password,
        telefono: userData.telefono,
        nivel: userData.nivel
      })
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "Error en el registro");
    }

    if (data.token) localStorage.setItem('padel_token', data.token);
    if (data.user) localStorage.setItem('padel_user', JSON.stringify(data.user));
    return data.user;
  },

  async logout() {
    localStorage.removeItem('padel_token');
    localStorage.removeItem('padel_user');
    return true;
  },

  async buscarZonas(query = '') {
    const q = query.trim().toLowerCase();

    const internacional = [
      "Belgrano (CABA)",
      "Palermo (CABA)",
      "Caballito (CABA)",
      "Recoleta (CABA)",
      "San Isidro (Buenos Aires)",
      "Vicente López (Buenos Aires)",
      "Tigre (Buenos Aires)",
      "Ramos Mejía (Buenos Aires)",
      "Lomas de Zamora (Buenos Aires)",
      "La Plata (Buenos Aires)",
      "Mar del Plata (Buenos Aires)",
      "Rosario (Santa Fe)",
      "Córdoba Capital (Córdoba)",
      "Mendoza Capital (Mendoza)",
      "Monte Buey (Córdoba)",
      "San Miguel de Tucumán (Tucumán)",
      "Salta Capital (Salta)",
      "Bariloche (Río Negro)",
      "Neuquén Capital (Neuquén)",
      "Montevideo (Uruguay)",
      "Punta del Este (Uruguay)",
      "Santiago (Chile)",
      "Asunción (Paraguay)",
      "Madrid (España)",
      "Barcelona (España)",
      "Miami (EE.UU.)"
    ];

    let apiResults = [];
    if (q.length >= 1) {
      try {
        const response = await fetch(`https://apis.datos.gob.ar/georef/api/localidades?nombre=${encodeURIComponent(query)}&max=20`);
        if (response.ok) {
          const data = await response.json();
          if (data.localidades) {
            apiResults = data.localidades.map(l => {
              const prov = l.provincia && l.provincia.nombre !== "Ciudad Autónoma de Buenos Aires" ? ` (${l.provincia.nombre})` : ' (CABA)';
              return `${l.nombre.trim()}${prov}`;
            });
          }
        }
      } catch (err) {
        console.warn("Búsqueda en vivo de Georef:", err);
      }
    }

    const matchesLocal = q ? internacional.filter(c => c.toLowerCase().includes(q)) : internacional;
    const combinados = Array.from(new Set([...matchesLocal, ...apiResults]));
    return combinados.slice(0, 30);
  },

  async obtenerCoordenadasLocalidad(nombreZona) {
    if (!nombreZona) return { lat: -34.6037, lng: -58.3816 };

    let nombre = nombreZona;
    let provincia = '';

    const match = nombreZona.match(/^(.*?)\s*\((.*?)\)$/);
    if (match) {
      nombre = match[1].trim();
      provincia = match[2].trim();
    }

    try {
      let url = `https://apis.datos.gob.ar/georef/api/localidades?nombre=${encodeURIComponent(nombre)}&max=1`;
      if (provincia && provincia.toUpperCase() !== 'CABA') {
        url += `&provincia=${encodeURIComponent(provincia)}`;
      }

      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        if (data.localidades && data.localidades.length > 0 && data.localidades[0].centroide) {
          return {
            lat: data.localidades[0].centroide.lat,
            lng: data.localidades[0].centroide.lon
          };
        }
      }
    } catch (e) {
      console.warn("No se pudieron obtener coordenadas de Georef:", e);
    }
    return { lat: -34.6037, lng: -58.3816 };
  },

  calcularHaversine(lat1, lon1, lat2, lon2) {
    if (!lat1 || !lon1 || !lat2 || !lon2) return null;
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(R * c * 10) / 10;
  },

  async getPartidos(filters = {}) {
    const params = new URLSearchParams();

    if (filters.zona && filters.zona !== "Todas las zonas" && filters.zona.trim() !== "") {
      params.append('zona', filters.zona.trim());
    }
    if (filters.fecha) {
      params.append('fecha', filters.fecha);
    }
    if (filters.nivel && filters.nivel !== "Todos los niveles") {
      params.append('nivel', filters.nivel);
    }
    if (filters.genero && filters.genero !== "Todos") {
      params.append('genero', filters.genero);
    }
    if (filters.sortByDistance) {
      params.append('orden', 'cercania');
    }
    if (filters.lat) params.append('lat', filters.lat);
    if (filters.lng) params.append('lng', filters.lng);

    const response = await fetch(`${API_BASE_URL}/partidos?${params.toString()}`, {
      method: 'GET',
      headers: this.getHeaders(true)
    });

    if (!response.ok) {
      throw new Error("No se pudo cargar el listado de partidos");
    }

    const matches = await response.json();
    return matches.map(m => this.normalizeMatch(m));
  },

  async getPartidoById(id) {
    const response = await fetch(`${API_BASE_URL}/partidos/${id}`, {
      method: 'GET',
      headers: this.getHeaders(true)
    });

    if (!response.ok) {
      throw new Error("Partido no encontrado");
    }

    return this.normalizeMatch(await response.json());
  },

  async createPartido(partidoData) {
    const coords = await this.obtenerCoordenadasLocalidad(partidoData.zona);
    const cuposFaltantesNum = parseInt(partidoData.cuposFaltantes) || 1;

    const response = await fetch(`${API_BASE_URL}/partidos`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify({
        fecha: partidoData.fecha,
        hora: partidoData.hora,
        cancha: partidoData.cancha,
        direccion: partidoData.direccion || "Dirección a confirmar",
        zona: partidoData.zona,
        nivel: partidoData.nivel,
        genero: partidoData.genero || 'Mixto',
        jugadoresFaltantes: cuposFaltantesNum,
        precioPersona: partidoData.precioPersona || "$4.000",
        tipoCancha: partidoData.tipoCancha || "Césped Sintético con Cristal",
        latitud: coords.lat,
        longitud: coords.lng
      })
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "No se pudo crear el partido. Verificá que hayas iniciado sesión.");
    }

    return this.normalizeMatch(data);
  },

  async unirseAPartido(partidoId, mensaje) {
    const response = await fetch(`${API_BASE_URL}/partidos/${partidoId}/unirse`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify({ mensaje: mensaje || null })
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "Error al unirse al partido");
    }

    return this.normalizeMatch(data);
  },

  async responderSolicitud(partidoId, solicitud, accion) {
    const inscripcionId = solicitud && solicitud.inscripcionId;
    const endpoint = accion === 'ACEPTAR' ? 'aceptar' : 'rechazar';

    const response = await fetch(`${API_BASE_URL}/partidos/${partidoId}/inscripciones/${inscripcionId}/${endpoint}`, {
      method: 'POST',
      headers: this.getHeaders(true)
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "Error al responder la solicitud");
    }

    return this.normalizeMatch(data);
  },

  async salirDePartido(partidoId) {
    const response = await fetch(`${API_BASE_URL}/partidos/${partidoId}/salir`, {
      method: 'POST',
      headers: this.getHeaders(true)
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "No se pudo salir del partido");
    }

    return data;
  },

  async eliminarPartido(partidoId) {
    const response = await fetch(`${API_BASE_URL}/partidos/${partidoId}`, {
      method: 'DELETE',
      headers: this.getHeaders(true)
    });

    const data = await this.parseJsonSafe(response);

    if (!response.ok) {
      throw new Error(data.message || data.error || "No se pudo eliminar el partido");
    }

    return data;
  },

  async getMisPartidos() {
    const response = await fetch(`${API_BASE_URL}/partidos/mis-partidos`, {
      method: 'GET',
      headers: this.getHeaders(true)
    });

    if (!response.ok) {
      throw new Error("No se pudo cargar tus partidos");
    }

    const data = await response.json();
    return {
      creados: (data.creados || []).map(m => this.normalizeMatch(m)),
      unidos: (data.unidos || []).map(m => this.normalizeMatch(m))
    };
  },

  buildWhatsAppUrl(telefono, partidoInfo) {
    const num = telefono || "5491100000000";
    const text = `¡Hola ${partidoInfo.organizador ? partidoInfo.organizador.nombre : ''}! Vi tu partido de pádel en CuartaPala para el ${partidoInfo.fecha} a las ${partidoInfo.hora}hs en ${partidoInfo.cancha} y me gustaría coordinar.`;
    return `https://wa.me/${num}?text=${encodeURIComponent(text)}`;
  }
};
