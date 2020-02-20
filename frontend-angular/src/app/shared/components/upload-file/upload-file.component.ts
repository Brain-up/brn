import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild, forwardRef, Renderer2} from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(()=> UploadFileComponent),
      multi: true
    }
  ]
})
export class UploadFileComponent implements OnInit, ControlValueAccessor {
  @Input() disabled = false;
  @Output() filesAdded: EventEmitter<Set<File>> = new EventEmitter();
  @ViewChild('file', {static: true}) file;

  ngOnInit() {
  }
  private onChange:  (change: Set<File>)=> void;
  private onTouch: ()=> void;
  writeValue(value: FileList) {
    this.renderer.setProperty(this.file, 'files', value)
  }
  registerOnChange(fn: any) {
    this.onChange = fn;
  }
  registerOnTouched(fn: ()=> void) {
    this.onTouch = fn;
  }

  onFilesAdded() {
    const filesToGo: Set<File> = new Set<File>();
    const files: { [key: string]: File } = this.file.nativeElement.files;
    for (const key in files) {
      // console.log(files);
      if (!isNaN(parseInt(key, 10))) {
        filesToGo.add(files[key]);
      }
    }
    this.onChange(filesToGo)
  }
  constructor(private renderer: Renderer2) {
  }
}
