import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonthTimeTrackItemComponent } from './month-time-track-item.component';

describe('MonthTimeTrackItemComponent', () => {
  let fixture: ComponentFixture<MonthTimeTrackItemComponent>;
  let component: MonthTimeTrackItemComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MonthTimeTrackItemComponent],
    });

    fixture = TestBed.createComponent(MonthTimeTrackItemComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
