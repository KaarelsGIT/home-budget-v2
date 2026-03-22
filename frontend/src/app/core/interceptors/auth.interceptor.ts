import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { I18nService } from '../services/i18n.service';
import { TokenStorageService } from '../services/token-storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const i18nService = inject(I18nService);
  const token = tokenStorage.getToken();
  const headers: Record<string, string> = {
    'Accept-Language': i18nService.language()
  };

  if (token && !req.url.includes('/auth/')) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return next(req.clone({ setHeaders: headers }));
};
