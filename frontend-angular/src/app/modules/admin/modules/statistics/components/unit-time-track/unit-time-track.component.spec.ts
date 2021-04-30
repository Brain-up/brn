import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UnitTimeTrackComponent } from './unit-time-track.component';

describe('UnitTimeTrackComponent', () => {
  let component: UnitTimeTrackComponent;
  let fixture: ComponentFixture<UnitTimeTrackComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UnitTimeTrackComponent],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UnitTimeTrackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
