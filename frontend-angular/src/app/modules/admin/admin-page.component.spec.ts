import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AdminPageComponent } from './admin-page.component';
import { UploadService } from './services/upload/upload.service';

describe('AdminPageComponent', () => {
  let component: AdminPageComponent;
  let fixture: ComponentFixture<AdminPageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
        declarations: [AdminPageComponent],
        providers: [UploadService]
      })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
