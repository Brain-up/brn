import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  let fixture: ComponentFixture<UsersComponent>;
  let component: UsersComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UsersComponent],
    });

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
