import * as dayjs from 'dayjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonthTimeTrackItemComponent } from './month-time-track-item.component';
import { TranslateModule } from '@ngx-translate/core';

describe('MonthTimeTrackItemComponent', () => {
  let fixture: ComponentFixture<MonthTimeTrackItemComponent>;
  let component: MonthTimeTrackItemComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MonthTimeTrackItemComponent],
      imports: [TranslateModule.forRoot()],
    });

    fixture = TestBed.createComponent(MonthTimeTrackItemComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  describe('Selected class on host', () => {
    beforeEach(() => {
      component.data = {
        date: dayjs(),
        days: 23,
        month: 'September',
        progress: 'BAD',
        time: '02:34:12',
        year: 2021,
      };
    });

    it('should element have class', () => {
      component.isSelected = true;

      fixture.detectChanges();

      expect(hostElement).toHaveClass('selected');
    });

    it('should NOT has', () => {
      fixture.detectChanges();

      expect(hostElement).not.toHaveClass('selected');
    });
  });
});
