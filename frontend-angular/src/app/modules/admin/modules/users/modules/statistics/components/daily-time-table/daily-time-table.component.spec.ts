import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyTimeTableComponent } from './daily-time-table.component';

describe('DailyTimeTableComponent', () => {
  let component: DailyTimeTableComponent;
  let fixture: ComponentFixture<DailyTimeTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DailyTimeTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DailyTimeTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
