import { ChangeDetectionStrategy, Component, Input, ViewChild, forwardRef, Renderer2 } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UploadFileComponent),
      multi: true
    }
  ]
})
export class UploadFileComponent implements ControlValueAccessor {
  @Input() disabled = false;
  @ViewChild('file', {static: true}) file;
  private onChange: (change: File) => void;
  private onTouch: () => void;

  constructor(private renderer: Renderer2) {
  }

  writeValue(value: FileList) {
    this.renderer.setProperty(this.file, 'files', value);
  }

  registerOnChange(fn: any) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouch = fn;
  }

  onFilesAdded() {
    const files: FileList = this.file.nativeElement.files;
    this.onChange(files.item(0));
  }
}
