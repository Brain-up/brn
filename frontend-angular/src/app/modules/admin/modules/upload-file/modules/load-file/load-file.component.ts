import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { mergeMap, takeUntil } from 'rxjs/operators';
import { SnackBarService } from '@shared/services/snack-bar.service';
import { CloudApiService } from '@admin/services/api/cloud-api.service';
import { AdminApiService } from '@admin/services/api/admin-api.service';

@Component({
  selector: 'app-load-file',
  templateUrl: './load-file.component.html',
  styleUrls: ['./load-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadFileComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();

  public folders$: Observable<string[]>;
  public uploadFileForm: FormGroup;

  constructor(
    private readonly router: Router,
    private readonly formBuilder: FormBuilder,
    private readonly snackBarService: SnackBarService,
    private readonly cloudApiService: CloudApiService,
    private readonly adminApiService: AdminApiService
  ) {}

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
        this.snackBarService.showHappySnackbar(`${fileName} was successfully uploaded`);
      });
  }
}
