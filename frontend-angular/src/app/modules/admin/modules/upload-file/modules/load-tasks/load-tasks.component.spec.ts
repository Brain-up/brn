import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadTasksComponent } from './load-tasks.component';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { GroupApiService } from '@admin/services/api/group-api.service';
import { SeriesApiService } from '@admin/services/api/series-api.service';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

describe('LoadTasksComponent', () => {
  let fixture: ComponentFixture<LoadTasksComponent>;
  let component: LoadTasksComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoadTasksComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: Router, useValue: {} },
        { provide: FormBuilder, useValue: {} },
        { provide: SnackBarService, useValue: {} },
        { provide: GroupApiService, useValue: {} },
        { provide: SeriesApiService, useValue: {} },
        { provide: AdminApiService, useValue: {} },
        { provide: TranslateService, useValue: {} },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(LoadTasksComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
