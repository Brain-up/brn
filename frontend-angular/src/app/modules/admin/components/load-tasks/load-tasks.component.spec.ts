import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LoadTasksComponent } from './load-tasks.component';

describe('LoadTasksComponent', () => {
  let component: LoadTasksComponent;
  let fixture: ComponentFixture<LoadTasksComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
        declarations: [LoadTasksComponent]
      })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
