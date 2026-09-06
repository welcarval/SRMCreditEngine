const API_BASE = import.meta.env.VITE_API_URL || '';

export async function request(path, options = {}) {
  const token = localStorage.getItem('operator_token');
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    const detail = await response.text();
    throw new Error(detail || `A API retornou ${response.status}.`);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  funds: () => request('/api/fundos'),
  fund: (id) => request(`/api/fundos/${id}`),
  createFund: (payload) => request('/api/fundos', { method: 'POST', body: JSON.stringify(payload) }),
  updateFund: (id, payload) => request(`/api/fundos/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteFund: (id) => request(`/api/fundos/${id}`, { method: 'DELETE' }),
  receivables: () => request('/api/recebiveis'),
  receivable: (id) => request(`/api/recebiveis/${id}`),
  createReceivable: (payload) => request('/api/recebiveis', { method: 'POST', body: JSON.stringify(payload) }),
  updateReceivable: (id, payload) => request(`/api/recebiveis/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteReceivable: (id) => request(`/api/recebiveis/${id}`, { method: 'DELETE' }),
  companies: () => request('/api/empresas')
};
