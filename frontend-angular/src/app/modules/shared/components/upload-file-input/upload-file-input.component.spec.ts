import { Renderer2, Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UploadFileInputComponent } from './upload-file-input.component';

describe('UploadFileInputComponent', () => {
  let fixture: ComponentFixture<UploadFileInputComponent>;
  let component: UploadFileInputComponent;
  let renderer2: Renderer2;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UploadFileInputComponent],
      providers: [Renderer2],
    });

    fixture = TestBed.createComponent(UploadFileInputComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {
    renderer2 = fixture.componentRef.injector.get<Renderer2>(
      Renderer2 as Type<Renderer2>,
    );
    spyOn(renderer2, 'setProperty').and.callThrough();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should register on change', () => {
    component.registerOnChange(1234);
    expect(component[`onChange`]).toBeTruthy();
  });

  it('should register on touched', () => {
    component.registerOnTouched(1234);
    expect(component[`onTouched`]).toBeTruthy();
  });

  it('should set property of element', () => {
    const list = new DataTransfer();
    const file = new File([`content`], 'filename.jpg');
    list.items.add(file);
    component.writeValue(list.files);
    expect(renderer2.setProperty).toHaveBeenCalledWith(
      jasmine.any(Object),
      'files',
      list.files,
    );
  });

  it('should set element disabled', () => {
    const flag = false;
    component.setDisabledState(flag);
    expect(renderer2.setProperty).toHaveBeenCalledWith(
      jasmine.any(Object),
      'disabled',
      flag,
    );
  });
});
