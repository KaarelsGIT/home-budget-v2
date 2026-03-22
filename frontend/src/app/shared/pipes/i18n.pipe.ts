import { ChangeDetectorRef, Pipe, PipeTransform } from '@angular/core';
import { I18nService } from '../../core/services/i18n.service';

@Pipe({
  name: 'i18n',
  standalone: true,
  pure: false
})
export class I18nPipe implements PipeTransform {
  constructor(
    private readonly i18nService: I18nService,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.i18nService.language();
    this.cdr.markForCheck();
  }

  transform(key: string): string {
    this.i18nService.language();
    return this.i18nService.t(key);
  }
}
