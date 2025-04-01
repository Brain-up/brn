import { ChangeDetectionStrategy, Component , OnInit, inject} from '@angular/core';
import { CommonModule } from "@angular/common";
import {
  AbstractControl,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from "@angular/forms";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { BehaviorSubject } from "rxjs";
import { finalize } from "rxjs/operators";
import { ResourcesApiService } from '@admin/services/resources.api.service';
import { Resources } from '@admin/models/resources.model';

@Component({
  selector: 'app-resources',
  imports: [    
        CommonModule,
        ReactiveFormsModule,
        TranslateModule,
        MatIconModule,
        MatProgressBarModule,
        MatSlideToggleModule,
  ],
  templateUrl: './resources.component.html',
  styleUrl: './resources.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResourcesComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private formBuilder = inject(UntypedFormBuilder);
  private router = inject(Router);
  private resourcesApiService = inject(ResourcesApiService);

  public resourcesForm: UntypedFormGroup;
  public resources;
  //public contributorTypes = contributorTypes;
  public file: File;
  public showErrormessage: boolean = false;
  public readonly isLoading$ = new BehaviorSubject(false);
  public pictureUrlSubj = new BehaviorSubject("");

  ngOnInit(): void {
    this.createForm();
    if (history.state.data) {
      this.convertContactsForForm();
      this.fillForm(this.resources);
    }
  }

  createForm(): void {
    this.resourcesForm = this.formBuilder.group({
      columnNumber: null,
      description: ["", [Validators.required, Validators.maxLength(512)]],
      audioFileUrl: [""],
      soundsCount: null,
      word: [""],
      wordType: [""],
      wordPronounce: [""],
      id: null,
      pictureFileUrl: [""],

      // company: [""],
      // companyEn: [""],
      // contribution: [0, Validators.min(1)],
      // contacts: this.formBuilder.group({
      //   phone: [""],
      //   email: [""],
      //   telegram: [""],
      // }),
      // description: ["", [Validators.required, Validators.maxLength(512)]],
      // descriptionEn: ["", [Validators.required, Validators.maxLength(512)]],
      // id: null,
      // pictureUrl: [""],
      // name: ["", [Validators.required, Validators.maxLength(255)]],
      // nameEn: ["", [Validators.required, Validators.maxLength(255)]],
      // type: "DEVELOPER",
      // active: [true],
      // github_user_id: [""],
    });

  }

  public fillForm(resources: Resources): void {
    this.resourcesForm.patchValue(resources);
    this.pictureUrlSubj.next(this.pictureUrl.value);
  }

  public get pictureUrl(): AbstractControl {
    return this.resourcesForm.get("pictureUrl");
  }

  public get id(): string {
    return this.resourcesForm.get("id").value;
  }

  public get columnNumber(): AbstractControl {
    return this.resourcesForm.get("columnNumber");
  }

  public get description(): AbstractControl {
    return this.resourcesForm.get("description");
  }

  public get audioFileUrl(): AbstractControl {
    return this.resourcesForm.get("audioFileUrl");
  }

  public get soundsCount(): AbstractControl {
    return this.resourcesForm.get("soundsCount");
  }

  public get word(): AbstractControl {
    return this.resourcesForm.get("word");
  }

  
  public get wordType(): AbstractControl {
    return this.resourcesForm.get("wordType");
  }

  public get wordPronounce(): AbstractControl {
    return this.resourcesForm.get("wordPronounce");
  }

  public get pictureFileUrl(): AbstractControl {
    return this.resourcesForm.get("pictureFileUrl");
  }

  getErrorMessage(value: ValidationErrors): string {
    switch (true) {
      case value && value.required:
        return `This field can't be blank`;
      case value && Boolean(value.maxlength):
        return `Max length ${value.maxlength.requiredLength}`;
      case value && Boolean(value.min):
        return "Should be positive";
      default:
        return "";
    }
  }

  public convertContactsForForm(): void {
    this.resources = history.state.data;
    if (this.resources.contacts.length) {
      const convertContactsForForm = {};
      this.resources.contacts.forEach((item) => {
        convertContactsForForm[item.type.toLocaleLowerCase()] = item.value;
      });
      this.resources.contacts = convertContactsForForm;
    }
  }

  public convertFormValueForSave(): void {
    const convertContactsForSave = [];
    this.resources = this.resourcesForm.getRawValue();
    Object.entries(this.resources.contacts).forEach(([key, value]) => {
      if (value) {
        convertContactsForSave.push({
          type: key.toUpperCase(),
          value,
        });
      }
    });
    this.resources.contacts = convertContactsForSave;
  }

  public saveResources(): void {
    if (this.resourcesForm.pristine) {
      this.cancelInput();
    }
    if (this.resourcesForm.valid) {
      this.isLoading$.next(true);
      this.convertFormValueForSave();
      if (this.id) {
        this.resourcesApiService
          .updateResources(this.id, this.resources)
          .pipe(finalize(() => this.isLoading$.next(false)))
          .subscribe(() => this.router.navigate(["resources"]));
      } else {
        this.resourcesApiService
          .createResources(this.resources)
          .pipe(finalize(() => this.isLoading$.next(false)))
          .subscribe(() => this.router.navigate(["resources"]));
      }
    }
  }

  public cancelInput(): void {
    this.router.navigate(["resources"]);
  }

  public uploadImage(event): void {
    this.file = event.target.files[0];
    /*if (!this.nameEn.value) {
      this.showErrormessage = true;
      return;
    }
     if (this.file) {
      const formatData = new FormData();
      formatData.append("file", this.file);
      formatData.append(
        "fileName",
        this.nameEn.value.trim().split(" ").join("_")
      );
      this.resourcesApiService
        .uploadContributorImage(formatData)
        .subscribe((resp) => {
          this.pictureUrl.setValue(resp.data);
          this.pictureUrlSubj.next(resp.data);
          this.showErrormessage = false;
        });
    } */
  }

}
