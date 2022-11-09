import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Contributor, contributorTypes } from '@admin/models/contrubutor.model';
import { ActivatedRoute, Router } from '@angular/router';
import { ContributorApiService } from '@admin/services/api/contributor-api.service';

@Component({
  selector: 'app-contributor',
  templateUrl: './contributor.component.html',
  styleUrls: ['./contributor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContributorComponent implements OnInit {
  public contributorForm: FormGroup;
  public contributor;
  public contributorTypes = contributorTypes;

  constructor(
    private activatedRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private router: Router,
    private contributorApiService: ContributorApiService
  ) {
  }

  ngOnInit(): void {
    this.createForm();
    if (history.state.data) {
      this.convertContactsForForm();
      this.fillForm(this.contributor);
    }
  }

  createForm(): void {
    this.contributorForm = this.formBuilder.group({
        company: [''],
        companyEn: [''],
        contribution: [0, Validators.min(1)],
        contacts: this.formBuilder.group({
          phone: [''],
          email: [''],
          telegram: ['']
        }),
        description: ['', [Validators.required, Validators.maxLength(512)]],
        descriptionEn: ['', [Validators.required, Validators.maxLength(512)]],
        id: null,
        pictureUrl: [''],
        name: ['', [Validators.required, Validators.maxLength(255)]],
        nameEn: ['', [Validators.required, Validators.maxLength(255)]],
        type: 'DEVELOPER',
        active: [true],
        github_user_id: [''],
      }
    );
  }

  public fillForm(contributor: Contributor): void {
    this.contributorForm.patchValue(contributor);
  }

  public get pictureUrl(): string {
    return this.contributorForm.get('pictureUrl').value;
  }

  public get id(): string {
    return this.contributorForm.get('id').value;
  }

  public get name(): AbstractControl {
    return this.contributorForm.get('name');
  }

  public get nameEn(): AbstractControl {
    return this.contributorForm.get('name');
  }

  public get description(): AbstractControl {
    return this.contributorForm.get('description');
  }

  public get descriptionEn(): AbstractControl {
    return this.contributorForm.get('descriptionEn');
  }

  public get contribution(): AbstractControl {
    return this.contributorForm.get('contribution');
  }

  getErrorMessage(value: ValidationErrors): string {
    switch (true) {
      case value && value.required:
        return `This field can't be blank`;
      case value && Boolean(value.maxlength):
        return `Max length ${value.maxlength.requiredLength}`;
      case value && Boolean(value.min):
        return 'Should be positive';
      default:
        return '';
    }
  }

  public convertContactsForForm(): void {
    this.contributor = history.state.data;
    if (this.contributor.contacts.length) {
      const convertContactsForForm = {};
      this.contributor.contacts.forEach(item => {
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
            value
          }
        );
      }
    });
    this.contributor.contacts = convertContactsForSave;
  }

  public saveContributor(): void {
    if (this.contributorForm.pristine) {
      this.cancelInput();
    }
    if (this.contributorForm.valid) {
      this.convertFormValueForSave();
      this.id ?
        this.contributorApiService.updateContributor(this.id, this.contributor).subscribe(res => res)
        : this.contributorApiService.createContributor(this.contributor).subscribe(res => res);
      this.router.navigate(['contributors']);
    }
  }

  public cancelInput() {
    this.router.navigate(['contributors']);
  }

  // will be implemented in the next story
  public chooseFoto() {
    console.log('foto should be chosen');
  }
}
