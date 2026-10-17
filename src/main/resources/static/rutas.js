let savedRoute = [];
let routeDraft = [];
let routeRequest = 0;

function configureRoutes() {
  document.querySelector("#ruta-linea").addEventListener("change", loadRoute);
  document.querySelector("#ruta-editar").addEventListener("click", () => {
    routeDraft = savedRoute.map(item => ({ ...item }));
    document.querySelector("#ruta-error").textContent = "";
    renderRouteEditor();
    bootstrap.Modal.getOrCreateInstance(document.querySelector("#ruta-modal")).show();
  });
  document.querySelector("#ruta-agregar").addEventListener("click", () => {
    captureRouteDraft();
    routeDraft.push({ estacionId: "", distanciaKm: routeDraft.length ? "" : 0 });
    renderRouteEditor();
  });
  document.querySelector("#ruta-form").addEventListener("submit", saveRoute);
}

async function refreshRouteSelector() {
  const select = document.querySelector("#ruta-linea");
  const current = select.value;
  select.replaceChildren(new Option("Seleccione una línea", ""));
  (recordsByKey.lineas || []).forEach(linea => select.add(new Option(optionLabel("lineas", linea), linea.id)));
  if ([...select.options].some(option => option.value === current)) select.value = current;
  await loadRoute();
}

async function loadRoute() {
  const id = document.querySelector("#ruta-linea").value;
  const version = ++routeRequest;
  const button = document.querySelector("#ruta-editar");
  button.disabled = true;
  savedRoute = [];
  renderRoute();
  if (!id) return;
  try {
    const response = await request(`/api/rutas/${id}`);
    if (version !== routeRequest) return;
    savedRoute = response;
    renderRoute();
    button.disabled = false;
  } catch (error) {
    if (version === routeRequest) {
      document.querySelector("#ruta-resumen").textContent = "No se pudo cargar la ruta.";
      showNotice(error.message, true);
    }
  }
}

function renderRoute() {
  const body = document.querySelector("#ruta-list");
  body.replaceChildren();
  savedRoute.forEach(item => {
    const row = document.createElement("tr");
    const accesos = (recordsByKey.accesos || []).filter(acceso => acceso.estacionId === item.estacionId);
    [item.orden, referenceName("estaciones", item.estacionId), Number(item.distanciaKm).toFixed(3),
      accesos.map(acceso => acceso.codigo).join(", ") || "Sin accesos"].forEach(value => {
      const cell = document.createElement("td");
      cell.textContent = value;
      row.append(cell);
    });
    body.append(row);
  });
  if (!savedRoute.length) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 4;
    cell.className = "empty";
    cell.textContent = "Sin estaciones en esta ruta.";
    row.append(cell);
    body.append(row);
  }
  const id = Number(document.querySelector("#ruta-linea").value);
  const total = savedRoute.reduce((sum, item) => sum + Number(item.distanciaKm), 0);
  const buses = (recordsByKey.buses || []).filter(bus => bus.lineaId === id).length;
  const stations = savedRoute.length;
  const valid = stations > 0 && buses >= stations && buses <= stations * 2;
  document.querySelector("#ruta-resumen").textContent = id
    ? `${stations} estaciones · ${total.toFixed(3)} km · ${buses} buses asignados. ${valid ? "Cumple la cantidad de buses por estación." : "La línea requiere entre uno y dos buses por estación antes de operar."}`
    : "Seleccione una línea.";
}

function captureRouteDraft() {
  document.querySelectorAll(".route-editor-row").forEach((row, index) => {
    routeDraft[index].estacionId = row.querySelector("select").value;
    routeDraft[index].distanciaKm = row.querySelector("input").value;
  });
}

function renderRouteEditor() {
  const editor = document.querySelector("#ruta-editor");
  editor.replaceChildren();
  routeDraft.forEach((item, index) => {
    const row = document.createElement("div");
    row.className = "route-editor-row";
    const actions = document.createElement("div");
    actions.className = "route-editor-actions";
    const title = document.createElement("strong");
    title.textContent = `Estación ${index + 1}`;
    actions.append(title);
    [["Subir", -1], ["Bajar", 1], ["Quitar", 0]].forEach(([label, direction]) => {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "row-action";
      button.textContent = label;
      button.disabled = direction === -1 && index === 0 || direction === 1 && index === routeDraft.length - 1;
      button.addEventListener("click", () => {
        captureRouteDraft();
        if (!direction) routeDraft.splice(index, 1);
        else [routeDraft[index], routeDraft[index + direction]] = [routeDraft[index + direction], routeDraft[index]];
        if (routeDraft.length) routeDraft[0].distanciaKm = 0;
        renderRouteEditor();
      });
      actions.append(button);
    });
    const stationLabel = document.createElement("label");
    stationLabel.textContent = "Estación";
    const select = document.createElement("select");
    select.required = true;
    select.add(new Option("Seleccione una estación", ""));
    (recordsByKey.estaciones || []).forEach(station => select.add(new Option(optionLabel("estaciones", station), station.id)));
    select.value = item.estacionId;
    stationLabel.append(select);
    const distanceLabel = document.createElement("label");
    distanceLabel.textContent = "Distancia desde la anterior (km)";
    const distance = document.createElement("input");
    distance.type = "number";
    distance.required = true;
    distance.min = index ? "0.001" : "0";
    distance.max = "99999.999";
    distance.step = "0.001";
    distance.value = index ? item.distanciaKm : 0;
    distance.readOnly = index === 0;
    distanceLabel.append(distance);
    row.append(actions, stationLabel, distanceLabel);
    editor.append(row);
  });
}

async function saveRoute(event) {
  event.preventDefault();
  captureRouteDraft();
  const button = document.querySelector("#ruta-guardar");
  button.disabled = true;
  try {
    await request(`/api/rutas/${document.querySelector("#ruta-linea").value}`, {
      method: "PUT", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ estaciones: routeDraft.map(item => ({ estacionId: Number(item.estacionId), distanciaKm: Number(item.distanciaKm) })) })
    });
    bootstrap.Modal.getOrCreateInstance(document.querySelector("#ruta-modal")).hide();
    await loadRoute();
    showNotice("Ruta guardada correctamente.");
  } catch (error) {
    document.querySelector("#ruta-error").textContent = error.message;
  } finally {
    button.disabled = false;
  }
}
