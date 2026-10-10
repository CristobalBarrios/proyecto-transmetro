const password = document.querySelector("#password");
const passwordToggle = document.querySelector("#password-toggle");
const message = document.querySelector("#login-message");

passwordToggle.addEventListener("click", () => {
  const visible = password.type === "text";
  password.type = visible ? "password" : "text";
  passwordToggle.textContent = visible ? "Mostrar" : "Ocultar";
  passwordToggle.setAttribute("aria-pressed", String(!visible));
});

if (new URLSearchParams(window.location.search).has("error")) {
  message.textContent = "Usuario o contraseña incorrectos.";
}

fetch("/api/csrf", { credentials: "same-origin" })
  .then(response => {
    if (!response.ok) throw new Error("No se pudo preparar el inicio de sesión.");
    return response.json();
  })
  .then(token => {
    const input = document.querySelector("#csrf-token");
    input.name = token.parametro;
    input.value = token.valor;
  })
  .catch(error => {
    message.textContent = error.message;
    document.querySelector("#login-form button[type='submit']").disabled = true;
  });
