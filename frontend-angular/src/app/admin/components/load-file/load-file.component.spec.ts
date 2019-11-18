import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadFileComponent } from './load-file.component';

describe('LoadFileComponent', () => {
  let component: LoadFileComponent;
  let fixture: ComponentFixture<LoadFileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoadFileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
