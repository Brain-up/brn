import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotFoundComponent } from './not-found.component';

describe('NotFoundComponent', () => {
  let fixture: ComponentFixture<AdminComponent>;
  let component: NotFoundComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NotFoundComponent],
    });

    fixture = TestBed.createComponent(NotFoundComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
