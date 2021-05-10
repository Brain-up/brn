import {
  ChangeDetectionStrategy,
  Component,
  ViewChild,
  forwardRef,
  Renderer2,
  ElementRef,
  HostListener,
} from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-upload-file-input',
  templateUrl: './upload-file-input.component.html',
  styleUrls: ['./upload-file-input.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UploadFileInputComponent),
      multi: true,
    },
  ],
})
export class UploadFileInputComponent implements ControlValueAccessor {
  private onChange: (value: File) => void;
  private onTouched: () => void;

  @ViewChild('file', { static: true })
  private file: ElementRef<HTMLInputElement>;

  constructor(private readonly renderer: Renderer2) {}

  @HostListener('change')
  public change(): void {
    this.onChange(this.file.nativeElement.files.item(0));
  }

  @HostListener('blur')
  public blur(): void {
    this.onTouched();
  }

  public writeValue(value: FileList): void {
    this.renderer.setProperty(this.file.nativeElement, 'files', value);
  }

  public registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  public registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  public setDisabledState(value: boolean): void {
    this.renderer.setProperty(this.file.nativeElement, 'disabled', value);
  }
}
