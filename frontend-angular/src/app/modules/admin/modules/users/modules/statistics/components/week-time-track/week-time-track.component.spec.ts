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
import * as dayjs from 'dayjs';
import { CompileTemplateMetadata } from '@angular/compiler';

describe('WeekTimeTrackComponent', () => {
  let fixture: ComponentFixture<WeekTimeTrackComponent>;
  let component: WeekTimeTrackComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WeekTimeTrackComponent],
      imports: [TranslateModule.forRoot()],
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

  it('should isAllowNextMonth set to false', () => {
    component.selectedMonth = dayjs();
    component.isAllowNextMonth();
    expect(component.isAllowNextMonth()).toBeFalsy();
  });

  it('should loadNextMonth not emit event', () => {
    component.selectedMonth = dayjs();
    const loadNextMonthEventSpy = spyOn(component.loadNextMonthEvent, 'emit');
    component.loadNextMonth();
    expect(loadNextMonthEventSpy).toHaveBeenCalledTimes(0);
  });

  it('should loadNextMonth emit event', () => {
    component.selectedMonth = dayjs().subtract(1, 'month');
    const loadNextMonthEventSpy = spyOn(component.loadNextMonthEvent, 'emit');
    component.loadNextMonth();
    expect(loadNextMonthEventSpy).toHaveBeenCalledTimes(1);
  });

  it('should loadPrevMonth emit event', () => {
    const loadPrevMonthEventSpy = spyOn(component.loadPrevMonthEvent, 'emit');
    component.loadPrevMonth();
    expect(loadPrevMonthEventSpy).toHaveBeenCalledTimes(1);
  });
});
