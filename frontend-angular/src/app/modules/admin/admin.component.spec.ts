import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { AdminComponent } from './admin.component';

describe('AdminComponent', () => {
  let fixture: ComponentFixture<AdminComponent>;
  let component: AdminComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminComponent],
      providers: [{ provide: Store, useValue: {} }],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
