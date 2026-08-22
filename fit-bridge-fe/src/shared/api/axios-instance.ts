import Axios, { type AxiosError, type AxiosRequestConfig } from 'axios';
// Импортируем keycloak-объект напрямую: токен всегда актуален после рефреша
import keycloak from '@/features/auth/keycloak';
import { fitBridgeConfig } from '@/shared/config/runtime';

export const AXIOS_INSTANCE = Axios.create({
  // Docker deployment gets this value from config.js; Vite .env remains a dev fallback.
  baseURL: fitBridgeConfig.apiBaseUrl,
});

// JWT-interceptor: читаем токен напрямую из keycloak, обновляя при необходимости
AXIOS_INSTANCE.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      // Обновляем токен, если он истекает в течение 30 секунд
      await keycloak.updateToken(30);
    } catch (error) {
      // Не отправляем заведомо просроченный JWT и завершаем повреждённую сессию.
      void keycloak.logout();
      return Promise.reject(error);
    }
  }
  const token = keycloak.token;
  if (!token) {
    void keycloak.logout();
    return Promise.reject(new Error('Access token is unavailable'));
  }
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Обработка 401: keycloak.logout() перенаправит на страницу входа Keycloak
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      void keycloak.logout();
    }
    return Promise.reject(error);
  }
);

// Тип для отмены запроса (совместим с интерфейсом Orval)
export type PromiseWithCancel<T> = Promise<T> & { cancel: () => void };

export interface CustomInstanceOptions extends AxiosRequestConfig {
  body?: unknown;
}

type ApiErrorPayload = {
  result?: string;
  errors?: Array<{ message?: string }>;
};

export class ApiResponseError extends Error {
  readonly payload: ApiErrorPayload;

  constructor(payload: ApiErrorPayload) {
    super(payload.errors?.[0]?.message ?? 'Произошла ошибка, попробуйте позже');
    this.name = 'ApiResponseError';
    this.payload = payload;
  }
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
  }).then((res) => {
    const payload = res.data as ApiErrorPayload | undefined;
    if (payload?.result === 'error') {
      throw new ApiResponseError(payload);
    }

    return {
      data: res.data,
      status: res.status,
      headers: res.headers,
    };
  }) as PromiseWithCancel<T>;

  promise.cancel = () => {
    controller.abort('Query was cancelled');
  };

  return promise;
};

export type ErrorType<Error> = AxiosError<Error>;
