import { Contributor, contributorTypes } from "@admin/models/contrubutor.model";
import { ContributorApiService } from "@admin/services/api/contributor-api.service";
import { CommonModule } from "@angular/common";
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from "@angular/core";
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

@Component({
  selector: "app-contributor",
  templateUrl: "./contributor.component.html",
  styleUrls: ["./contributor.component.scss"],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    MatIconModule,
    MatProgressBarModule,
    MatSlideToggleModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContributorComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private formBuilder = inject(UntypedFormBuilder);
  private router = inject(Router);
  private contributorApiService = inject(ContributorApiService);

  public contributorForm: UntypedFormGroup;
  public contributor;
  public contributorTypes = contributorTypes;
  public file: File;
  public showErrormessage: boolean;
  public readonly isLoading$ = new BehaviorSubject(false);
  public pictureUrlSubj = new BehaviorSubject("");

  ngOnInit(): void {
    this.createForm();
    if (history.state.data) {
      this.convertContactsForForm();
      this.fillForm(this.contributor);
    }
  }

  createForm(): void {
    this.contributorForm = this.formBuilder.group({
      company: [""],
      companyEn: [""],
      contribution: [0, Validators.min(1)],
      contacts: this.formBuilder.group({
        phone: [""],
        email: [""],
        telegram: [""],
      }),
      description: ["", [Validators.required, Validators.maxLength(512)]],
      descriptionEn: ["", [Validators.required, Validators.maxLength(512)]],
      id: null,
      pictureUrl: [""],
      name: ["", [Validators.required, Validators.maxLength(255)]],
      nameEn: ["", [Validators.required, Validators.maxLength(255)]],
      type: "DEVELOPER",
      active: [true],
      github_user_id: [""],
    });
  }

  public fillForm(contributor: Contributor): void {
    this.contributorForm.patchValue(contributor);
    this.pictureUrlSubj.next(this.pictureUrl.value);
  }

  public get pictureUrl(): AbstractControl {
    return this.contributorForm.get("pictureUrl");
  }

  public get id(): string {
    return this.contributorForm.get("id").value;
  }

  public get name(): AbstractControl {
    return this.contributorForm.get("name");
  }

  public get nameEn(): AbstractControl {
    return this.contributorForm.get("nameEn");
  }

  public get description(): AbstractControl {
    return this.contributorForm.get("description");
  }

  public get descriptionEn(): AbstractControl {
    return this.contributorForm.get("descriptionEn");
  }

  public get contribution(): AbstractControl {
    return this.contributorForm.get("contribution");
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
    this.contributor = history.state.data;
    if (this.contributor.contacts.length) {
      const convertContactsForForm = {};
      this.contributor.contacts.forEach((item) => {
        convertContactsForForm[item.type.toLocaleLowerCase()] = item.value;
      });
      this.contributor.contacts = convertContactsForForm;
    }
  }

  public convertFormValueForSave(): void {
    const convertContactsForSave = [];
    this.contributor = this.contributorForm.getRawValue();
    Object.entries(this.contributor.contacts).forEach(([key, value]) => {
      if (value) {
        convertContactsForSave.push({
          type: key.toUpperCase(),
          value,
        });
      }
    });
    this.contributor.contacts = convertContactsForSave;
  }

  public saveContributor(): void {
    if (this.contributorForm.pristine) {
      this.cancelInput();
    }
    if (this.contributorForm.valid) {
      this.isLoading$.next(true);
      this.convertFormValueForSave();
      if (this.id) {
        this.contributorApiService
          .updateContributor(this.id, this.contributor)
          .pipe(finalize(() => this.isLoading$.next(false)))
          .subscribe(() => this.router.navigate(["contributors"]));
      } else {
        this.contributorApiService
          .createContributor(this.contributor)
          .pipe(finalize(() => this.isLoading$.next(false)))
          .subscribe(() => this.router.navigate(["contributors"]));
      }
    }
  }

  public cancelInput(): void {
    this.router.navigate(["contributors"]);
  }

  public uploadImage(event): void {
    this.file = event.target.files[0];
    if (!this.nameEn.value) {
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
      this.contributorApiService
        .uploadContributorImage(formatData)
        .subscribe((resp) => {
          this.pictureUrl.setValue(resp.data);
          this.pictureUrlSubj.next(resp.data);
          this.showErrormessage = false;
        });
    }
  }
}
