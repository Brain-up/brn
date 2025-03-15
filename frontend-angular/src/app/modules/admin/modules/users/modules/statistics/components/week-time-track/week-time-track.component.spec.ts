import { NO_ERRORS_SCHEMA } from '@angular/core';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { WeekTimeTrackComponent } from './week-time-track.component';
import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import dayjs from 'dayjs';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';

describe('WeekTimeTrackComponent', () => {
  let fixture: ComponentFixture<WeekTimeTrackComponent>;
  let component: WeekTimeTrackComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [TranslateModule.forRoot(), WeekTimeTrackComponent],
    schemas: [NO_ERRORS_SCHEMA],
});

    fixture = TestBed.createComponent(WeekTimeTrackComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return on input property data if null', fakeAsync(() => {
    const data: BarDataType = [
      ['1', 1234],
      ['2', 5678],
    ];
    component.barData = data;
    component.initialIndex = 1;
    const selectedDay = dayjs();
    component.selectedDay = selectedDay;
    component.data = undefined;
    tick();
    expect(component.barData).toBe(data);
    expect(component.initialIndex).toBe(1);
    expect(component.selectedDay).toBe(selectedDay);
  }));

  it('should return on input property data if null', fakeAsync(() => {
    const data: BarDataType = null;
    component.barData = data;
    component.data = undefined;
    tick();
    expect(component.barData).toBe(data);
  }));

  it('should return on input property data if null', fakeAsync(() => {
    const data: BarDataType = [
      ['1', 1234],
      ['2', 5678],
    ];
    component.barData = data;
    component.data = undefined;
    tick();
    expect(component.barData).toBe(data);
  }));

  it('should calculate initial bar indexz and selected day on input property data', fakeAsync(() => {
    const lastStatisticDayInMonth = '2022-01-25T00:00:00';
    const data: UserWeeklyStatistics[] = [
      {
        date: '2022-01-01T00:00:00',
        exercisingTimeSeconds: 10,
        progress: 'GOOD',
      },
      {
        date: '2022-01-15T00:00:00',
        exercisingTimeSeconds: 10,
        progress: 'GOOD',
      },
      {
        date: lastStatisticDayInMonth,
        exercisingTimeSeconds: 10,
        progress: 'GOOD',
      },
    ];
    fixture.componentRef.setInput('selectedMonth', dayjs(lastStatisticDayInMonth));
    component.data = data;
    tick();
    expect(component.initialIndex).toBe(24);
    expect(component.selectedDay.date).toBe(dayjs(lastStatisticDayInMonth).date);
  }));

  it('should isAllowNextMonth set to false', () => {
    fixture.componentRef.setInput('selectedMonth', dayjs());
    component.isAllowNextMonth();
    expect(component.isAllowNextMonth()).toBeFalsy();
  });

  it('should loadNextMonth not emit event', () => {
    fixture.componentRef.setInput('selectedMonth', dayjs());
    const loadNextMonthEventSpy = spyOn(component.loadNextMonthEvent, 'emit');
    component.loadNextMonth();
    expect(loadNextMonthEventSpy).toHaveBeenCalledTimes(0);
  });

  it('should loadNextMonth emit event', () => {
    fixture.componentRef.setInput('selectedMonth', dayjs().subtract(1, 'month'));
    const loadNextMonthEventSpy = spyOn(component.loadNextMonthEvent, 'emit');
    component.loadNextMonth();
    expect(loadNextMonthEventSpy).toHaveBeenCalledTimes(1);
  });

  it('should loadPrevMonth emit event', () => {
    const loadPrevMonthEventSpy = spyOn(component.loadPrevMonthEvent, 'emit');
    component.loadPrevMonth();
    expect(loadPrevMonthEventSpy).toHaveBeenCalledTimes(1);
  });

  it('should onClickItem return right selected day of month', () => {
    const selectedMonth = Date.UTC(2022, 0, 10);
    fixture.componentRef.setInput('selectedMonth', dayjs(selectedMonth));
    for (let i = 0; i < 31; i++) {
      component.onClickItem(i);
      const date = component.selectedMonth().clone();
      expect(component.selectedDay).toEqual(date.set('date', i));
    }
  });
});
