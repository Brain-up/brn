import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsersTableComponent } from './users-table.component';

describe('UsersTableComponent', () => {
  let fixture: ComponentFixture<UsersTableComponent>;
  let component: UsersTableComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UsersTableComponent],
    });

    fixture = TestBed.createComponent(UsersTableComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
