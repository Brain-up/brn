import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, HostListener } from '@angular/core';

@Component({
  selector: 'app-toggler',
  templateUrl: './toggler.component.html',
  styleUrls: ['./toggler.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TogglerComponent {
  @Input()
  public state = false;

  @Input()
  public offText = '-';

  @Input()
  public onText = '-';

  @Output()
  public toggleEvent = new EventEmitter<boolean>();

  @HostListener('click')
  public toggle(): void {
    this.toggleEvent.emit(!this.state);
  }
}
