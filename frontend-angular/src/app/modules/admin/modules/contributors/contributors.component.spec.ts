import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ContributorsComponent } from './contributors.component';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

fdescribe('ContributorsComponent', () => {
  let component: ContributorsComponent;
  let fixture: ComponentFixture<ContributorsComponent>;
  const mockContributors = [
    {
      id: 1,
      name: 'Petr',
      description: 'FE',
      company: 'EPAM',
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
      declarations: [ ContributorsComponent ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {
          provide: AdminApiService,
          useValue: {
            getContributors: () => of(mockContributors),
          },
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContributorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it ('should get contributors from server', () => {
    expect(component.contributorsList).toEqual(mockContributors);
  });
});
