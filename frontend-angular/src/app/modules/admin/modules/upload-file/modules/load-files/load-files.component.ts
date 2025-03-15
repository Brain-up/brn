import { AdminApiService } from '@admin/services/api/admin-api.service';
import { CloudApiService } from '@admin/services/api/cloud-api.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SnackBarService } from '@root/services/snack-bar.service';
import { Observable, Subject } from 'rxjs';
import { mergeMap, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-load-files',
  templateUrl: './load-files.component.html',
  styleUrls: ['./load-files.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadFilesComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly formBuilder = inject(UntypedFormBuilder);
  private readonly snackBarService = inject(SnackBarService);
  private readonly cloudApiService = inject(CloudApiService);
  private readonly adminApiService = inject(AdminApiService);
  private readonly translateService = inject(TranslateService);

  private readonly destroyer$ = new Subject<void>();

  public folders$: Observable<string[]>;
  public uploadFileForm: UntypedFormGroup;

  ngOnInit(): void {
    this.folders$ = this.cloudApiService.getFolders();

    this.uploadFileForm = this.formBuilder.group({
      files: [],
      folder: [],
    });
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public loadFiles(): void {
    const fileName = this.uploadFileForm.value.files.name;

    this.cloudApiService
      .getUploadForm(this.uploadFileForm.value.folder + fileName)
      .pipe(
        mergeMap((data) => {
          const formData = new FormData();
          data.input.forEach((input) => {
            const key = Object.keys(input)[0];
            formData.append(key, input[key]);
          });
          formData.append('file', this.uploadFileForm.value.files);

          return this.adminApiService.sendFormData(data.action, formData);
        }),
        takeUntil(this.destroyer$)
      )
      .subscribe(() => {
        this.router.navigateByUrl('/');
        this.snackBarService.success(
          this.translateService.get(
            'Admin.Modules.UploadFile.Modules.LoadFiles.SnackBar.FileSuccessfullyUploaded [fileName]',
            fileName
          )
        );
      });
  }
}
