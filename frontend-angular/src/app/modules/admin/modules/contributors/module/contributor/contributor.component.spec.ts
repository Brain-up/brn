import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ContributorComponent } from './contributor.component';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@Component({
    template: '',
    standalone: false
})
class DummyComponent {
}

describe('ContributorComponent', () => {
  let component: ContributorComponent;
  let fixture: ComponentFixture<ContributorComponent>;

  const mockedRoutes =
    {
      path: 'contributors',
      component: DummyComponent,
    };
  const mockContributor = [
    {
      id: 1,
      name: 'Пётр',
      nameEn: 'Petr',
      description: 'Разработчик',
      descriptionEn: 'FE',
      company: 'Эпам',
      companyEn: 'EPAM',
      pictureUrl: 'HTTP/',
      contacts: [
        {
          type: 'phone',
          value: '1234567'
        }
      ],
      type: 'DEVELOPER',
      contribution: 100,
      active: true,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    schemas: [NO_ERRORS_SCHEMA],
    imports: [TranslateModule.forRoot(),
        RouterTestingModule.withRoutes([mockedRoutes]),
        ReactiveFormsModule, ContributorComponent],
    providers: [
        {
            provide: ActivatedRoute,
            useValue: {}
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
    ]
})
      .compileComponents();
  });

  beforeEach(() => {
    window.history.replaceState({ data: ''}, 'title', '' );
    fixture = TestBed.createComponent(ContributorComponent);
    component = fixture.componentInstance;
    component.contributor = mockContributor;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
