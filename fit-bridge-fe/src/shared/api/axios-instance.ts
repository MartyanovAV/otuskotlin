import Axios, { type AxiosError, type AxiosRequestConfig } from 'axios';
// Импортируем keycloak-объект напрямую: токен всегда актуален после рефреша
import keycloak from '@/features/auth/keycloak';

export const AXIOS_INSTANCE = Axios.create({
  // В dev используется vite-proxy (/api → http://localhost:8080/v2).
  // В production baseURL берётся из переменной окружения VITE_API_BASE_URL.
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
});

// JWT-interceptor: читаем токен напрямую из keycloak, обновляя при необходимости
AXIOS_INSTANCE.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      // Обновляем токен, если он истекает в течение 30 секунд
      await keycloak.updateToken(30);
    } catch {
      // Игнорируем ошибку здесь, при 401 сработает response interceptor
    }
  }
  const token = keycloak.token;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Обработка 401: keycloak.logout() перенаправит на страницу входа Keycloak
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.logout();
    }
    return Promise.reject(error);
  }
);

// Тип для отмены запроса (совместим с интерфейсом Orval)
export type PromiseWithCancel<T> = Promise<T> & { cancel: () => void };

export interface CustomInstanceOptions extends AxiosRequestConfig {
  body?: unknown;
}

export const customInstance = <T>(
  url: string,
  options?: CustomInstanceOptions
): PromiseWithCancel<T> => {
  const controller = new AbortController();
  const { body, ...restOptions } = options ?? {};

  let data = restOptions.data;
  if (data === undefined && body !== undefined) {
    if (typeof body === 'string') {
      try {
        data = JSON.parse(body);
      } catch {
        data = body;
      }
    } else {
      data = body;
    }
  }

  const promise = AXIOS_INSTANCE({
    url,
    data,
    ...restOptions,
    signal: controller.signal,
  }).then((res) => ({
    data: res.data,
    status: res.status,
    headers: res.headers,
  })) as PromiseWithCancel<T>;

  promise.cancel = () => {
    controller.abort('Query was cancelled');
  };

  return promise;
};

export type ErrorType<Error> = AxiosError<Error>;
