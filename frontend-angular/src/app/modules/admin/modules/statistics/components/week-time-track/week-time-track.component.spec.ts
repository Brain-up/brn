import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WeekTimeTrackComponent } from './week-time-track.component';

describe('WeekTimeTrackComponent', () => {
  let fixture: ComponentFixture<WeekTimeTrackComponent>;
  let component: WeekTimeTrackComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WeekTimeTrackComponent],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(WeekTimeTrackComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
