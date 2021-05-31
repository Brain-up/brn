import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { PAGE_SIZE_DEFAULT } from '@shared/constants/common-constants';

@Component({
  selector: 'app-paginator',
  templateUrl: './paginator.component.html',
  styleUrls: ['./paginator.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginatorComponent {
  public get startRange(): number {
    const start = (this.pageNumber - 1) * this.pageSize + 1;

    return start > this.total ? this.total : start;
  }

  public get endRange(): number {
    const end = this.pageNumber * this.pageSize;

    return end > this.total ? this.total : end;
  }

  @Input()
  public pageSize = PAGE_SIZE_DEFAULT;

  @Input()
  public total: number;

  @Input()
  public pageNumber = 1;

  @Output()
  public selectPageEvent = new EventEmitter<number>();
}
