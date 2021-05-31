import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import * as dayjs from 'dayjs';
import { MonthTimeTrackItemComponent } from './month-time-track-item.component';

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
        progress: 'BAD',
        time: '02:34:12',
        days: 23,
        month: 'September',
        year: 2021,
        date: dayjs(),
      };
    });

    it('should has', () => {
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
