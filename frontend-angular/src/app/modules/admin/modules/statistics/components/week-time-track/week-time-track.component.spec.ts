import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { WeekTimeTrackComponent } from './week-time-track.component';

describe('WeekTimeTrackComponent', () => {
  let fixture: ComponentFixture<WeekTimeTrackComponent>;
  let component: WeekTimeTrackComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WeekTimeTrackComponent],
      imports: [TranslateModule.forRoot()],
    });

    fixture = TestBed.createComponent(WeekTimeTrackComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
