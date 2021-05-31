import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchInputComponent } from './search-input.component';

describe('SearchInputComponent', () => {
  let fixture: ComponentFixture<SearchInputComponent>;
  let component: SearchInputComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SearchInputComponent],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(SearchInputComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
