import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadTasksComponent } from './load-tasks.component';

describe('LoadTasksComponent', () => {
  let component: LoadTasksComponent;
  let fixture: ComponentFixture<LoadTasksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoadTasksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
