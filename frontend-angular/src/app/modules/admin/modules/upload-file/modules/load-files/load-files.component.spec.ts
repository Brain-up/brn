import { Subject } from 'rxjs';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { CloudApiService } from '@admin/services/api/cloud-api.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SnackBarService } from '@root/services/snack-bar.service';
import { LoadFilesComponent } from './load-files.component';

describe('LoadFilesComponent', () => {
  let component: LoadFilesComponent;
  let fixture: ComponentFixture<LoadFilesComponent>;
  let mockCloudApiService: CloudApiService;
  const folders: string[] = ['folder1', 'folder2'];

  beforeEach(async () => {
    mockCloudApiService = jasmine.createSpyObj<CloudApiService>(
      'CloudApiService',
      {
        getUploadForm: undefined,
        getFolders: undefined,
      },
    );

    await TestBed.configureTestingModule({
      declarations: [LoadFilesComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: Router, useValue: {} },
        { provide: FormBuilder, useValue: {} },
        { provide: SnackBarService, useValue: {} },
        { provide: CloudApiService, useValue: {} },
        { provide: AdminApiService, useValue: {} },
        { provide: TranslateService, useValue: {} },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(LoadFilesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should unsubscribe when destoryed', () => {
    component[`destroyer`] = new Subject();
    const spyDestroy = spyOn(Subject.prototype, 'next');
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });
});
