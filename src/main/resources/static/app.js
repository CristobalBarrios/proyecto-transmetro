const notice = document.querySelector("#notice");
let csrfToken;
const recordsByKey = {};
const routes = {
  inicio: { title: "Inicio", breadcrumb: "Inicio" },
  "red/lineas": { title: "Líneas", breadcrumb: "Red / Líneas" },
  "red/estaciones": { title: "Estaciones", breadcrumb: "Red / Estaciones" },
  "red/rutas": { title: "Rutas de líneas", breadcrumb: "Red / Rutas de líneas" },
  "flota/parqueos": { title: "Parqueos", breadcrumb: "Flota / Parqueos" },
  "flota/buses": { title: "Buses", breadcrumb: "Flota / Buses" },
  "personal/pilotos": { title: "Pilotos", breadcrumb: "Personal / Pilotos" },
  "personal/educacion": { title: "Historial educativo", breadcrumb: "Personal / Historial educativo" },
  "seguridad/accesos": { title: "Accesos", breadcrumb: "Seguridad / Accesos" },
  "seguridad/guardias": { title: "Guardias", breadcrumb: "Seguridad / Guardias" },
  "seguridad/asignaciones": { title: "Asignaciones de guardias", breadcrumb: "Seguridad / Asignaciones" },
  "administracion/municipalidades": { title: "Municipalidades", breadcrumb: "Administración / Municipalidades" },
  operacion: { title: "Preparación de líneas", breadcrumb: "Operación / Preparación" },
  "operacion/recorridos": { title: "Recorridos", breadcrumb: "Operación / Recorridos" },
  "operacion/visitas": { title: "Visitas de estaciones", breadcrumb: "Operación / Visitas" },
  "operacion/alertas": { title: "Alertas", breadcrumb: "Operación / Alertas" },
  "operacion/eventos": { title: "Eventos operativos", breadcrumb: "Operación / Eventos" },
  "administracion/usuarios": { title: "Usuarios", breadcrumb: "Administración / Usuarios" },
  "administracion/bitacora": { title: "Bitácora", breadcrumb: "Administración / Bitácora" },
  "red/referencias": { title: "Datos de referencia", breadcrumb: "Red / Datos de referencia" },
  reportes: { title: "Reportes", breadcrumb: "Reportes" }
};

const resources = [
  {
    key: "educacion", endpoint: "/api/educacion-pilotos", container: "#educacion-content", route: "personal/educacion", icon: "school",
    title: "Formación registrada", singular: "formación", newLabel: "Nueva formación",
    columns: [
      { label: "Piloto", value: record => referenceName("pilotos", record.pilotoId) },
      { label: "Nivel", value: record => record.nivel },
      { label: "Institución", value: record => record.institucion },
      { label: "Título", value: record => record.titulo || "-" },
      { label: "Año de finalización", value: record => record.anioFinalizacion ?? "-" }
    ],
    fields: [
      { name: "pilotoId", label: "Piloto", type: "select", reference: "pilotos", required: true },
      { name: "nivel", label: "Nivel educativo", required: true, maxLength: 60 },
      { name: "institucion", label: "Institución", required: true, maxLength: 150 },
      { name: "titulo", label: "Título obtenido", maxLength: 150, optional: true },
      { name: "anioFinalizacion", label: "Año de finalización", type: "number", min: 1900, max: 2100, optional: true }
    ]
  },
  {
    key: "municipalidades", endpoint: "/api/municipalidades", container: "#municipalidades-content", route: "administracion/municipalidades", icon: "account_balance",
    title: "Municipalidades registradas", singular: "municipalidad", newLabel: "Nueva municipalidad",
    columns: [{ label: "Nombre", value: record => record.nombre }],
    fields: [{ name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Municipalidad de Guatemala" }]
  },
  {
    key: "lineas", endpoint: "/api/lineas", container: "#lineas-content", route: "red/lineas", icon: "alt_route",
    title: "Líneas registradas", singular: "línea", newLabel: "Nueva línea",
    columns: [
      { label: "Código", value: record => record.codigo },
      { label: "Nombre", value: record => record.nombre },
      { label: "Municipalidad", value: record => referenceName("municipalidades", record.municipalidadId) },
      { label: "Estado", value: record => record.estado }
    ],
    fields: [
      { name: "municipalidadId", label: "Municipalidad", type: "select", reference: "municipalidades", required: true },
      { name: "codigo", label: "Código", required: true, maxLength: 20, placeholder: "LÍNEA-02" },
      { name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Nombre de la línea" }
    ]
  },
  {
    key: "estaciones", endpoint: "/api/estaciones", container: "#estaciones-content", route: "red/estaciones", icon: "location_on",
    title: "Estaciones registradas", singular: "estación", newLabel: "Nueva estación",
    columns: [
      { label: "Nombre", value: record => record.nombre },
      { label: "Dirección", value: record => record.direccion },
      { label: "Zona", value: record => record.zona },
      { label: "Código de ruta", value: record => record.codigoRuta },
      { label: "Municipalidad", value: record => referenceName("municipalidades", record.municipalidadId) }
    ],
    fields: [
      { name: "municipalidadId", label: "Municipalidad", type: "select", reference: "municipalidades", required: true },
      { name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Estación Central" },
      { name: "direccion", label: "Dirección", required: true, maxLength: 250, placeholder: "Dirección de la estación" },
      { name: "zona", label: "Zona", type: "number", required: true, min: 1, placeholder: "1" },
      { name: "codigoRuta", label: "Código de ruta", required: true, maxLength: 30, placeholder: "LÍNEA-02-A" }
    ]
  },
  {
    key: "parqueos", endpoint: "/api/parqueos", container: "#parqueos-content", route: "flota/parqueos", icon: "local_parking",
    title: "Parqueos registrados", singular: "parqueo", newLabel: "Nuevo parqueo",
    columns: [
      { label: "Nombre", value: record => record.nombre },
      { label: "Estación", value: record => referenceName("estaciones", record.estacionId, "Sin estación") }
    ],
    fields: [
      { name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Parqueo central" },
      { name: "estacionId", label: "Estación", type: "select", reference: "estaciones", optional: true }
    ]
  },
  {
    key: "buses", endpoint: "/api/buses", container: "#buses-content", route: "flota/buses", icon: "directions_bus",
    title: "Buses registrados", singular: "bus", newLabel: "Nuevo bus",
    columns: [
      { label: "Placa", value: record => record.placa },
      { label: "Capacidad", value: record => record.capacidadMaxima },
      { label: "Línea", value: record => referenceName("lineas", record.lineaId, "Sin línea") },
      { label: "Parqueo", value: record => referenceName("parqueos", record.parqueoId) },
      { label: "Estado", value: record => record.estado }
    ],
    fields: [
      { name: "placa", label: "Placa", required: true, maxLength: 20, placeholder: "C-123ABC" },
      { name: "capacidadMaxima", label: "Capacidad máxima", type: "number", required: true, min: 1, placeholder: "80" },
      { name: "lineaId", label: "Línea", type: "select", reference: "lineas", optional: true },
      { name: "parqueoId", label: "Parqueo", type: "select", reference: "parqueos", required: true },
      { name: "estado", label: "Estado", type: "select", required: true, options: ["disponible", "mantenimiento", "fuera_servicio"], defaultValue: "disponible" }
    ]
  },
  {
    key: "pilotos", endpoint: "/api/pilotos", container: "#pilotos-content", route: "personal/pilotos", icon: "person",
    title: "Pilotos registrados", singular: "piloto", newLabel: "Nuevo piloto",
    columns: [
      { label: "Nombre", value: record => record.nombre },
      { label: "Residencia", value: record => record.residencia },
      { label: "Teléfono", value: record => record.telefono },
      { label: "Correo", value: record => record.correo || "-" }
    ],
    fields: [
      { name: "nombre", label: "Nombre", required: true, maxLength: 150, placeholder: "Nombre completo" },
      { name: "residencia", label: "Residencia", required: true, maxLength: 250, placeholder: "Dirección de residencia" },
      { name: "telefono", label: "Teléfono", required: true, maxLength: 30, placeholder: "0000-0000" },
      { name: "correo", label: "Correo", type: "email", maxLength: 150, placeholder: "correo@ejemplo.com", optional: true }
    ]
  },
  {
    key: "accesos", endpoint: "/api/accesos", container: "#accesos-content", route: "seguridad/accesos", icon: "meeting_room",
    title: "Accesos registrados", singular: "acceso", newLabel: "Nuevo acceso",
    columns: [
      { label: "Código", value: record => record.codigo },
      { label: "Estación", value: record => referenceName("estaciones", record.estacionId) },
      { label: "Descripción", value: record => record.descripcion || "-" }
    ],
    fields: [
      { name: "estacionId", label: "Estación", type: "select", reference: "estaciones", required: true },
      { name: "codigo", label: "Código", required: true, maxLength: 30, placeholder: "ACC-01" },
      { name: "descripcion", label: "Descripción", maxLength: 200, placeholder: "Acceso norte", optional: true }
    ]
  },
  {
    key: "guardias", endpoint: "/api/guardias", container: "#guardias-content", route: "seguridad/guardias", icon: "shield_person",
    title: "Guardias registrados", singular: "guardia", newLabel: "Nuevo guardia",
    columns: [
      { label: "Nombre", value: record => record.nombre },
      { label: "Teléfono", value: record => record.telefono || "-" }
    ],
    fields: [
      { name: "nombre", label: "Nombre", required: true, maxLength: 150, placeholder: "Nombre completo" },
      { name: "telefono", label: "Teléfono", maxLength: 30, placeholder: "0000-0000", optional: true }
    ]
  }
];

function showNotice(message, isError = false) {
  notice.textContent = message;
  notice.style.background = isError ? "#a2192a" : "#172b78";
  notice.classList.add("show");
  window.setTimeout(() => notice.classList.remove("show"), 3500);
}

async function getCsrfToken() {
  if (csrfToken) return csrfToken;
  const response = await fetch("/api/csrf", { credentials: "same-origin" });
  if (!response.ok) throw new Error("No se pudo validar la sesión.");
  csrfToken = await response.json();
  return csrfToken;
}

async function request(path, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = new Headers(options.headers);
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    const token = await getCsrfToken();
    headers.set(token.encabezado, token.valor);
  }
  const response = await fetch(path, { ...options, headers, credentials: "same-origin" });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || "No fue posible completar la operación.");
  }
  if (response.status === 204) return null;
  return response.json();
}

function createInterfaces() {
  resources.forEach(resource => {
    createPanel(resource);
    createModal(resource);
  });
}

function createPanel(resource) {
  const panel = document.createElement("section");
  panel.className = "panel module-panel";
  const heading = document.createElement("div");
  heading.className = "panel-heading panel-heading-actions";
  const titleContainer = document.createElement("div");
  titleContainer.innerHTML = `<p class="eyebrow">Entidad</p><h3><span class="material-symbols-outlined" aria-hidden="true">${resource.icon}</span>${resource.title}</h3>`;
  const button = document.createElement("button");
  button.className = "button button-add";
  button.type = "button";
  button.textContent = resource.newLabel;
  button.addEventListener("click", () => openForm(resource));
  heading.append(titleContainer);
  if (resource.newLabel && puedeEscribir(resource.key)) heading.append(button);

  const responsive = document.createElement("div");
  responsive.className = "table-responsive";
  const table = document.createElement("table");
  table.className = "records-table";
  const head = document.createElement("thead");
  const headerRow = document.createElement("tr");
  [...resource.columns.map(column => column.label), "Acciones"].forEach(label => {
    const cell = document.createElement("th");
    cell.scope = "col";
    cell.textContent = label;
    headerRow.append(cell);
  });
  head.append(headerRow);
  const body = document.createElement("tbody");
  body.id = `${resource.key}-list`;
  table.append(head, body);
  responsive.append(table);
  panel.append(heading, responsive);
  document.querySelector(resource.container).append(panel);
}

function createModal(resource) {
  const modal = document.createElement("div");
  modal.className = "modal fade";
  modal.id = `${resource.key}-modal`;
  modal.tabIndex = -1;
  modal.setAttribute("aria-hidden", "true");
  modal.innerHTML = `<div class="modal-dialog modal-dialog-centered modal-dialog-scrollable"><div class="modal-content transmetro-modal"><form id="${resource.key}-form" class="modal-form"><div class="modal-header"><div><p class="eyebrow">Acción</p><h3 id="${resource.key}-form-title">${resource.newLabel}</h3></div><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button></div><div class="modal-body"></div><div class="modal-footer form-actions"><button type="button" class="button button-secondary" data-bs-dismiss="modal">Cancelar</button><button class="button" type="submit">Guardar ${resource.singular}</button></div></form></div></div>`;
  const body = modal.querySelector(".modal-body");
  resource.fields.forEach(field => body.append(createField(resource, field)));
  const form = modal.querySelector("form");
  form.addEventListener("submit", event => saveResource(event, resource));
  modal.addEventListener("hidden.bs.modal", () => resetForm(resource));
  document.body.append(modal);
}

function createField(resource, field) {
  const label = document.createElement("label");
  label.textContent = field.label;
  const control = document.createElement(field.type === "select" ? "select" : "input");
  control.name = field.name;
  control.required = Boolean(field.required);
  if (field.type && field.type !== "select") control.type = field.type;
  if (field.maxLength) control.maxLength = field.maxLength;
  if (field.min !== undefined) control.min = field.min;
  if (field.max) control.max = field.max;
  if (field.placeholder) control.placeholder = field.placeholder;
  control.dataset.resource = resource.key;
  label.append(control);
  return label;
}

async function loadAll() {
  try {
    const responses = await Promise.all(resources.filter(resource => puedeLeer(resource.key)).map(async resource => [resource.key, await request(resource.endpoint)]));
    responses.forEach(([key, records]) => { recordsByKey[key] = records; });
    populateSelects();
    actualizarFiltrosReportes();
    resources.filter(resource => puedeLeer(resource.key)).forEach(renderResource);
    renderDashboard();
    await refreshOperationPanels();
    await refreshRouteSelector();
    if (["administrador", "supervisor"].includes(sesion.perfil)) tablaConsulta("#bitacora-list", await request("/api/bitacora"));
  } catch (error) {
    showNotice(`No se pudieron cargar los registros: ${error.message}`, true);
  }
}

function renderDashboard() {
  const container = document.querySelector("#summary-cards");
  container.replaceChildren();
  resources.filter(resource => puedeLeer(resource.key)).forEach(resource => {
    const card = document.createElement("a");
    card.className = "summary-card";
    card.href = `#/${resource.route}`;
    const icon = document.createElement("span");
    icon.className = "material-symbols-outlined";
    icon.setAttribute("aria-hidden", "true");
    icon.textContent = resource.icon;
    const count = document.createElement("strong");
    count.textContent = String((recordsByKey[resource.key] || []).length);
    const label = document.createElement("span");
    label.textContent = routes[resource.route].title;
    card.append(icon, count, label);
    container.append(card);
  });
}

function populateSelects() {
  resources.forEach(resource => resource.fields.filter(field => field.type === "select").forEach(field => {
    document.querySelectorAll(`#${resource.key}-form [name='${field.name}']`).forEach(control => {
      const current = control.value;
      control.replaceChildren();
      if (field.optional || field.reference) {
        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = field.optional ? "Sin asignar" : "Seleccione una opción";
        placeholder.disabled = !field.optional;
        control.append(placeholder);
      }
      const options = field.reference
        ? (recordsByKey[field.reference] || []).filter(record => !field.filter || field.filter(record)).filter(record => field.reference !== "estaciones" || !["visitas","eventos"].includes(resource.key) || sesion.perfil !== "operador" || record.id === sesion.estacionId).map(record => ({ value: record.id, label: optionLabel(field.reference, record) }))
        : field.options.map(value => ({ value, label: value.replaceAll("_", " ") }));
      options.forEach(option => {
        const element = document.createElement("option");
        element.value = option.value;
        element.textContent = option.label;
        control.append(element);
      });
      control.value = current || field.defaultValue || "";
    });
  }));
}

function optionLabel(key, record) {
  if (key === "lineas") return `${record.codigo} - ${record.nombre}`;
  if (key === "estaciones") return `${record.nombre} (${record.codigoRuta})`;
  if (key === "buses") return record.placa;
  if (key === "recorridos") return `${record.id} · ${record.linea} · ${record.bus}`;
  return record.nombre;
}

function referenceName(key, id, emptyValue = "-") {
  if (!id) return emptyValue;
  const record = (recordsByKey[key] || []).find(item => item.id === id);
  return record ? optionLabel(key, record) : `ID ${id}`;
}

function renderResource(resource) {
  const body = document.querySelector(`#${resource.key}-list`);
  body.replaceChildren();
  const records = recordsByKey[resource.key] || [];
  if (!records.length) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.className = "empty";
    cell.colSpan = resource.columns.length + 1;
    cell.textContent = `No hay ${resource.title.toLowerCase()}.`;
    row.append(cell);
    body.append(row);
    return;
  }
  records.forEach(record => {
    const row = document.createElement("tr");
    resource.columns.forEach(column => {
      const cell = document.createElement("td");
      cell.textContent = column.value(record);
      row.append(cell);
    });
    const actions = document.createElement("td");
    actions.className = "table-actions";
    const edit = document.createElement("button");
    edit.className = "row-action";
    edit.type = "button";
    edit.textContent = "Editar";
    edit.addEventListener("click", () => openForm(resource, record));
    if (resource.allowEdit !== false && puedeEscribir(resource.key)) actions.append(edit);
    if (puedeEscribir(resource.key)) (resource.actions?.(record) || []).forEach(action => {
      const button = document.createElement("button");
      button.className = "row-action";
      button.type = "button";
      button.textContent = action.label;
      button.onclick = async () => {
        try { await action.run(); await loadAll(); }
        catch (e) { showNotice(e.message, true); }
      };
      actions.append(button);
    });
    row.append(actions);
    body.append(row);
  });
}

function openForm(resource, record = null) {
  const form = document.querySelector(`#${resource.key}-form`);
  resetForm(resource);
  if (record) {
    form.dataset.recordId = record.id;
    resource.fields.forEach(field => { form.elements[field.name].value = record[field.name] ?? ""; });
    document.querySelector(`#${resource.key}-form-title`).textContent = `Modificar ${resource.singular}`;
    form.querySelector("button[type='submit']").textContent = "Guardar cambios";
  }
  bootstrap.Modal.getOrCreateInstance(document.querySelector(`#${resource.key}-modal`)).show();
}

function resetForm(resource) {
  const form = document.querySelector(`#${resource.key}-form`);
  form.reset();
  delete form.dataset.recordId;
  resource.fields.forEach(field => {
    if (field.defaultValue) form.elements[field.name].value = field.defaultValue;
  });
  document.querySelector(`#${resource.key}-form-title`).textContent = resource.newLabel;
  form.querySelector("button[type='submit']").textContent = `Guardar ${resource.singular}`;
}

async function saveResource(event, resource) {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = {};
  resource.fields.forEach(field => {
    const raw = form.elements[field.name].value.trim();
    payload[field.name] = valorCampo(field, raw);
  });
  const id = form.dataset.recordId;
  try {
    await request(id ? `${resource.endpoint}/${id}` : resource.endpoint, {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (resource.key === "usuarios" && Number(id) === sesion.id) { window.location.assign("/login"); return; }
    bootstrap.Modal.getOrCreateInstance(document.querySelector(`#${resource.key}-modal`)).hide();
    showNotice(id ? "Cambios guardados correctamente." : "Registro guardado correctamente.");
    await loadAll();
  } catch (error) {
    showNotice(error.message, true);
  }
}

function currentRoute() {
  const value = window.location.hash.replace(/^#\//, "");
  return routes[value] ? value : "inicio";
}

function closeNavigation() {
  document.body.classList.remove("sidebar-open");
  const toggle = document.querySelector("#menu-toggle");
  toggle.setAttribute("aria-expanded", "false");
}

function showRoute() {
  const route = currentRoute();
  if (!puedeRuta(route)) { window.location.hash = "#/inicio"; return; }
  document.querySelectorAll("[data-view]").forEach(panel => {
    panel.hidden = panel.dataset.view !== route;
  });
  document.querySelectorAll("[data-route]").forEach(link => {
    const active = link.dataset.route === route;
    link.classList.toggle("active", active);
    if (active) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  });
  const metadata = routes[route];
  document.querySelector("#view-title").textContent = metadata.title;
  document.querySelector("#view-breadcrumb").textContent = metadata.breadcrumb;
  document.title = `${metadata.title} | Control operativo Transmetro`;
  const activeLink = document.querySelector(`[data-route="${route}"]`);
  const group = activeLink?.closest(".nav-group");
  if (group) {
    group.classList.remove("is-collapsed");
    group.querySelector(".nav-group-toggle").setAttribute("aria-expanded", "true");
  }
  closeNavigation();
}

function configureNavigation() {
  const toggle = document.querySelector("#menu-toggle");
  toggle.addEventListener("click", () => {
    const isOpen = document.body.classList.toggle("sidebar-open");
    toggle.setAttribute("aria-expanded", String(isOpen));
  });
  document.querySelector("#sidebar-backdrop").addEventListener("click", closeNavigation);
  document.querySelectorAll(".nav-group-toggle").forEach(button => {
    button.addEventListener("click", () => {
      const group = button.closest(".nav-group");
      const collapsed = group.classList.toggle("is-collapsed");
      button.setAttribute("aria-expanded", String(!collapsed));
    });
  });
  window.addEventListener("hashchange", showRoute);
  const requestedRoute = window.location.hash.replace(/^#\//, "");
  if (!routes[requestedRoute]) {
    window.location.hash = "#/inicio";
  } else {
    showRoute();
  }
}

async function iniciarAplicacion() {
  try {
    sesion = await request("/api/sesion");
    resources.push(...gestionResources);
    crearPantallasGestion();
    createInterfaces();
    configureRoutes();
    configureAssignments();
    configurarConsultas();
    document.querySelectorAll("[data-route]").forEach(link => { link.hidden = !puedeRuta(link.dataset.route); });
    document.querySelector("#ruta-editar").hidden = !puedeEscribir("lineas");
    document.querySelector("#asignacion-nueva").hidden = !["administrador", "supervisor", "seguridad"].includes(sesion.perfil);
    document.querySelector(".session-status").textContent = `${sesion.nombreUsuario} · ${sesion.perfil}`;
    document.querySelector("#cerrar-sesion").onclick = async () => {
      const token = await getCsrfToken();
      await fetch("/logout", { method: "POST", credentials: "same-origin", headers: { [token.encabezado]: token.valor } });
      window.location.assign("/login");
    };
    configureNavigation();
    await loadAll();
  } catch (e) { showNotice(e.message, true); }
}
iniciarAplicacion();
