const MOCK_CURRENT_USER = {
  id: 101,
  nombre: "Gonzalo Gazzero",
  email: "gonzalo@ejemplo.com",
  telefono: "5491134567890",
  nivel: "Intermedio (4ta-5ta)",
  avatarIniciales: "GG"
};

const MOCK_ZONAS = [
  "Todas las zonas",
  "Belgrano (CABA)",
  "San Isidro (Buenos Aires)",
  "Caballito (CABA)",
  "Ramos Mejía (Buenos Aires)",
  "Lomas de Zamora (Buenos Aires)",
  "Rosario (Santa Fe)",
  "Córdoba Capital (Córdoba)",
  "Mendoza Capital (Mendoza)",
  "Montevideo (Uruguay)",
  "Madrid (España)"
];

const MOCK_NIVELES = [
  "Todos los niveles",
  "8va Categoría (Iniciación)",
  "7ma Categoría",
  "6ta Categoría",
  "5ta Categoría",
  "4ta Categoría",
  "3ra Categoría",
  "2da Categoría",
  "1ra Categoría (Profesional / Pro)"
];

const INITIAL_MATCHES = [];

function getStoredMatches() {
  const stored = localStorage.getItem('padel_matches');
  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      if (Array.isArray(parsed)) return parsed;
    } catch(e) {}
  }
  localStorage.setItem('padel_matches', JSON.stringify(INITIAL_MATCHES));
  return INITIAL_MATCHES;
}

function saveStoredMatches(matches) {
  localStorage.setItem('padel_matches', JSON.stringify(matches));
}

let MOCK_MATCHES = getStoredMatches();

function getStoredUsers() {
  const stored = localStorage.getItem('padel_users');
  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    } catch(e) {}
  }
  const defaults = [MOCK_CURRENT_USER];
  localStorage.setItem('padel_users', JSON.stringify(defaults));
  return defaults;
}

function saveStoredUser(user) {
  const users = getStoredUsers();
  const index = users.findIndex(u => u.email.toLowerCase() === user.email.toLowerCase());
  if (index >= 0) {
    users[index] = user;
  } else {
    users.push(user);
  }
  localStorage.setItem('padel_users', JSON.stringify(users));
}
