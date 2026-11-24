// Adjust this URL and the invoice paths when the Spring Boot API is implemented.
export const API_BASE_URL = 'http://localhost:8080/api';

export async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: { Accept: 'application/json', ...options.headers }
    });
  } catch (error) {
    throw new Error(`Unable to reach the API: ${error.message}`);
  }
  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); }
    catch { if (response.ok) throw new Error('The API returned invalid JSON.'); }
  }
  if (!response.ok) throw new Error(data?.message || `API request failed (${response.status}).`);
  return data;
}

// These paths are placeholders; no requests are made until explicitly called.
export async function createInvoice(invoice) {
  return request('/invoices', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(invoice)
  });
}

export async function getInvoice(id) {
  return request(`/invoices/${encodeURIComponent(id)}`);
}

export async function getInvoices() {
  return request('/invoices');
}
