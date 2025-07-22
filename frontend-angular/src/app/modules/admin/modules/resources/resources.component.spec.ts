import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourcesComponent } from './resources.component';
import { RouterTestingModule } from '@angular/router/testing';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourcesApiService } from '@admin/services/resources.api.service';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

@Component({
    template: '',
    
})
class DummyComponent {
}

describe('ResourcesComponent', () => {
  let component: ResourcesComponent;
  let fixture: ComponentFixture<ResourcesComponent>;

  const mockedRoutes =
    {
      path: 'resources',
      component: DummyComponent,
    };
  const mockContributor = [
      {
        columnNumber: 1,
        description: " Test",
        audioFileUrl: "HTTP/",
        soundsCount: 1,
        word: "Hello World",
        wordType: "Test",
        wordPronounce: "Great",
        id: 1,
        pictureFileUrl: "HTTP/"
    },
  ];


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [ NoopAnimationsModule,
              TranslateModule.forRoot(),
              RouterTestingModule.withRoutes([mockedRoutes]),
               ResourcesComponent
              ],
        providers: [
            {
                provide: ActivatedRoute,
                useValue: {}
            },
            {
              provide: ResourcesApiService,
              useValue: {
                getContributors: () => of(mockContributor),
              },
            }
        ]
    })
    .compileComponents();
  });

    beforeEach(() => {
      fixture = TestBed.createComponent(ResourcesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it("should get contributors from server", () => {
    expect(component.resourcesList).toEqual(mockContributor);
  });

});

