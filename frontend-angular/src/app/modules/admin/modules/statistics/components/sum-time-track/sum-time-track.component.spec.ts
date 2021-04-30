import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SumTimeTrackComponent } from './sum-time-track.component';

describe('SumTimeTrackComponent', () => {
  let component: SumTimeTrackComponent;
  let fixture: ComponentFixture<SumTimeTrackComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [SumTimeTrackComponent],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(SumTimeTrackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
