import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourcesComponent } from './resources.component';
import { RouterTestingModule } from '@angular/router/testing';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

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
        columnNumber: "1",
        description: " Test",
        audioFileUrl: "https://brnup.s3.eu-north-1.amazonaws.com/pictures/specialists/sivenkova.png",
        soundsCount: 1,
        word: "Hello World",
        wordType: "Test",
        wordPronounce: "Great",
        id: 1,
        pictureFileUrl: "https://brnup.s3.eu-north-1.amazonaws.com/pictures/specialists/sivenkova.png"
    },
  ];


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [TranslateModule.forRoot(),
        RouterTestingModule.withRoutes([mockedRoutes]),
        ReactiveFormsModule, ResourcesComponent],
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

    fixture = TestBed.createComponent(ResourcesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  beforeEach(() => {
    window.history.replaceState({ data: ''}, 'title', '' );
    fixture = TestBed.createComponent(ResourcesComponent);
    component = fixture.componentInstance;
    component.resources = mockContributor;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
