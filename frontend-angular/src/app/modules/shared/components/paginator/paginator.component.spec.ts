import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { PaginatorComponent } from './paginator.component';

describe('PaginatorComponent', () => {
  let fixture: ComponentFixture<PaginatorComponent>;
  let component: PaginatorComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PaginatorComponent],
      imports: [TranslateModule.forRoot()],
    });

    fixture = TestBed.createComponent(PaginatorComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  it('should emit page number', () => {
    component.total = component.pageSize + 1;
    const defaultPageNumber = component.pageNumber;
    const selectPageEventEmitSpy = spyOn(component.selectPageEvent, 'emit');

    fixture.detectChanges();

    const nextButtonElem = hostElement.querySelector<HTMLButtonElement>('.numeric-buttons button.next');
    nextButtonElem.click();

    expect(selectPageEventEmitSpy).toHaveBeenCalledWith(defaultPageNumber + 1);
  });

  describe('Displaying numeric buttons', () => {
    it('should hide all numeric buttons', () => {
      component.total = component.pageSize - 1;

      fixture.detectChanges();

      const numericButtons = hostElement.querySelectorAll<HTMLButtonElement>('.numeric-buttons button');

      expect(numericButtons.length).toBe(0);
    });

    it('should show two numeric buttons from start', () => {
      component.total = component.pageSize + 1;

      fixture.detectChanges();

      const numericButtons = hostElement.querySelectorAll<HTMLButtonElement>('.numeric-buttons button');

      expect(numericButtons.length).toBe(2);
    });

    it('should show two numeric buttons from end', () => {
      component.total = component.pageSize * 2 + 1;
      component.pageNumber = 3;

      fixture.detectChanges();

      const numericButtons = hostElement.querySelectorAll<HTMLButtonElement>('.numeric-buttons button');

      expect(numericButtons.length).toBe(2);
    });

    it('should show all numeric buttons', () => {
      component.total = component.pageSize * 2 + 1;
      component.pageNumber = 2;

      fixture.detectChanges();

      const numericButtons = hostElement.querySelectorAll<HTMLButtonElement>('.numeric-buttons button');

      expect(numericButtons.length).toBe(3);
    });
  });

  describe('Calculation range for info section', () => {
    describe('Start range', () => {
      it('should equal zero', () => {
        component.total = 0;

        expect(component.startRange).toBe(0);
      });

      it('should equal one', () => {
        component.total = 1;

        expect(component.startRange).toBe(component.total);
      });

      it('should equal page plus one', () => {
        component.total = component.pageSize + 2;
        component.pageNumber = 2;

        expect(component.startRange).toBe((component.pageNumber - 1) * component.pageSize + 1);
      });
    });

    describe('End range', () => {
      it('should equal zero', () => {
        component.total = 0;

        expect(component.endRange).toBe(0);
      });

      it('should equal page plus page size', () => {
        component.total = component.pageSize + 1;

        expect(component.endRange).toBe(component.pageNumber * component.pageSize);
      });

      it('should equal total', () => {
        component.total = component.pageSize - 1;

        expect(component.endRange).toBe(component.total);
      });
    });
  });
});
