import * as dayjs from 'dayjs';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonthTimeTrackComponent } from './month-time-track.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

describe('MonthTimeTrackComponent', () => {
  let fixture: ComponentFixture<MonthTimeTrackComponent>;
  let component: MonthTimeTrackComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [TranslateModule.forRoot(), MonthTimeTrackComponent],
    schemas: [NO_ERRORS_SCHEMA],
});

    fixture = TestBed.createComponent(MonthTimeTrackComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  describe('Click on element', () => {
    describe('Prev load year button', () => {
      it('should emit prev event', () => {
        const prevButtonElem =
          hostElement.querySelector<HTMLButtonElement>('button.prev');
        const loadPrevYearEventEmitSpy = spyOn(
          component.loadPrevYearEvent,
          'emit',
        );

        prevButtonElem.click();

        expect(loadPrevYearEventEmitSpy).toHaveBeenCalledTimes(1);
      });
    });

    describe('Next load year button', () => {
      let nextButtonElem: HTMLButtonElement;

      beforeEach(() => {
        nextButtonElem =
          hostElement.querySelector<HTMLButtonElement>('button.next');
      });

      it('should emit next event', () => {
        component.selectedMonth = dayjs().subtract(1, 'year');
        const loadNextYearEventEmitSpy = spyOn(
          component.loadNextYearEvent,
          'emit',
        );

        nextButtonElem.click();

        expect(loadNextYearEventEmitSpy).toHaveBeenCalledTimes(1);
      });

      it('should NOT emit next event', () => {
        component.selectedMonth = dayjs();
        const loadNextYearEventEmitSpy = spyOn(
          component.loadNextYearEvent,
          'emit',
        );

        nextButtonElem.click();

        expect(loadNextYearEventEmitSpy).toHaveBeenCalledTimes(0);
      });
    });

    describe('Select month', () => {
      it('should emit select month event', async () => {
        component.isLoading = false;
        component.selectedMonth = dayjs();
        component.data = await new AdminApiServiceFake({
          responseDelayInMs: 100,
        })
          .getUserYearlyStatistics(
            1,
            component.selectedMonth().startOf('year'),
            component.selectedMonth().endOf('year'),
          )
          .toPromise();

        fixture.detectChanges();

        const appMonthTimeTrackItemElem =
          hostElement.querySelector<HTMLElement>('app-month-time-track-item');
        const selectMonthEventEmitSpy = spyOn(
          component.selectMonthEvent,
          'emit',
        );

        appMonthTimeTrackItemElem.click();

        expect(selectMonthEventEmitSpy).toHaveBeenCalledWith(
          component.monthTimeTrackItemsData[0].date,
        );
      });

      it('should not emit select month event', async () => {
        const data = {
          date: dayjs(),
          days: 0,
          month: 'February',
          progress: null,
          time: '00:01:39',
          year: 2021,
        };

        const selectMonthEventEmitSpy = spyOn(
          component.selectMonthEvent,
          'emit',
        );

        expect(component.selectMonth(data)).toBeFalsy();
        expect(selectMonthEventEmitSpy).not.toHaveBeenCalled();
      });
    });
  });

  describe('Display data', () => {
    it('should show loading bar', () => {
      component.isLoading = true;
      component.selectedMonth = dayjs();

      fixture.detectChanges();

      const matProgressBarElem =
        hostElement.querySelector<HTMLElement>('mat-progress-bar');

      expect(matProgressBarElem).toBeTruthy();
    });

    it('should show data', async () => {
      component.isLoading = false;
      component.selectedMonth = dayjs();
      component.data = await new AdminApiServiceFake({ responseDelayInMs: 100 })
        .getUserYearlyStatistics(
          1,
          component.selectedMonth().startOf('year'),
          component.selectedMonth().endOf('year'),
        )
        .toPromise();

      fixture.detectChanges();

      const appMonthTimeTrackItemElem =
        hostElement.querySelectorAll<HTMLElement>('app-month-time-track-item');

      expect(appMonthTimeTrackItemElem.length).toBeGreaterThan(0);
    });

    it('should show empty data message', async () => {
      component.isLoading = false;
      component.selectedMonth = dayjs();
      component.data = await new AdminApiServiceFake({
        responseDelayInMs: 100,
        isUserYearlyStatisticsEmptyData: true,
      })
        .getUserYearlyStatistics(
          1,
          component.selectedMonth().startOf('year'),
          component.selectedMonth().endOf('year'),
        )
        .toPromise();

      fixture.detectChanges();

      const emptyDataElem =
        hostElement.querySelector<HTMLElement>('.empty-data');

      expect(emptyDataElem).toBeTruthy();
    });
  });

  describe('Method isAllowNextYear', () => {
    it('should return true', () => {
      component.selectedMonth = dayjs().subtract(1, 'year');

      expect(component.isAllowNextYear()).toBeTrue();
    });

    it('should return false', () => {
      component.selectedMonth = dayjs();

      expect(component.isAllowNextYear()).toBeFalse();
    });
  });

  describe('Method isSelectedMonth', () => {
    it('should return true', async () => {
      component.selectedMonth = dayjs();
      const data = {
        date: dayjs(),
        days: 2,
        month: 'February',
        progress: null,
        time: '00:01:39',
        year: 2021,
      };

      expect(component.isSelectedMonth(data)).toBeTrue();
    });

    it('should return false', () => {
      component.selectedMonth = dayjs();
      const monthsData = {
        date: dayjs(),
        days: 0,
        month: 'February',
        progress: null,
        time: '00:01:39',
        year: 2021,
      };

      expect(component.isSelectedMonth(monthsData)).toBeFalse();
    });
  });
});
