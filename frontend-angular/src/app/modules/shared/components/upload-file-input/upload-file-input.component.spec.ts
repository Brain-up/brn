import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UploadFileInputComponent } from './upload-file-input.component';

describe('UploadFileInputComponent', () => {
  let fixture: ComponentFixture<UploadFileInputComponent>;
  let component: UploadFileInputComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UploadFileInputComponent],
    });

    fixture = TestBed.createComponent(UploadFileInputComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
