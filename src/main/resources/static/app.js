const notice = document.querySelector("#notice");
let csrfToken;
const recordsByKey = {};

const resources = [
  {
    key: "municipalidades", endpoint: "/api/municipalidades", container: "#administracion-content",
    title: "Municipalidades registradas", singular: "municipalidad", newLabel: "Nueva municipalidad",
    columns: [{ label: "Nombre", value: record => record.nombre }],
    fields: [{ name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Municipalidad de Guatemala" }]
  },
  {
    key: "lineas", endpoint: "/api/lineas", container: "#lineas-content",
    title: "Líneas registradas", singular: "línea", newLabel: "Nueva línea",
    columns: [
      { label: "Código", value: record => record.codigo },
      { label: "Nombre", value: record => record.nombre },
      { label: "Municipalidad", value: record => referenceName("municipalidades", record.municipalidadId) }
    ],
    fields: [
      { name: "municipalidadId", label: "Municipalidad", type: "select", reference: "municipalidades", required: true },
      { name: "codigo", label: "Código", required: true, maxLength: 20, placeholder: "LÍNEA-02" },
      { name: "nombre", label: "Nombre", required: true, maxLength: 120, placeholder: "Nombre de la línea" }
    ]
  },
  {
    key: "estaciones", endpoint: "/api/estaciones", container: "#estaciones-content",
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
    key: "parqueos", endpoint: "/api/parqueos", container: "#flota-content",
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
    key: "buses", endpoint: "/api/buses", container: "#flota-content",
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
    key: "pilotos", endpoint: "/api/pilotos", container: "#personal-content",
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
    key: "accesos", endpoint: "/api/accesos", container: "#seguridad-content",
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
    key: "guardias", endpoint: "/api/guardias", container: "#seguridad-content",
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
  titleContainer.innerHTML = `<p class="eyebrow">Entidad</p><h3>${resource.title}</h3>`;
  const button = document.createElement("button");
  button.className = "button button-add";
  button.type = "button";
  button.textContent = resource.newLabel;
  button.addEventListener("click", () => openForm(resource));
  heading.append(titleContainer, button);

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
  modal.innerHTML = `<div class="modal-dialog modal-dialog-centered"><div class="modal-content transmetro-modal"><form id="${resource.key}-form" class="modal-form"><div class="modal-header"><div><p class="eyebrow">Acción</p><h3 id="${resource.key}-form-title">${resource.newLabel}</h3></div><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button></div><div class="modal-body"></div><div class="modal-footer form-actions"><button type="button" class="button button-secondary" data-bs-dismiss="modal">Cancelar</button><button class="button" type="submit">Guardar ${resource.singular}</button></div></form></div></div>`;
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
  if (field.min) control.min = field.min;
  if (field.placeholder) control.placeholder = field.placeholder;
  control.dataset.resource = resource.key;
  label.append(control);
  return label;
}

async function loadAll() {
  try {
    const responses = await Promise.all(resources.map(async resource => [resource.key, await request(resource.endpoint)]));
    responses.forEach(([key, records]) => { recordsByKey[key] = records; });
    populateSelects();
    resources.forEach(renderResource);
  } catch (error) {
    showNotice(`No se pudieron cargar los registros: ${error.message}`, true);
  }
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
        ? (recordsByKey[field.reference] || []).map(record => ({ value: record.id, label: optionLabel(field.reference, record) }))
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
    actions.append(edit);
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
    payload[field.name] = field.type === "number" || field.type === "select" && field.reference
      ? (raw === "" ? null : Number(raw))
      : (raw === "" && field.optional ? null : raw);
  });
  const id = form.dataset.recordId;
  try {
    await request(id ? `${resource.endpoint}/${id}` : resource.endpoint, {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    bootstrap.Modal.getOrCreateInstance(document.querySelector(`#${resource.key}-modal`)).hide();
    showNotice(id ? "Cambios guardados correctamente." : "Registro guardado correctamente.");
    await loadAll();
  } catch (error) {
    showNotice(error.message, true);
  }
}

createInterfaces();
loadAll();
