import { Injectable, computed, signal } from '@angular/core';
import i18next from 'i18next';
import { LanguageCode } from '../models/auth.model';
import { resources } from '../i18n/resources';
import { TokenStorageService } from './token-storage.service';

const supportedLanguages: LanguageCode[] = ['en', 'et', 'fi'];

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly languageSignal = signal<LanguageCode>('en');
  readonly language = this.languageSignal.asReadonly();
  readonly languages = supportedLanguages;
  readonly ready = signal(false);
  readonly currentLanguageLabel = computed(() => this.languageSignal().toUpperCase());

  constructor(private readonly storage: TokenStorageService) {}

  async init(): Promise<void> {
    const storedLanguage = this.storage.getLanguage() ?? 'en';
    await i18next.init({
      lng: storedLanguage,
      fallbackLng: 'en',
      resources
    });
    this.languageSignal.set(storedLanguage);
    this.ready.set(true);
  }

  async setLanguage(language: LanguageCode): Promise<void> {
    await i18next.changeLanguage(language);
    this.storage.saveLanguage(language);
    this.languageSignal.set(language);
  }

  t(key: string): string {
    return i18next.t(key, { defaultValue: key });
  }
}
