document.addEventListener('DOMContentLoaded', () => {
  let currentFilters = {
    zona: '',
    fecha: '',
    nivel: 'Todos los niveles',
    genero: 'Todos',
    sortByDistance: false
  };

  let activeMyMatchesTab = 'unidos';
  let currentSelectedMatchId = null;

  initApp();

  async function initApp() {
    await populateSelectOptions();
    await updateHeaderUserStatus();
    await renderMatchesList();
    setupEventListeners();

    const createDateInput = document.getElementById('create-fecha');
    if (createDateInput) {
      const today = new Date().toISOString().split('T')[0];
      createDateInput.value = today;
    }

    ['filter-fecha', 'create-fecha', 'create-hora'].forEach(id => {
      const input = document.getElementById(id);
      if (!input) return;
      const wrap = input.closest('.datetime-wrap');
      if (!wrap) return;
      const refresh = () => wrap.classList.toggle('has-value', !!input.value);
      input.addEventListener('input', refresh);
      input.addEventListener('change', refresh);
      refresh();
    });
  }

  async function populateSelectOptions() {
    const selectNivel = document.getElementById('filter-nivel');
    const selectCreateNivel = document.getElementById('create-nivel');

    if (selectNivel) {
      selectNivel.innerHTML = MOCK_NIVELES.map(n => `<option value="${n}">${n}</option>`).join('');
    }

    if (selectCreateNivel) {
      selectCreateNivel.innerHTML = MOCK_NIVELES.filter(n => n !== "Todos los niveles")
        .map(n => `<option value="${n}">${n}</option>`).join('');
    }
  }

  function setupAutocomplete(inputId, dropdownId, onSelectCallback) {
    const input = document.getElementById(inputId);
    const dropdown = document.getElementById(dropdownId);
    if (!input || !dropdown) return;

    let debounceTimer = null;

    async function renderDropdown(query = '') {
      const zonas = await ApiService.buscarZonas(query);

      if (zonas.length === 0) {
        dropdown.innerHTML = query.trim().length > 0 
          ? `<div class="autocomplete-no-results">Sin coincidencias exactas. Se usará "<strong>${query}</strong>"</div>`
          : `<div class="autocomplete-no-results">Escribí para buscar ciudades de Argentina o el mundo...</div>`;
      } else {
        dropdown.innerHTML = zonas.map(z => `
          <div class="autocomplete-item" data-value="${z}">
            <span class="autocomplete-item-icon">📍</span>
            <span>${z}</span>
          </div>
        `).join('');
      }

      dropdown.querySelectorAll('.autocomplete-item').forEach(item => {
        item.addEventListener('mousedown', (e) => {
          e.preventDefault();
          const val = item.dataset.value;
          input.value = val;
          dropdown.classList.remove('active');
          if (onSelectCallback) onSelectCallback(val);
        });
      });

      dropdown.classList.add('active');
    }

    input.addEventListener('focus', () => {
      renderDropdown(input.value);
    });

    input.addEventListener('input', (e) => {
      const val = e.target.value;
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        renderDropdown(val);
        if (onSelectCallback) onSelectCallback(val);
      }, 150);
    });

    input.addEventListener('blur', () => {
      setTimeout(() => dropdown.classList.remove('active'), 200);
    });
  }

  function switchView(targetViewId) {
    const views = document.querySelectorAll('.view');
    views.forEach(v => v.classList.remove('active'));

    const activeView = document.getElementById(targetViewId);
    if (activeView) {
      activeView.classList.add('active');
    }

    const navLinks = document.querySelectorAll('.nav-link, .mobile-nav-item');
    navLinks.forEach(link => {
      if (link.dataset.view === targetViewId) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });

    if (targetViewId === 'view-my-matches') {
      renderMyMatches();
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function renderMatchesList() {
    const container = document.getElementById('matches-list-container');
    if (!container) return;

    try {
      const matches = await ApiService.getPartidos(currentFilters);

      if (matches.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">🔍</div>
            <div class="empty-state-title">No encontramos partidos</div>
            <div class="empty-state-desc">Prueba cambiando los filtros de zona o fecha para ver más partidos convocados.</div>
          </div>
        `;
        return;
      }

      const currentUser = await ApiService.getCurrentUser();

      container.innerHTML = matches.map(match => {
        const isFull = match.cuposDisponibles <= 0;
        const isJoined = match.estadoInscripcionUsuario
          ? match.estadoInscripcionUsuario === 'ACEPTADO'
          : !!(currentUser && match.jugadores && match.jugadores.some(j => j.id === currentUser.id || j.nombre === currentUser.nombre));
        const isPending = match.estadoInscripcionUsuario
          ? match.estadoInscripcionUsuario === 'PENDIENTE'
          : !!(currentUser && match.solicitudes && match.solicitudes.some(s => (s.id === currentUser.id || s.nombre === currentUser.nombre) && s.estado === 'PENDIENTE'));
        const isOrganizer = match.estadoInscripcionUsuario
          ? match.estadoInscripcionUsuario === 'ORGANIZADOR'
          : !!(currentUser && match.organizador && (match.organizador.id === currentUser.id || match.organizador.nombre === currentUser.nombre));

        let bubblesHtml = '';
        for (let i = 0; i < match.cuposTotales; i++) {
          if (i < match.jugadores.length) {
            bubblesHtml += `<div class="player-bubble" title="${match.jugadores[i].nombre}">${match.jugadores[i].iniciales}</div>`;
          } else {
            bubblesHtml += `<div class="player-bubble empty" title="Cupo libre">+</div>`;
          }
        }

        let actionButtonHtml = '';
        if (!currentUser) {
          actionButtonHtml = `
            <button class="btn btn-outline btn-full btn-require-login">
              🔐 Iniciar sesión para sumarte
            </button>
          `;
        } else if (isJoined) {
          actionButtonHtml = `
            <button class="btn btn-whatsapp btn-full btn-contact-wa" data-id="${match.id}">
              💬 Chat WhatsApp
            </button>
          `;
        } else if (isPending) {
          actionButtonHtml = `
            <button class="btn btn-outline btn-full" disabled style="opacity:0.85; border-color:var(--warning); color:var(--warning);">
              ⌛ Solicitud Pendiente
            </button>
          `;
        } else if (isOrganizer) {
          const pendingCount = (match.solicitudes || []).filter(s => s.estado === 'PENDIENTE').length;
          actionButtonHtml = `
            <button class="btn btn-primary btn-full btn-view-detail" data-id="${match.id}">
              ⚙️ ${pendingCount > 0 ? `Solicitudes (${pendingCount})` : 'Mi Partido'}
            </button>
          `;
        } else {
          actionButtonHtml = `
            <button class="btn btn-primary btn-full btn-quick-join" data-id="${match.id}" ${isFull ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
              ${isFull ? 'Sin Cupo' : 'Sumarme al partido'}
            </button>
          `;
        }

        return `
          <article class="match-card">
            <div class="match-card-header">
              <div class="match-time-badge">
                <span>📅 ${formatDate(match.fecha)} - ${match.hora} hs</span>
              </div>
              <div style="display:flex; gap:6px; flex-wrap:wrap; justify-content:flex-end;">
                <span class="badge-level">${match.nivel}</span>
                <span class="badge-level" style="background: rgba(0, 229, 255, 0.12); color: var(--secondary); border: 1px solid var(--secondary);">
                  ${match.genero === 'Masculino' ? '👨 Masculino' : match.genero === 'Femenino' ? '👩 Femenino' : '👫 Mixto'}
                </span>
              </div>
            </div>

            <div class="match-location">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>${match.cancha}</span>
              ${match.distanciaKm ? `<span class="match-distance">📍 a ${match.distanciaKm} km</span>` : ''}
            </div>

            <div class="match-details-grid">
              <div class="detail-item">
                <span class="label">Zona</span>
                <span class="value">${match.zona}</span>
              </div>
              <div class="detail-item">
                <span class="label">Costo por persona</span>
                <span class="value">${match.precioPersona || '$4.000'}</span>
              </div>
            </div>

            <div class="spots-container">
              <div class="spots-avatars">
                ${bubblesHtml}
              </div>
              <div class="spots-text ${isFull ? 'full' : 'has-spots'}">
                ${isFull ? '¡Partido Lleno!' : `Faltan ${match.cuposDisponibles} jugador(es)`}
              </div>
            </div>

            <div class="match-card-actions">
              <button class="btn btn-outline btn-full btn-view-detail" data-id="${match.id}">
                Ver Detalle
              </button>
              ${actionButtonHtml}
            </div>
          </article>
        `;
      }).join('');

      container.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', () => openMatchDetailModal(btn.dataset.id));
      });

      container.querySelectorAll('.btn-quick-join').forEach(btn => {
        btn.addEventListener('click', () => handleQuickJoin(btn.dataset.id));
      });

      container.querySelectorAll('.btn-contact-wa').forEach(btn => {
        btn.addEventListener('click', () => openMatchWhatsApp(btn.dataset.id));
      });

      container.querySelectorAll('.btn-require-login').forEach(btn => {
        btn.addEventListener('click', () => openAuthModal('login'));
      });

    } catch (err) {
      console.error("Error al renderizar listado:", err);
      showToast("❌ Error al cargar los partidos", "danger");
    }
  }

  async function openMatchDetailModal(matchId) {
    currentSelectedMatchId = matchId;
    const modal = document.getElementById('modal-detail');
    const modalBody = document.getElementById('modal-match-body');

    try {
      const match = await ApiService.getPartidoById(matchId);
      const currentUser = await ApiService.getCurrentUser();
      const isJoined = match.estadoInscripcionUsuario
        ? match.estadoInscripcionUsuario === 'ACEPTADO'
        : !!(currentUser && match.jugadores.some(j => j.id === currentUser.id || j.nombre === currentUser.nombre));
      const isPending = match.estadoInscripcionUsuario
        ? match.estadoInscripcionUsuario === 'PENDIENTE'
        : !!(currentUser && match.solicitudes && match.solicitudes.some(s => (s.id === currentUser.id || s.nombre === currentUser.nombre) && s.estado === 'PENDIENTE'));
      const isFull = match.cuposDisponibles <= 0;
      const isOrganizer = match.estadoInscripcionUsuario
        ? match.estadoInscripcionUsuario === 'ORGANIZADOR'
        : !!(currentUser && match.organizador && (match.organizador.id === currentUser.id || match.organizador.nombre === currentUser.nombre));

      let playersListHtml = match.jugadores.map(j => `
        <div class="player-item">
          <div class="player-info">
            <div class="player-avatar-large">${j.iniciales}</div>
            <div>
              <div class="player-name">${j.nombre}</div>
              ${j.rol === 'Organizador' ? `<span class="player-role">👑 Organizador</span>` : ''}
            </div>
          </div>
          <span style="font-size: 0.8rem; color: var(--success); font-weight: 600;">Confirmado</span>
        </div>
      `).join('');

      let pendingSectionHtml = '';
      if (isOrganizer && match.solicitudes && match.solicitudes.length > 0) {
        const pendingList = match.solicitudes.filter(s => s.estado === 'PENDIENTE');
        if (pendingList.length > 0) {
          pendingSectionHtml = `
            <div class="pending-section">
              <div class="pending-title">
                <span>⌛</span> Solicitudes Pendientes de Aprobación (${pendingList.length})
              </div>
              <div style="display:flex; flex-direction:column; gap:8px;">
                ${pendingList.map(s => `
                  <div class="applicant-card">
                    <div class="applicant-info">
                      <div class="player-avatar-large">${s.iniciales || 'JG'}</div>
                      <div>
                        <div class="player-name">${s.nombre}</div>
                        <div style="font-size:0.75rem; color:var(--text-muted);">${s.nivel || match.nivel} • 📱 ${s.telefono || 'Sin cel'}</div>
                      </div>
                    </div>
                    <div class="applicant-actions">
                      <button class="btn btn-primary btn-sm btn-aceptar-solicitud" data-candidatoid="${s.id}" data-inscripcionid="${s.inscripcionId || ''}">
                        ✅ Aceptar
                      </button>
                      <button class="btn btn-outline btn-sm btn-rechazar-solicitud" data-candidatoid="${s.id}" data-inscripcionid="${s.inscripcionId || ''}" style="border-color:var(--danger); color:var(--danger);">
                        ❌ Rechazar
                      </button>
                    </div>
                  </div>
                `).join('')}
              </div>
            </div>
          `;
        }
      }

      let bottomActionHtml = '';
      if (!currentUser) {
        bottomActionHtml = `
          <button class="btn btn-primary btn-full btn-require-login-modal">
            🔐 Iniciar sesión para sumarte
          </button>
        `;
      } else if (isJoined) {
        bottomActionHtml = `
          <a href="${ApiService.buildWhatsAppUrl(match.organizador.telefono, match)}" target="_blank" class="btn btn-whatsapp btn-full">
            💬 Abrir WhatsApp del Organizador (${match.organizador.nombre})
          </a>
          <div style="text-align:center; font-size:0.85rem; color:var(--success); font-weight:700;">
            ✅ ¡Ya estás confirmado en este partido!
          </div>
        `;
      } else if (isPending) {
        bottomActionHtml = `
          <div style="text-align:center; padding:12px; background:rgba(245, 158, 11, 0.1); border:1px solid var(--warning); border-radius:var(--radius-sm); color:var(--warning); font-size:0.875rem; font-weight:600;">
            ⌛ Tu solicitud para unirte a este partido fue enviada al organizador y está pendiente de aprobación.
          </div>
        `;
      } else if (isOrganizer) {
        bottomActionHtml = `
          <div style="text-align:center; font-size:0.85rem; color:var(--primary); font-weight:700;">
            👑 Eres el organizador de este partido.
          </div>
        `;
      } else {
        bottomActionHtml = `
          <button class="btn btn-primary btn-full" id="btn-modal-join" ${isFull ? 'disabled style="opacity:0.5;"' : ''}>
            ${isFull ? '¡Partido Lleno!' : 'Sumarme a este Partido'}
          </button>
        `;
      }

      modalBody.innerHTML = `
        <div style="display:flex; flex-direction:column; gap: 16px;">
          
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span class="badge-level">${match.nivel}</span>
            <span style="font-size: 0.9rem; font-weight:700; color: var(--primary);">
              📅 ${formatDate(match.fecha)} a las ${match.hora} hs
            </span>
          </div>

          <div>
            <h3 style="font-size: 1.25rem; font-weight:800; color: var(--text-main);">${match.cancha}</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem;">📍 ${match.direccion || match.zona}</p>
          </div>

          <div class="match-details-grid">
            <div class="detail-item">
              <span class="label">Zona</span>
              <span class="value">${match.zona}</span>
            </div>
            <div class="detail-item">
              <span class="label">Categoría / Género</span>
              <span class="value">${match.genero === 'Masculino' ? '👨 Masculino' : match.genero === 'Femenino' ? '👩 Femenino' : '👫 Mixto'}</span>
            </div>
            <div class="detail-item">
              <span class="label">Faltan Cubrir</span>
              <span class="value" style="color:var(--primary); font-weight:700;">${match.cuposDisponibles} jugador(es)</span>
            </div>
            <div class="detail-item">
              <span class="label">Precio por Persona</span>
              <span class="value">${match.precioPersona || '$4.000'}</span>
            </div>
          </div>

          <div>
            <h4 style="font-size: 0.95rem; font-weight:700; margin-bottom: 10px; color: var(--text-main);">
              👥 Jugadores Confirmados (${match.jugadores.length}/${match.cuposTotales})
            </h4>
            <div class="players-list">
              ${playersListHtml}
            </div>
          </div>

          ${pendingSectionHtml}

          <div style="display:flex; flex-direction:column; gap:10px; margin-top:10px;">
            ${bottomActionHtml}
          </div>

        </div>
      `;

      modal.classList.add('active');

      modalBody.querySelectorAll('.btn-aceptar-solicitud').forEach(btn => {
        btn.addEventListener('click', async () => {
          try {
            const solicitud = { id: btn.dataset.candidatoid, inscripcionId: btn.dataset.inscripcionid || null };
            await ApiService.responderSolicitud(matchId, solicitud, 'ACEPTAR');
            showToast("✅ Jugador aceptado en el partido", "success");
            await renderMatchesList();
            openMatchDetailModal(matchId);
          } catch (err) {
            showToast(`⚠️ ${err.message}`, "danger");
          }
        });
      });

      modalBody.querySelectorAll('.btn-rechazar-solicitud').forEach(btn => {
        btn.addEventListener('click', async () => {
          try {
            const solicitud = { id: btn.dataset.candidatoid, inscripcionId: btn.dataset.inscripcionid || null };
            await ApiService.responderSolicitud(matchId, solicitud, 'RECHAZAR');
            showToast("ℹ️ Solicitud rechazada", "info");
            await renderMatchesList();
            openMatchDetailModal(matchId);
          } catch (err) {
            showToast(`⚠️ ${err.message}`, "danger");
          }
        });
      });

      const requireLoginBtn = modalBody.querySelector('.btn-require-login-modal');
      if (requireLoginBtn) {
        requireLoginBtn.addEventListener('click', () => {
          modal.classList.remove('active');
          openAuthModal('login');
        });
      }

      const joinBtn = document.getElementById('btn-modal-join');
      if (joinBtn && !isFull && !isJoined && !isPending && !isOrganizer) {
        joinBtn.addEventListener('click', async () => {
          await handleQuickJoin(matchId);
          openMatchDetailModal(matchId);
        });
      }

    } catch (err) {
      console.error(err);
      showToast("❌ No se pudo cargar el detalle del partido", "danger");
    }
  }

  function openMatchWhatsApp(matchId) {
    ApiService.getPartidoById(matchId).then(match => {
      const url = ApiService.buildWhatsAppUrl(match.organizador.telefono, match);
      window.open(url, '_blank');
    });
  }

  async function handleQuickJoin(matchId) {
    try {
      const updatedMatch = await ApiService.unirseAPartido(matchId);
      showToast(`⌛ Solicitud enviada al creador del partido (${updatedMatch.cancha}). Quedó en estado pendiente.`, "success");
      await renderMatchesList();
    } catch (err) {
      showToast(`⚠️ ${err.message}`, "danger");
    }
  }

  async function renderMyMatches() {
    const container = document.getElementById('my-matches-list');
    if (!container) return;

    try {
      const { creados, unidos } = await ApiService.getMisPartidos();
      const listToRender = activeMyMatchesTab === 'creados' ? creados : unidos;

      if (listToRender.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">🎾</div>
            <div class="empty-state-title">
              ${activeMyMatchesTab === 'creados' ? 'No has creado partidos aún' : 'No estás anotado en ningún partido'}
            </div>
            <div class="empty-state-desc">
              ${activeMyMatchesTab === 'creados' ? 'Haz clic en "Crear Partido" para organizar tu primer partido.' : 'Explora el listado para sumarte a un partido abierto.'}
            </div>
          </div>
        `;
        return;
      }

      container.innerHTML = listToRender.map(match => `
        <article class="match-card">
          <div class="match-card-header">
            <div class="match-time-badge">📅 ${formatDate(match.fecha)} - ${match.hora} hs</div>
            <span class="badge-level">${match.nivel}</span>
          </div>

          <div class="match-location">
            <span>${match.cancha}</span>
          </div>

          <div class="match-details-grid">
            <div class="detail-item">
              <span class="label">Zona</span>
              <span class="value">${match.zona}</span>
            </div>
            <div class="detail-item">
              <span class="label">Jugadores</span>
              <span class="value">${match.jugadores.length} / ${match.cuposTotales}</span>
            </div>
          </div>

          <div class="match-card-actions">
            <button class="btn btn-outline btn-full btn-view-detail" data-id="${match.id}">Ver Detalle</button>
            <button class="btn btn-whatsapp btn-full btn-contact-wa" data-id="${match.id}">WhatsApp Organizador</button>
          </div>
        </article>
      `).join('');

      container.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', () => openMatchDetailModal(btn.dataset.id));
      });

      container.querySelectorAll('.btn-contact-wa').forEach(btn => {
        btn.addEventListener('click', () => openMatchWhatsApp(btn.dataset.id));
      });

    } catch (err) {
      console.error(err);
      showToast("❌ Error al cargar tus partidos", "danger");
    }
  }

  async function updateHeaderUserStatus() {
    const userContainer = document.getElementById('user-status-container');
    if (!userContainer) return;

    const user = await ApiService.getCurrentUser();

    if (user) {
      userContainer.innerHTML = `
        <div class="user-avatar" id="btn-user-profile" title="${user.nombre} (${user.email})">
          ${user.avatarIniciales || 'GG'}
        </div>
      `;

      document.getElementById('btn-user-profile').addEventListener('click', () => {
        if (confirm(`Sesión iniciada como ${user.nombre}.\n¿Deseas cerrar sesión?`)) {
          ApiService.logout().then(() => {
            updateHeaderUserStatus();
            showToast("👋 Sesión cerrada", "success");
          });
        }
      });
    } else {
      userContainer.innerHTML = `
        <button class="btn-login-header" id="btn-open-login">Iniciar Sesión</button>
      `;

      document.getElementById('btn-open-login').addEventListener('click', () => {
        openAuthModal('login');
      });
    }
  }

  function openAuthModal(tab = 'login') {
    const modal = document.getElementById('modal-auth');
    const formLogin = document.getElementById('form-login');
    const formRegister = document.getElementById('form-register');
    const tabLogin = document.getElementById('auth-tab-login');
    const tabRegister = document.getElementById('auth-tab-register');
    const modalTitle = document.getElementById('auth-modal-title');

    if (tab === 'login') {
      formLogin.style.display = 'block';
      formRegister.style.display = 'none';
      tabLogin.classList.add('active');
      tabRegister.classList.remove('active');
      modalTitle.innerText = 'Iniciar Sesión';
    } else {
      formLogin.style.display = 'none';
      formRegister.style.display = 'block';
      tabLogin.classList.remove('active');
      tabRegister.classList.add('active');
      modalTitle.innerText = 'Crear Cuenta';
    }

    modal.classList.add('active');
  }

  const VISTAS_QUE_REQUIEREN_LOGIN = ['view-create-match', 'view-my-matches'];

  async function irAVista(viewId) {
    if (VISTAS_QUE_REQUIEREN_LOGIN.includes(viewId) && !(await ApiService.getCurrentUser())) {
      showToast("🔐 Iniciá sesión para continuar", "danger");
      openAuthModal('login');
      return;
    }
    switchView(viewId);
  }

  function setupEventListeners() {
    const navButtons = document.querySelectorAll('[data-view]');
    navButtons.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        irAVista(btn.dataset.view);
      });
    });

    const fabBtn = document.getElementById('fab-create-btn');
    if (fabBtn) {
      fabBtn.addEventListener('click', () => irAVista('view-create-match'));
    }

    const logo = document.getElementById('brand-logo');
    if (logo) {
      logo.addEventListener('click', () => switchView('view-list'));
    }

    const btnClearZona = document.getElementById('btn-clear-zona');
    const inputFecha = document.getElementById('filter-fecha');
    const selectNivel = document.getElementById('filter-nivel');
    const toggleCercania = document.getElementById('toggle-cercania');

    setupAutocomplete('filter-zona', 'dropdown-filter-zona', (val) => {
      currentFilters.zona = val;
      if (btnClearZona) {
        btnClearZona.style.display = val.length > 0 ? 'block' : 'none';
      }
      renderMatchesList();
    });

    setupAutocomplete('create-zona', 'dropdown-create-zona');

    if (btnClearZona) {
      btnClearZona.addEventListener('click', () => {
        const inputZona = document.getElementById('filter-zona');
        if (inputZona) inputZona.value = '';
        currentFilters.zona = '';
        btnClearZona.style.display = 'none';
        renderMatchesList();
      });
    }

    if (inputFecha) {
      inputFecha.addEventListener('change', (e) => {
        currentFilters.fecha = e.target.value;
        renderMatchesList();
      });
    }

    if (selectNivel) {
      selectNivel.addEventListener('change', (e) => {
        currentFilters.nivel = e.target.value;
        renderMatchesList();
      });
    }

    const selectGenero = document.getElementById('filter-genero');
    if (selectGenero) {
      selectGenero.addEventListener('change', (e) => {
        currentFilters.genero = e.target.value;
        renderMatchesList();
      });
    }

    if (toggleCercania) {
      toggleCercania.addEventListener('change', (e) => {
        if (e.target.checked) {
          if (!("geolocation" in navigator)) {
            toggleCercania.checked = false;
            showToast("⚠️ Tu navegador no soporta geolocalización", "danger");
            return;
          }

          toggleCercania.disabled = true;
          showToast("📍 Buscando tu ubicación...", "success");

          navigator.geolocation.getCurrentPosition(
            (position) => {
              toggleCercania.disabled = false;
              currentFilters.lat = position.coords.latitude;
              currentFilters.lng = position.coords.longitude;
              currentFilters.sortByDistance = true;
              showToast("📍 Ubicación obtenida: partidos ordenados por cercanía", "success");
              renderMatchesList();
            },
            (error) => {
              console.warn("No se pudo obtener la ubicación:", error);
              toggleCercania.disabled = false;
              toggleCercania.checked = false;
              currentFilters.sortByDistance = false;
              delete currentFilters.lat;
              delete currentFilters.lng;

              let msg = "⚠️ No pudimos obtener tu ubicación.";
              if (error.code === 1) {
                msg = "⚠️ Denegaste el permiso de ubicación. Habilitalo en la configuración del navegador para ordenar por cercanía.";
              } else if (error.code === 2) {
                msg = "⚠️ No se pudo determinar tu ubicación. Revisá que el servicio de ubicación esté activado en tu dispositivo.";
              } else if (error.code === 3) {
                msg = "⚠️ Se agotó el tiempo para obtener tu ubicación. Probá de nuevo.";
              }
              showToast(msg, "danger");
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
          );
        } else {
          currentFilters.sortByDistance = false;
          delete currentFilters.lat;
          delete currentFilters.lng;
          renderMatchesList();
        }
      });
    }

    const formCreate = document.getElementById('form-create-match');
    if (formCreate) {
      formCreate.addEventListener('submit', async (e) => {
        e.preventDefault();

        const newMatchData = {
          cancha: document.getElementById('create-cancha').value,
          direccion: document.getElementById('create-direccion').value,
          zona: document.getElementById('create-zona').value,
          nivel: document.getElementById('create-nivel').value,
          genero: document.getElementById('create-genero')?.value || 'Mixto',
          fecha: document.getElementById('create-fecha').value,
          hora: document.getElementById('create-hora').value,
          cuposTotales: 4,
          cuposFaltantes: document.getElementById('create-cupos-faltantes')?.value || 1,
          precioPersona: document.getElementById('create-precio').value
        };

        try {
          const created = await ApiService.createPartido(newMatchData);
          showToast("🚀 ¡Partido creado con éxito!", "success");
          formCreate.reset();
          document.querySelectorAll('#form-create-match .datetime-wrap').forEach(wrap => {
            const input = wrap.querySelector('input');
            wrap.classList.toggle('has-value', !!(input && input.value));
          });
          switchView('view-list');
          await renderMatchesList();
        } catch (err) {
          showToast("❌ Error al crear partido", "danger");
        }
      });
    }

    const tabUnidos = document.getElementById('tab-unidos');
    const tabCreados = document.getElementById('tab-creados');

    if (tabUnidos && tabCreados) {
      tabUnidos.addEventListener('click', () => {
        activeMyMatchesTab = 'unidos';
        tabUnidos.classList.add('active');
        tabCreados.classList.remove('active');
        renderMyMatches();
      });

      tabCreados.addEventListener('click', () => {
        activeMyMatchesTab = 'creados';
        tabCreados.classList.add('active');
        tabUnidos.classList.remove('active');
        renderMyMatches();
      });
    }

    const btnCloseDetail = document.getElementById('btn-close-detail');
    const modalDetail = document.getElementById('modal-detail');
    if (btnCloseDetail) {
      btnCloseDetail.addEventListener('click', () => modalDetail.classList.remove('active'));
    }

    const btnCloseAuth = document.getElementById('btn-close-auth');
    const modalAuth = document.getElementById('modal-auth');
    if (btnCloseAuth) {
      btnCloseAuth.addEventListener('click', () => modalAuth.classList.remove('active'));
    }

    const btnOpenLegal = document.getElementById('btn-open-legal');
    const btnCloseLegal = document.getElementById('btn-close-legal');
    const modalLegal = document.getElementById('modal-legal');
    if (btnOpenLegal && modalLegal) {
      btnOpenLegal.addEventListener('click', () => modalLegal.classList.add('active'));
    }
    if (btnCloseLegal && modalLegal) {
      btnCloseLegal.addEventListener('click', () => modalLegal.classList.remove('active'));
    }

    const tabAuthLogin = document.getElementById('auth-tab-login');
    const tabAuthRegister = document.getElementById('auth-tab-register');
    if (tabAuthLogin && tabAuthRegister) {
      tabAuthLogin.addEventListener('click', () => openAuthModal('login'));
      tabAuthRegister.addEventListener('click', () => openAuthModal('register'));
    }

    const formLogin = document.getElementById('form-login');
    if (formLogin) {
      formLogin.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const pass = document.getElementById('login-password').value;

        try {
          const user = await ApiService.login(email, pass);
          modalAuth.classList.remove('active');
          updateHeaderUserStatus();
          showToast(`✅ ¡Bienvenido de nuevo, ${user.nombre}!`, "success");
          renderMatchesList();
        } catch (err) {
          showToast("❌ Error al iniciar sesión", "danger");
        }
      });
    }

    const formRegister = document.getElementById('form-register');
    if (formRegister) {
      formRegister.addEventListener('submit', async (e) => {
        e.preventDefault();

        const pass = document.getElementById('register-password').value;
        const confirmPass = document.getElementById('register-confirm-password').value;

        if (pass !== confirmPass) {
          showToast("❌ Las contraseñas no coinciden", "danger");
          return;
        }

        const userData = {
          nombre: document.getElementById('register-name').value,
          email: document.getElementById('register-email').value,
          telefono: document.getElementById('register-phone').value,
          nivel: document.getElementById('register-nivel').value,
          password: pass
        };

        try {
          const user = await ApiService.register(userData);
          modalAuth.classList.remove('active');
          updateHeaderUserStatus();
          showToast(`🎉 ¡Cuenta creada con éxito! Bienvenido ${user.nombre}`, "success");
          renderMatchesList();
        } catch (err) {
          showToast(err.message ? `❌ ${err.message}` : "❌ Error en el registro", "danger");
        }
      });
    }
  }

  function showToast(message, type = 'success') {
    const toast = document.getElementById('toast-notification');
    const toastMsg = document.getElementById('toast-message');

    if (!toast || !toastMsg) return;

    toastMsg.innerText = message;
    toast.classList.remove('toast-success', 'toast-danger', 'toast-info');
    toast.classList.add(`toast-${type}`);

    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3500);
  }

  function formatDate(dateString) {
    if (!dateString) return '';
    const parts = dateString.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateString;
  }
});
