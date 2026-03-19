import { Pipe, PipeTransform } from '@angular/core';
import { TransactionType } from '../../core/models/transaction.model';

@Pipe({
  name: 'signedAmount',
  standalone: true
})
export class SignedAmountPipe implements PipeTransform {
  transform(amount: number, type: TransactionType): string {
    if (type === 'INCOME') {
      return `+${amount}`;
    }

    if (type === 'EXPENSE') {
      return `-${amount}`;
    }

    return `${amount}`;
  }
}
