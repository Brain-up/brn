import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiagnosticsTrackComponent } from './diagnostics-track.component';

describe('DiagnosticsTrackComponent', () => {
  let component: DiagnosticsTrackComponent;
  let fixture: ComponentFixture<DiagnosticsTrackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DiagnosticsTrackComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DiagnosticsTrackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
