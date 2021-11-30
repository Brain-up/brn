import { BarDataType } from './models/bar-data';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarChartComponent } from './bar-chart.component';
import { BarOptionsType } from './models/bar-options';

describe('BarChartComponent', () => {
  let fixture: ComponentFixture<BarChartComponent>;
  let component: BarChartComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BarChartComponent],
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
});
