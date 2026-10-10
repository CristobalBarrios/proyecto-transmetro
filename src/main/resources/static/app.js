const api = "/api/lineas";
const notice = document.querySelector("#notice");
let csrfToken;

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

  const response = await fetch(`${api}${path}`, { ...options, headers, credentials: "same-origin" });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || "No fue posible completar la operación.");
  }
  return response.json();
}

async function loadLines() {
  try {
    const lines = await request("");
    const list = document.querySelector("#lineas-list");
    list.replaceChildren();

    if (!lines.length) {
      const empty = document.createElement("p");
      empty.className = "empty";
      empty.textContent = "No hay líneas registradas.";
      list.append(empty);
      return;
    }

    lines.forEach(line => {
      const row = document.createElement("div");
      const code = document.createElement("strong");
      const name = document.createElement("span");
      row.className = "row";
      code.textContent = line.codigo;
      name.textContent = line.nombre;
      row.append(code, name);
      list.append(row);
    });
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
