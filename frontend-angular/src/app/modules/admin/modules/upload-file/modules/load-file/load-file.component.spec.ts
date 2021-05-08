import { AdminApiService } from '@admin/services/api/admin-api.service';
import { CloudApiService } from '@admin/services/api/cloud-api.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { SnackBarService } from '@shared/services/snack-bar.service';
import { LoadFileComponent } from './load-file.component';

describe('LoadFileComponent', () => {
  let fixture: ComponentFixture<LoadFileComponent>;
  let component: LoadFileComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoadFileComponent],
      providers: [
        { provide: Router, useValue: {} },
        { provide: FormBuilder, useValue: {} },
        { provide: SnackBarService, useValue: {} },
        { provide: CloudApiService, useValue: {} },
        { provide: AdminApiService, useValue: {} },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(LoadFileComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
