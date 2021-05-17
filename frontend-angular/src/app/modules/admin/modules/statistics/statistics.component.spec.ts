import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { StatisticsComponent } from './statistics.component';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import * as dayjs from 'dayjs';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';

describe('StatisticsComponent', () => {
  const responseDelayInMs = 0;
  const tickInMs = responseDelayInMs + 100;

  let fixture: ComponentFixture<StatisticsComponent>;
  let component: StatisticsComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatisticsComponent],
      imports: [RouterTestingModule, TranslateModule.forRoot(), MatDialogModule],
      providers: [{ provide: AdminApiService, useFactory: () => new AdminApiServiceFake({ responseDelayInMs }) }],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(StatisticsComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  describe('Loading data', () => {
    it('hook ngOnInit', fakeAsync(() => {
      component.ngOnInit();

      tick(tickInMs);

      expect(component.weekTimeTrackData).toBeTruthy();
      expect(component.monthTimeTrackData).toBeTruthy();
    }));

    it('method selectMonth', fakeAsync(() => {
      const currentMonth = component.selectedMonth.month();

      component.selectMonth(dayjs().subtract(1, 'month'));

      tick(tickInMs);

      expect(component.selectedMonth.month()).toBe(currentMonth - 1);
      expect(component.weekTimeTrackData).toBeTruthy();
    }));

    it('method loadPrevYear', fakeAsync(() => {
      const currentYear = component.selectedMonth.year();

      component.loadPrevYear();

      tick(tickInMs);

      expect(component.selectedMonth.year()).toBe(currentYear - 1);
      expect(component.weekTimeTrackData).toBeTruthy();
      expect(component.monthTimeTrackData).toBeTruthy();
    }));

    it('method loadNextYear', fakeAsync(() => {
      const currentYear = component.selectedMonth.year();

      component.loadNextYear();

      tick(tickInMs);

      expect(component.selectedMonth.year()).toBe(currentYear + 1);
      expect(component.weekTimeTrackData).toBeTruthy();
      expect(component.monthTimeTrackData).toBeTruthy();
    }));
  });

  it('should opened statistics info dialog', () => {
    const matDialogOpenSpy = spyOn(component.matDialog, 'open');
    const statisticsInfoDialogButtonElem = hostElement.querySelector<HTMLButtonElement>(
      'button.statistics-info-dialog'
    );

    statisticsInfoDialogButtonElem.click();

    expect(matDialogOpenSpy).toHaveBeenCalledWith(StatisticsInfoDialogComponent, { width: '650px' });
  });
});
