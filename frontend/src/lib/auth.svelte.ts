import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { UserManager, WebStorageStateStore } from 'oidc-client-ts';
import type { User, UserManagerSettings } from 'oidc-client-ts';
import { env } from '$env/dynamic/public';

const config: UserManagerSettings = {
  authority: `${env.PUBLIC_KEYCLOAK_URL || 'http://localhost:8180'}/realms/${env.PUBLIC_KEYCLOAK_REALM || 'srm-credit-engine'}`,
  client_id: env.PUBLIC_KEYCLOAK_CLIENT_ID || 'srm-frontend',
  redirect_uri: browser ? `${window.location.origin}/auth/callback` : '',
  post_logout_redirect_uri: browser ? `${window.location.origin}/login` : '',
  response_type: 'code',
  scope: 'openid profile email',
  userStore: browser ? new WebStorageStateStore({ store: window.sessionStorage }) : undefined
};

export const auth = $state<{ user: User | null; loading: boolean; error: string }>({ user: null, loading: true, error: '' });
let manager: UserManager | undefined;

function getManager(): UserManager {
  if (!browser) throw new Error('A autenticação OIDC só pode ser iniciada no navegador.');
  manager ??= new UserManager(config);
  return manager;
}

export async function initializeAuth() {
  if (!browser) return;
  try {
    auth.user = (await getManager().getUser()) ?? null;
  } catch (error: unknown) {
    auth.error = error instanceof Error ? error.message : 'Não foi possível iniciar a autenticação.';
  } finally {
    auth.loading = false;
  }
}

export async function login() {
  auth.error = '';
  await getManager().signinRedirect();
}

export async function completeLogin() {
  auth.user = (await getManager().signinCallback()) ?? null;
  await goto('/');
}

export async function logout() {
  const currentUser = auth.user;
  auth.user = null;
  await getManager().signoutRedirect({ id_token_hint: currentUser?.id_token });
}

export function accessToken() {
  return auth.user?.access_token || '';
}
