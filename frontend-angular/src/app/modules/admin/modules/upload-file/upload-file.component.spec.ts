import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { UploadFileComponent } from './upload-file.component';

describe('UploadFileComponent', () => {
  let fixture: ComponentFixture<UploadFileComponent>;
  let component: UploadFileComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UploadFileComponent],
      imports: [TranslateModule.forRoot()],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(UploadFileComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
