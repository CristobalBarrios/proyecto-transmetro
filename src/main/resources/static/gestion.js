const gestionResources = [
  {
    key: "usuarios", endpoint: "/api/usuarios", container: "#usuarios-content", route: "administracion/usuarios", icon: "manage_accounts",
    title: "Usuarios registrados", singular: "usuario", newLabel: "Nuevo usuario",
    columns: [
      { label: "Nombre", value: r => r.nombre }, { label: "Usuario", value: r => r.nombreUsuario },
      { label: "Perfil", value: r => r.perfil }, { label: "Estación", value: r => referenceName("estaciones", r.estacionId) },
      { label: "Activo", value: r => r.activo ? "Sí" : "No" }
    ],
    fields: [
      { name: "nombre", label: "Nombre", required: true, maxLength: 150 },
      { name: "nombreUsuario", label: "Usuario", required: true, maxLength: 60 },
      { name: "contrasena", label: "Contraseña (obligatoria al crear; vacía para conservar)", type: "password", maxLength: 72, optional: true },
      { name: "perfil", label: "Perfil", type: "select", required: true, options: ["supervisor", "operador", "flota", "estaciones", "seguridad", "personal", "administrador"] },
      { name: "estacionId", label: "Estación (obligatoria para operador)", type: "select", reference: "estaciones", optional: true },
      { name: "activo", label: "Activo", type: "select", options: ["true", "false"], boolean: true, defaultValue: "true" }
    ]
  },
  {
    key: "recorridos", endpoint: "/api/recorridos", container: "#recorridos-content", route: "operacion/recorridos", icon: "directions_bus",
    title: "Recorridos registrados", singular: "recorrido", newLabel: "Nuevo recorrido", allowEdit: false,
    columns: [{ label: "ID", value: r => r.id }, { label: "Línea", value: r => r.linea },
      { label: "Bus", value: r => r.bus }, { label: "Piloto", value: r => r.piloto },
      { label: "Inicio", value: r => fechaLocal(r.inicio) }, { label: "Fin", value: r => fechaLocal(r.fin) }],
    fields: [
      { name: "lineaId", label: "Línea operativa", type: "select", reference: "lineas", required: true, filter: r => r.estado === "operativa" },
      { name: "busId", label: "Bus", type: "select", reference: "buses", required: true, filter: r => r.estado === "disponible" },
      { name: "pilotoId", label: "Piloto", type: "select", reference: "pilotos", required: true }
    ],
    actions: r => r.fin ? [] : [{ label: "Finalizar", run: () => request(`/api/recorridos/${r.id}/finalizar`, { method: "PUT" }) }]
  },
  {
    key: "visitas", endpoint: "/api/visitas", container: "#visitas-content", route: "operacion/visitas", icon: "location_on",
    title: "Visitas registradas", singular: "visita", newLabel: "Nueva visita", allowEdit: false,
    columns: [{ label: "Recorrido", value: r => r.recorrido_id }, { label: "Estación", value: r => r.estacion },
      { label: "Orden", value: r => r.orden_programado }, { label: "A bordo", value: r => r.pasajeros_abordo },
      { label: "Esperando", value: r => r.pasajeros_esperando }, { label: "Capacidad", value: r => r.capacidad_bus },
      { label: "Llegada", value: r => fechaLocal(r.llegada) }, { label: "Salida prevista", value: r => fechaLocal(r.salida_prevista) },
      { label: "Espera extra (min)", value: r => r.espera_extra_min }, { label: "Salida", value: r => fechaLocal(r.salida) }],
    fields: [
      { name: "recorridoId", label: "Recorrido", type: "select", reference: "recorridos", required: true, filter: r => !r.fin },
      { name: "estacionId", label: "Estación", type: "select", reference: "estaciones", required: true },
      { name: "llegada", label: "Llegada", type: "datetime-local", required: true },
      { name: "salidaPrevista", label: "Salida prevista (sin espera extra)", type: "datetime-local", required: true },
      { name: "pasajerosAbordo", label: "Pasajeros a bordo", type: "number", min: 0, required: true },
      { name: "pasajerosEsperando", label: "Pasajeros esperando", type: "number", min: 0, required: true }
    ],
    actions: r => r.salida ? [] : [{ label: "Registrar salida", run: () => abrirAccion("Registrar salida", [{ name: "salida", label: "Fecha y hora de salida", type: "datetime-local", required: true }], payload => enviarJson(`/api/visitas/${r.id}/salida`, payload, "PUT")) }]
  },
  {
    key: "alertas", endpoint: "/api/alertas", container: "#alertas-content", route: "operacion/alertas", icon: "warning",
    title: "Alertas de ocupación", singular: "alerta", newLabel: null, allowEdit: false, fields: [],
    columns: [{ label: "Estación", value: r => r.estacion }, { label: "Línea", value: r => r.linea },
      { label: "Esperando", value: r => r.pasajeros_esperando }, { label: "Capacidad", value: r => r.capacidad_bus },
      { label: "Estado", value: r => r.estado }, { label: "Bus de refuerzo", value: r => r.bus_refuerzo || "-" },
      { label: "Creada", value: r => fechaLocal(r.creada) }, { label: "Atendida", value: r => fechaLocal(r.atendida) }],
    actions: r => r.estado !== "pendiente" ? [] : [{ label: "Asignar refuerzo", run: () => abrirAccion("Atender alerta", [{ name: "busId", label: "Bus de refuerzo", type: "select", reference: "buses", required: true }], payload => enviarJson(`/api/alertas/${r.id}/atender`, payload, "PUT")) }]
  },
  {
    key: "eventos", endpoint: "/api/eventos", container: "#eventos-content", route: "operacion/eventos", icon: "assignment",
    title: "Eventos operativos", singular: "evento", newLabel: "Nuevo evento", allowEdit: false,
    columns: [{ label: "Tipo", value: r => r.tipo }, { label: "Descripción", value: r => r.descripcion },
      { label: "Estado", value: r => r.estado }, { label: "Línea", value: r => referenceName("lineas", r.linea_id) },
      { label: "Estación", value: r => referenceName("estaciones", r.estacion_id) }, { label: "Bus", value: r => referenceName("buses", r.bus_id) },
      { label: "Registrado por", value: r => r.registrado_por }, { label: "Fecha", value: r => fechaLocal(r.fecha) },
      { label: "Cerrado por", value: r => r.cerrado_por || "-" }, { label: "Cierre", value: r => fechaLocal(r.fecha_cierre) }],
    fields: [
      { name: "tipo", label: "Tipo", required: true, maxLength: 60 }, { name: "descripcion", label: "Descripción", required: true, maxLength: 500 },
      { name: "lineaId", label: "Línea afectada", type: "select", reference: "lineas", optional: true },
      { name: "estacionId", label: "Estación afectada", type: "select", reference: "estaciones", optional: true },
      { name: "busId", label: "Bus afectado", type: "select", reference: "buses", optional: true },
      { name: "accesoId", label: "Acceso afectado", type: "select", reference: "accesos", optional: true }
    ],
    actions: r => r.estado === "cerrado" ? [] : [
      ...(r.estado === "abierto" ? [{ label: "Atender", run: () => enviarJson(`/api/eventos/${r.id}/estado`, { estado: "en_atencion" }, "PUT") }] : []),
      { label: "Cerrar", run: () => enviarJson(`/api/eventos/${r.id}/estado`, { estado: "cerrado" }, "PUT") }
    ]
  }
];

function fechaLocal(valor) { return valor ? new Date(valor).toLocaleString("es-GT") : "-"; }
function enviarJson(url, datos, method = "POST") {
  return request(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(datos) });
}

function abrirAccion(titulo, campos, guardar) {
  const modal = document.querySelector("#accion-modal");
  modal.querySelector("h3").textContent = titulo;
  const cuerpo = modal.querySelector(".modal-body");
  cuerpo.replaceChildren();
  campos.forEach(campo => {
    const label = createField({ key: "accion" }, campo);
    const control = label.querySelector("input,select");
    if (campo.reference) {
      control.add(new Option("Seleccione una opción", ""));
      (recordsByKey[campo.reference] || []).forEach(r => control.add(new Option(optionLabel(campo.reference, r), r.id)));
    }
    cuerpo.append(label);
  });
  const error = document.createElement("p");
  error.className = "login-message";
  error.setAttribute("role", "alert");
  cuerpo.append(error);
  modal.querySelector("form").onsubmit = async event => {
    event.preventDefault();
    const boton = event.currentTarget.querySelector("[type=submit]");
    boton.disabled = true;
    const datos = {};
    campos.forEach(c => { datos[c.name] = valorCampo(c, event.currentTarget.elements[c.name].value); });
    try {
      await guardar(datos);
      bootstrap.Modal.getOrCreateInstance(modal).hide();
      await loadAll();
      showNotice("Cambio guardado correctamente.");
    } catch (e) { error.textContent = e.message; }
    finally { boton.disabled = false; }
  };
  bootstrap.Modal.getOrCreateInstance(modal).show();
}

function valorCampo(campo, raw) {
  if (campo.boolean) return raw === "true";
  if (campo.type === "datetime-local") return raw ? new Date(raw).toISOString() : null;
  if (campo.type === "number" || campo.type === "select" && campo.reference) return raw === "" ? null : Number(raw);
  return raw === "" && campo.optional ? null : raw;
}

const permisosEscritura = {
  educacion: ["personal"], pilotos: ["personal"], guardias: ["seguridad"], accesos: ["seguridad"],
  lineas: ["estaciones"], estaciones: ["estaciones"], municipalidades: ["estaciones"],
  parqueos: ["flota"], buses: ["flota"], usuarios: [], recorridos: ["operador"], visitas: ["operador"], alertas: ["operador"], eventos: ["todos"]
};
let sesion;
function puedeEscribir(key) {
  if (!sesion) return false;
  if (key === "usuarios") return sesion.perfil === "administrador";
  return ["administrador", "supervisor"].includes(sesion.perfil) || (permisosEscritura[key] || []).some(p => p === "todos" || p === sesion.perfil);
}
function puedeLeer(key) {
  if (key === "usuarios") return sesion.perfil === "administrador";
  if (["pilotos", "educacion"].includes(key)) return ["administrador", "supervisor", "personal", "operador", "flota"].includes(sesion.perfil);
  if (key === "guardias") return ["administrador", "supervisor", "personal", "seguridad"].includes(sesion.perfil);
  if (["recorridos", "visitas", "alertas"].includes(key)) return ["administrador", "supervisor", "operador"].includes(sesion.perfil);
  return true;
}

function puedeRuta(ruta) {
  if (ruta === "inicio" || ruta === "reportes" || ruta === "operacion/eventos" || ruta === "red/rutas") return true;
  if (ruta === "administracion/bitacora") return ["administrador", "supervisor"].includes(sesion.perfil);
  if (ruta === "red/referencias") return puedeEscribir("lineas");
  if (ruta === "operacion" || ruta.startsWith("operacion/")) return puedeLeer("recorridos");
  if (ruta === "seguridad/asignaciones") return ["administrador", "supervisor", "seguridad"].includes(sesion.perfil);
  const recurso = resources.find(r => r.route === ruta);
  return recurso ? puedeLeer(recurso.key) : false;
}

function crearPantallasGestion() {
  const contenido = document.querySelector(".view-content");
  gestionResources.forEach(r => {
    const panel = document.createElement("section");
    panel.className = "view-panel";
    panel.dataset.view = r.route;
    panel.hidden = true;
    panel.innerHTML = `<div id="${r.key}-content" class="module-grid"></div>`;
    contenido.append(panel);
  });
  const extras = [
    ["administracion/bitacora", '<div class="panel"><div class="panel-heading"><h3>Bitácora de cambios</h3></div><div id="bitacora-list" class="table-responsive"></div></div>'],
    ["reportes", `<div class="panel"><div class="panel-heading"><h3>Consulta de reportes</h3></div>
      <form id="reportes-form" class="route-toolbar"><label>Reporte<select name="tipo">${["estaciones","lineas","buses","rutas","accesos","personal","recorridos","visitas","alertas","eventos"].map(t => `<option value="${t}">${t === "lineas" ? "Líneas" : t[0].toUpperCase()+t.slice(1)}</option>`).join("")}</select></label>
      <label>Desde<input type="date" name="desde"></label><label>Hasta<input type="date" name="hasta"></label>
      <label>Línea<select name="lineaId"></select></label><label>Estación<select name="estacionId"></select></label><label>Bus<select name="busId"></select></label>
      <button class="button" type="submit">Consultar</button></form><p class="route-summary">Los filtros de fecha y recurso se aplican a recorridos, visitas, alertas y eventos.</p><div id="reportes-list" class="table-responsive"></div></div>`],
    ["red/referencias", `<div class="panel"><div class="panel-heading"><h3>Cargar datos de referencia</h3></div>
      <form id="referencias-form" class="modal-form"><label>Fuente autorizada<input name="fuente" required></label>
      <label>Archivo JSON de rutas y estaciones<input name="archivo" type="file" accept=".json,application/json" required></label>
      <label><input name="autorizada" type="checkbox" required> Confirmo que la fuente está autorizada para esta actualización.</label>
      <p>El archivo contiene una lista «rutas» con municipalidadId, codigo, nombre y estaciones. Cada estación incluye nombre, direccion, zona, codigoRuta y distanciaKm desde la anterior (cero para la primera).</p>
      <button class="button" type="submit">Cargar referencias</button></form><p id="referencias-resumen" class="route-summary" role="status"></p><div id="referencias-rechazos" class="table-responsive"></div></div>`]
  ];
  extras.forEach(([ruta, html]) => {
    const panel = document.createElement("section");
    panel.className = "view-panel";
    panel.dataset.view = ruta;
    panel.hidden = true;
    panel.innerHTML = html;
    contenido.append(panel);
  });
  const nav = document.querySelector("#app-navigation");
  const operacion = nav.querySelector('[data-route="operacion"]');
  const grupo = document.createElement("section");
  grupo.className = "nav-group";
  grupo.dataset.group = "operacion";
  grupo.innerHTML = '<button class="nav-group-toggle" type="button" aria-expanded="true"><span class="nav-group-name"><span class="material-symbols-outlined" aria-hidden="true">monitoring</span>Operación</span><span class="nav-chevron" aria-hidden="true"></span></button><div class="nav-group-items"></div>';
  nav.insertBefore(grupo, operacion);
  operacion.lastChild.textContent = "Preparación de líneas";
  grupo.querySelector(".nav-group-items").append(operacion);
  const enlaces = [
    ["operacion", "operacion/recorridos", "Recorridos", "directions_bus"],
    ["operacion", "operacion/visitas", "Visitas", "location_on"],
    ["operacion", "operacion/alertas", "Alertas", "warning"],
    ["operacion", "operacion/eventos", "Eventos", "assignment"],
    ["administracion", "administracion/usuarios", "Usuarios", "manage_accounts"],
    ["administracion", "administracion/bitacora", "Bitácora", "history"],
    ["red", "red/referencias", "Datos de referencia", "upload_file"],
    [null, "reportes", "Reportes", "summarize"]
  ];
  enlaces.forEach(([grupo, ruta, titulo, icono]) => {
    const enlace = document.createElement("a");
    enlace.className = "side-link";
    enlace.href = `#/${ruta}`;
    enlace.dataset.route = ruta;
    enlace.innerHTML = `<span class="material-symbols-outlined" aria-hidden="true">${icono}</span>${titulo}`;
    (grupo ? nav.querySelector(`[data-group="${grupo}"] .nav-group-items`) : nav).append(enlace);
  });
  const modal = document.createElement("div");
  modal.id = "accion-modal";
  modal.className = "modal fade";
  modal.tabIndex = -1;
  modal.innerHTML = '<div class="modal-dialog modal-dialog-centered"><div class="modal-content transmetro-modal"><form><div class="modal-header"><h3>Acción</h3><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button></div><div class="modal-body"></div><div class="modal-footer"><button class="button button-secondary" type="button" data-bs-dismiss="modal">Cancelar</button><button class="button" type="submit">Guardar</button></div></form></div></div>';
  document.body.append(modal);
}

function actualizarFiltrosReportes() {
  const form = document.querySelector("#reportes-form");
  [["lineaId","lineas"],["estacionId","estaciones"],["busId","buses"]].forEach(([campo,key]) => {
    const select = form.elements[campo];
    const valor = select.value;
    select.replaceChildren(new Option("Todos", ""));
    (recordsByKey[key] || []).forEach(r => select.add(new Option(optionLabel(key,r),r.id)));
    select.value = valor;
  });
  const operativos = ["recorridos","visitas","alertas","eventos"].includes(form.elements.tipo.value);
  ["desde","hasta","lineaId","estacionId","busId"].forEach(c => { form.elements[c].disabled = !operativos; });
}

function tablaConsulta(destino, datos) {
  const caja = document.querySelector(destino);
  caja.replaceChildren();
  const resumen = document.createElement("p");
  resumen.textContent = `Total de registros: ${datos.length}`;
  caja.append(resumen);
  if (!datos.length) return;
  const tabla = document.createElement("table");
  tabla.className = "records-table";
  const head = tabla.createTHead().insertRow();
  const columnas = Object.keys(datos[0]);
  columnas.forEach(c => { const th = document.createElement("th"); th.textContent = c.replaceAll("_", " "); head.append(th); });
  const body = tabla.createTBody();
  datos.forEach(r => {
    const fila = body.insertRow();
    columnas.forEach(c => {
      let valor = r[c] ?? "-";
      if (c.includes("distancia") && typeof valor === "number") valor = valor.toFixed(3);
      if (["fecha", "fecha_cierre", "inicio", "fin", "llegada", "salida", "salida_prevista", "creada", "atendida"].includes(c)) valor = fechaLocal(r[c]);
      operationCell(fila, valor);
    });
  });
  caja.append(tabla);
}

function configurarConsultas() {
  document.querySelector('#reportes-form [name=tipo]').onchange = actualizarFiltrosReportes;
  document.querySelector("#reportes-form").onsubmit = async event => {
    event.preventDefault();
    const query = new URLSearchParams();
    new FormData(event.currentTarget).forEach((v, k) => { if (v) query.set(k, v); });
    try { tablaConsulta("#reportes-list", await request(`/api/reportes?${query}`)); }
    catch (e) { showNotice(e.message, true); }
  };
  document.querySelector("#referencias-form").onsubmit = async event => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
      const texto = await form.elements.archivo.files[0].text();
      const resultado = await enviarJson("/api/referencias", { fuente: form.elements.fuente.value,
        autorizada: form.elements.autorizada.checked, rutas: JSON.parse(texto).rutas });
      document.querySelector("#referencias-resumen").textContent = `Rutas aceptadas: ${resultado.aceptados.length}. Rechazadas: ${resultado.rechazados.length}.`;
      tablaConsulta("#referencias-rechazos", resultado.rechazados);
      await loadAll();
    } catch (e) { showNotice(`No se pudo cargar la referencia: ${e.message}`, true); }
  };
}
