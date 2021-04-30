import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { StatisticsInfoDialogComponent } from './statistics-info-dialog.component';

describe('StatisticsInfoDialogComponent', () => {
  let component: StatisticsInfoDialogComponent;
  let fixture: ComponentFixture<StatisticsInfoDialogComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [StatisticsInfoDialogComponent],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(StatisticsInfoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
