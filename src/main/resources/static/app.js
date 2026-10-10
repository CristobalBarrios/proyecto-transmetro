const notice = document.querySelector("#notice");
let csrfToken;
let editingLineId = null;
let editingStationId = null;
const tabs = Array.from(document.querySelectorAll("#module-tabs [data-bs-toggle='tab']"));

function showNotice(message, isError = false) {
  notice.textContent = message;
  notice.style.background = isError ? "#a2192a" : "#172b78";
  notice.classList.add("show");
  window.setTimeout(() => notice.classList.remove("show"), 3500);
}

async function getCsrfToken() {
  if (csrfToken) return csrfToken;

  const response = await fetch("/api/csrf", { credentials: "same-origin" });
  if (!response.ok) throw new Error("No se pudo validar la sesion.");
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
  return response.json();
}

async function loadLines() {
  try {
    const lines = await request("/api/lineas");
    renderTable("#lineas-list", lines, line => [line.codigo, line.nombre], "No hay líneas registradas.", editLine);
  } catch (error) {
    showNotice(`No se pudo cargar las líneas: ${error.message}`, true);
  }
}

async function loadStations() {
  try {
    const stations = await request("/api/estaciones");
    renderTable(
      "#estaciones-list",
      stations,
      station => [station.nombre, station.direccion, station.zona, station.codigoRuta],
      "No hay estaciones registradas.",
      editStation
    );
  } catch (error) {
    showNotice(`No se pudo cargar las estaciones: ${error.message}`, true);
  }
}

function renderTable(selector, records, values, emptyMessage, edit) {
  const body = document.querySelector(selector);
  body.replaceChildren();

  if (!records.length) {
    const row = document.createElement("tr");
    const empty = document.createElement("td");
    empty.className = "empty";
    empty.colSpan = body.closest("table").querySelectorAll("thead th").length;
    empty.textContent = emptyMessage;
    row.append(empty);
    body.append(row);
    return;
  }

  records.forEach(record => {
    const row = document.createElement("tr");
    const editButton = document.createElement("button");
    editButton.className = "row-action";
    editButton.type = "button";
    editButton.textContent = "Editar";
    editButton.addEventListener("click", () => edit(record));
    values(record).forEach(value => {
      const cell = document.createElement("td");
      cell.textContent = value;
      row.append(cell);
    });
    const actions = document.createElement("td");
    actions.className = "table-actions";
    actions.append(editButton);
    row.append(actions);
    body.append(row);
  });
}

document.querySelector("#linea-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  try {
    const editing = editingLineId !== null;
    await request(editing ? `/api/lineas/${editingLineId}` : "/api/lineas", {
      method: editing ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });
    resetLineForm();
    showNotice(editing ? "Línea modificada correctamente." : "Línea guardada correctamente.");
    await loadLines();
  } catch (error) {
    showNotice(error.message, true);
  }
});

document.querySelector("#estacion-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  data.zona = Number(data.zona);
  try {
    const editing = editingStationId !== null;
    await request(editing ? `/api/estaciones/${editingStationId}` : "/api/estaciones", {
      method: editing ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });
    resetStationForm();
    showNotice(editing ? "Estación modificada correctamente." : "Estación guardada correctamente.");
    await loadStations();
  } catch (error) {
    showNotice(error.message, true);
  }
});

function editLine(line) {
  const form = document.querySelector("#linea-form");
  editingLineId = line.id;
  form.elements.codigo.value = line.codigo;
  form.elements.nombre.value = line.nombre;
  document.querySelector("#linea-form-title").textContent = "Modificar línea";
  document.querySelector("#linea-submit").textContent = "Guardar cambios";
  document.querySelector("#linea-cancel").hidden = false;
  form.elements.codigo.focus();
}

function resetLineForm() {
  editingLineId = null;
  document.querySelector("#linea-form").reset();
  document.querySelector("#linea-form-title").textContent = "Agregar línea";
  document.querySelector("#linea-submit").textContent = "Guardar línea";
  document.querySelector("#linea-cancel").hidden = true;
}

function editStation(station) {
  const form = document.querySelector("#estacion-form");
  editingStationId = station.id;
  form.elements.nombre.value = station.nombre;
  form.elements.direccion.value = station.direccion;
  form.elements.zona.value = station.zona;
  form.elements.codigoRuta.value = station.codigoRuta;
  document.querySelector("#estacion-form-title").textContent = "Modificar estación";
  document.querySelector("#estacion-submit").textContent = "Guardar cambios";
  document.querySelector("#estacion-cancel").hidden = false;
  form.elements.nombre.focus();
}

function resetStationForm() {
  editingStationId = null;
  document.querySelector("#estacion-form").reset();
  document.querySelector("#estacion-form-title").textContent = "Agregar estación";
  document.querySelector("#estacion-submit").textContent = "Guardar estación";
  document.querySelector("#estacion-cancel").hidden = true;
}

document.querySelector("#linea-cancel").addEventListener("click", resetLineForm);
document.querySelector("#estacion-cancel").addEventListener("click", resetStationForm);

tabs.forEach(tab => {
  tab.addEventListener("show.bs.tab", () => {
    if (tab.dataset.title) {
      document.querySelector("#planned-title").textContent = tab.dataset.title;
      document.querySelector("#planned-description").textContent = tab.dataset.description;
      document.querySelector("#panel-pendiente").setAttribute("aria-labelledby", tab.id);
    }
  });
});

loadLines();
loadStations();
