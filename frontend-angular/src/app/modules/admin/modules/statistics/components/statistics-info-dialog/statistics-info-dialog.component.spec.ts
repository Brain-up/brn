import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatisticsInfoDialogComponent } from './statistics-info-dialog.component';

describe('StatisticsInfoDialogComponent', () => {
  let fixture: ComponentFixture<StatisticsInfoDialogComponent>;
  let component: StatisticsInfoDialogComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatisticsInfoDialogComponent],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(StatisticsInfoDialogComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should image source of dialog content section is valid', async () => {
    const imgElem = hostElement.querySelector<HTMLImageElement>('img.statistics-info-dialog');

    const fakeImgElem = new Image();
    const loadingImg = new Promise<{ type: string }>((resolve, reject) => {
      fakeImgElem.onload = resolve;
      fakeImgElem.onerror = reject;
    });
    fakeImgElem.src = imgElem.src;

    try {
      const result = await loadingImg;
      expect(result.type).toBe('load');
    } catch (result) {
      expect(result.type).not.toBe('error');
    }
  });
});
