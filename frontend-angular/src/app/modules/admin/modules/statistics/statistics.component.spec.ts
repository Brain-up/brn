import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatisticsComponent } from './statistics.component';

describe('StatisticsComponent', () => {
  let fixture: ComponentFixture<StatisticsComponent>;
  let component: StatisticsComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatisticsComponent],
    });

    fixture = TestBed.createComponent(StatisticsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
