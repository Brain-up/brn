import { ChangeDetectionStrategy, Component, Input, ViewChild, forwardRef, Renderer2, ElementRef } from '@angular/core';
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
  @Input()
  public disabled = false;

  @ViewChild('file', { static: true })
  public file: ElementRef<HTMLInputElement>;

  private onChange: (change: File) => void;
  private onTouch: () => void;

  constructor(private readonly renderer: Renderer2) {}

  public writeValue(value: FileList): void {
    this.renderer.setProperty(this.file, 'files', value);
  }

  public registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  public registerOnTouched(fn: () => void): void {
    this.onTouch = fn;
  }

  public onFilesAdded(): void {
    const files: FileList = this.file.nativeElement.files;
    this.onChange(files.item(0));
  }
}
