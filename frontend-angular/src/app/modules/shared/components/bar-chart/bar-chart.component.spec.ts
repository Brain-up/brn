import { BarDataType } from './models/bar-data';
import { ComponentFixture, TestBed, tick } from '@angular/core/testing';
import { BarChartComponent, SELECTED_BAR_CLASS_NAME } from './bar-chart.component';
import { BarOptionsType } from './models/bar-options';
import { ElementRef } from '@angular/core';

describe('BarChartComponent', () => {
  let fixture: ComponentFixture<BarChartComponent>;
  let component: BarChartComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [BarChartComponent],
});

    fixture = TestBed.createComponent(BarChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not set data', () => {
    component.data = null;
    fixture.detectChanges();
    expect(component[`chartColumns`]).toEqual([]);
  });

  it('should set data', () => {
    const data: BarDataType = [
      ['1', 1234],
      ['2', 5678],
    ];
    component.data = data;
    fixture.detectChanges();
    expect(component[`chartColumns`]).toBeTruthy();
  });

  it('should not set options', () => {
    component.options = null;
    fixture.detectChanges();
    expect(component[`chartOptions`]).toEqual(undefined);
  });

  it('should set options', () => {
    const options: BarOptionsType = { axis: { x: null }, bar: { width: 200 } };
    component.options = options;
    fixture.detectChanges();
    expect(component[`chartOptions`]).toBeTruthy();
  });

  it('should not be add class SELECTED_BAR_CLASS_NAME to bar when no initialBarIndex setted', () => {
    const daysInMonth = 31;
    const daysData = Array.from({length: daysInMonth}, (v, i) => i);
    const barData: BarDataType = [
      ['data', ...daysData],
    ];
    const childViewElement: HTMLElement = document.createElement('div');
    component.chartElemRef = new ElementRef<any>(childViewElement);
    component.data = barData;
    component.ngAfterViewInit();
    for (let i = 0; i < daysInMonth; i++) {
      const querySelector = childViewElement.querySelector('.bb-bar-' + i);
      const hasSelectedClass = querySelector.classList.contains(SELECTED_BAR_CLASS_NAME);
      expect(hasSelectedClass).toBeFalse();
    }
  });

  it('should be add class SELECTED_BAR_CLASS_NAME to bar when receive initialBarIndex', () => {
    const daysInMonth = 31;
    const daysData = Array.from({length: daysInMonth}, (v, i) => i);
    const barData: BarDataType = [['data', ...daysData]];
    const childViewElement: HTMLElement = document.createElement('div');
    component.chartElemRef = new ElementRef<any>(childViewElement);
    component.data = barData;
    for (let i = 0; i < daysInMonth; i++) {
      component.initialBarIndex = i;
      component.ngAfterViewInit();
      const element = childViewElement.querySelector('.bb-bar-' + i);
      const hasSelectedClass = element.classList?.contains(SELECTED_BAR_CLASS_NAME);
      expect(hasSelectedClass).toBeTrue();
    }
  });
});
