export async function api(endpoint, options = {}) {
  const token = localStorage.getItem('clinitech_token');
  const res = await fetch(endpoint, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
    body: options.body ? (typeof options.body === 'string' ? options.body : JSON.stringify(options.body)) : undefined,
  });

  if (!res.ok) {
    if (res.status === 401) {
      localStorage.removeItem('clinitech_token');
      localStorage.removeItem('clinitech_user');
    }
    const text = await res.text().catch(() => '');
    let errData = {};
    try {
      errData = text && text.trim() ? JSON.parse(text) : {};
    } catch {
      errData = {};
    }

    const fallbackMessage =
      res.status === 401
        ? 'El usuario o la contraseña son incorrectos. Por favor, verifica tus datos.'
        : res.status === 403
        ? 'No tienes permisos para realizar esta acción.'
        : res.status === 404
        ? 'El recurso solicitado no fue encontrado.'
        : res.status === 409
        ? (errData.message || 'La operación entra en conflicto con datos existentes (ej. DNI, CMP o Usuario ya registrado).')
        : res.status >= 500
        ? 'El servidor no pudo procesar la solicitud en este momento. Intenta de nuevo más tarde.'
        : 'Ocurrió un error al procesar la solicitud.';

    const message = errData.message || errData.mensaje || errData.error || fallbackMessage;
    const errorObj = new Error(message);
    errorObj.status = res.status;
    errorObj.fields = errData.fields || {};
    throw errorObj;
  }

  // Si la respuesta está vacía (ej. 201 Created sin cuerpo, 204 No Content), no invocar JSON.parse
  const text = await res.text();
  if (!text || text.trim() === '') {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}
