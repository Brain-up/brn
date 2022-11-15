import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ContributorComponent } from './contributor.component';
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  template: '',
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
      contribution: 100
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule.withRoutes([mockedRoutes]),
        HttpClientTestingModule,
        ReactiveFormsModule,
      ],
      declarations: [ContributorComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {}
        }
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
