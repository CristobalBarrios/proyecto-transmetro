const api = "/api/lineas";
const notice = document.querySelector("#notice");

function showNotice(message, isError = false) {
  notice.textContent = message;
  notice.style.background = isError ? "#a2192a" : "#172b78";
  notice.classList.add("show");
  window.setTimeout(() => notice.classList.remove("show"), 3500);
}

async function request(path, options) {
  const response = await fetch(`${api}${path}`, options);
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || "No fue posible completar la operación.");
  }
  return response.json();
}

async function loadLines() {
  try {
    const lines = await request("");
    document.querySelector("#lineas-list").innerHTML = lines.length
      ? lines.map(line => `<div class="row"><strong>${line.codigo}</strong><span>${line.nombre}</span></div>`).join("")
      : '<p class="empty">No hay líneas registradas.</p>';
  } catch (error) {
    showNotice(`No se pudo cargar las líneas: ${error.message}`, true);
  }
}

document.querySelector("#linea-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  try {
    await request("", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) });
    form.reset();
    showNotice("Línea guardada correctamente.");
    await loadLines();
  } catch (error) {
    showNotice(error.message, true);
  }
});

loadLines();
