import { AdminApiService } from '@admin/services/api/admin-api.service';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SwaggerComponent } from './swagger.component';
import SpyObj = jasmine.SpyObj;

describe('SwaggerComponent', () => {
  let component: SwaggerComponent;
  let fixture: ComponentFixture<SwaggerComponent>;

  let mockAdminApiService: SpyObj<any>;
  const data = {
    swagger: '2.0',
    info: {
      description: 'REST API for brn',
      title: 'Brain up project',
    },
  };

  beforeEach(async () => {
    mockAdminApiService = jasmine.createSpyObj('AdminApiService', [
      'getSwaggerUi',
    ]);
    mockAdminApiService.getSwaggerUi.and.returnValue(of(data));

    TestBed.configureTestingModule({
    imports: [SwaggerComponent],
    providers: [
        { provide: AdminApiService, useValue: { mockAdminApiService } },
    ],
}).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SwaggerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // it('should create', () => {
  //   expect(component).toBeTruthy();
  // });
});
