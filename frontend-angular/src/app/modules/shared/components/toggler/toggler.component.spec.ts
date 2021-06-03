import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TogglerComponent } from './toggler.component';

describe('TogglerComponent', () => {
  let fixture: ComponentFixture<TogglerComponent>;
  let component: TogglerComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TogglerComponent],
    });

    fixture = TestBed.createComponent(TogglerComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  describe('Click on element', () => {
    it('should toggle to on', () => {
      const toggleEventEmitSpy = spyOn(component.toggleEvent, 'emit');

      hostElement.click();

      expect(toggleEventEmitSpy).toHaveBeenCalledWith(true);
    });

    it('should toggle to off', () => {
      component.state = true;
      const toggleEventEmitSpy = spyOn(component.toggleEvent, 'emit');

      hostElement.click();

      expect(toggleEventEmitSpy).toHaveBeenCalledWith(false);
    });
  });
});
