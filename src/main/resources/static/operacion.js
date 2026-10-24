function operationCell(row, value) {
  const cell = document.createElement("td");
  cell.textContent = value;
  row.append(cell);
  return cell;
}

function operationAction(row, label, action) {
  const cell = operationCell(row, "");
  const button = document.createElement("button");
  button.className = "row-action";
  button.type = "button";
  button.textContent = label;
  button.addEventListener("click", async () => {
    button.disabled = true;
    try { await action(); await loadAll(); showNotice("Cambio guardado correctamente."); }
    catch (error) { showNotice(error.message, true); }
    finally { button.disabled = false; }
  });
  cell.append(button);
}

async function refreshOperationPanels() {
  const [lines, assignments] = await Promise.all([
    ["administrador", "supervisor", "operador"].includes(sesion.perfil) ? request("/api/operacion/lineas") : [], request("/api/asignaciones-guardias")
  ]);
  const body = document.querySelector("#operacion-list");
  body.replaceChildren();
  lines.forEach(line => {
    const row = document.createElement("tr");
    [line.codigo, line.nombre, line.estado, line.estaciones, line.buses,
      line.pendientes.join(" ") || "Lista para operar"].forEach(value => operationCell(row, value));
    const active = line.estado === "operativa";
    operationAction(row, active ? "Desactivar" : "Activar", () => request(`/api/operacion/lineas/${line.id}/estado`, {
      method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estado: active ? "inactiva" : "operativa" })
    }));
    body.append(row);
  });
  const history = document.querySelector("#asignaciones-list");
  history.replaceChildren();
  assignments.forEach(item => {
    const row = document.createElement("tr");
    [item.guardia, item.acceso, item.estacion, item.responsable,
      new Date(item.fechaInicio).toLocaleString("es-GT"), item.fechaFin ? new Date(item.fechaFin).toLocaleString("es-GT") : "-", item.estado]
      .forEach(value => operationCell(row, value));
    if (item.estado === "activa") operationAction(row, "Finalizar", () => request(`/api/asignaciones-guardias/${item.id}/finalizar`, { method: "PUT" }));
    else operationCell(row, "Finalizada");
    history.append(row);
  });
  if (!assignments.length) {
    const row = document.createElement("tr");
    operationCell(row, "No hay asignaciones registradas.").colSpan = 8;
    history.append(row);
  }
  for (const [id, records, label] of [
    ["asignacion-guardia", recordsByKey.guardias || [], item => item.nombre],
    ["asignacion-acceso", recordsByKey.accesos || [], item => `${item.codigo} · ${referenceName("estaciones", item.estacionId)}`]
  ]) {
    const select = document.getElementById(id);
    const current = select.value;
    select.replaceChildren(new Option("Seleccione una opción", ""));
    records.forEach(item => select.add(new Option(label(item), item.id)));
    select.value = current;
  }
}

function configureAssignments() {
  const modal = document.querySelector("#asignacion-modal");
  document.querySelector("#asignacion-nueva").addEventListener("click", () => {
    document.querySelector("#asignacion-form").reset();
    document.querySelector("#asignacion-error").textContent = "";
    bootstrap.Modal.getOrCreateInstance(modal).show();
  });
  document.querySelector("#asignacion-form").addEventListener("submit", async event => {
    event.preventDefault();
    const button = document.querySelector("#asignacion-guardar");
    button.disabled = true;
    try {
      await request("/api/asignaciones-guardias", { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ guardiaId: Number(document.querySelector("#asignacion-guardia").value), accesoId: Number(document.querySelector("#asignacion-acceso").value) }) });
      bootstrap.Modal.getOrCreateInstance(modal).hide();
      await loadAll();
      showNotice("Guardia asignado correctamente.");
    } catch (error) { document.querySelector("#asignacion-error").textContent = error.message; }
    finally { button.disabled = false; }
  });
}
