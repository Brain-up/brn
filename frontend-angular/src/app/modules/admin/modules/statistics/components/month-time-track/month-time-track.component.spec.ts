import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonthTimeTrackComponent } from './month-time-track.component';

describe('MonthTimeTrackComponent', () => {
  let fixture: ComponentFixture<MonthTimeTrackComponent>;
  let component: MonthTimeTrackComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MonthTimeTrackComponent],
    });

    fixture = TestBed.createComponent(MonthTimeTrackComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
