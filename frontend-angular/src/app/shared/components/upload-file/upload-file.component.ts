import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UploadFileComponent implements OnInit {
  @Input() disabled = false;
  @Output() filesAdded: EventEmitter<Set<File>> = new EventEmitter();
  @ViewChild('file', {static: true}) file;

  constructor() {
  }

  ngOnInit() {
  }

  onFilesAdded() {
    const filesToGo: Set<File> = new Set<File>();
    const files: { [key: string]: File } = this.file.nativeElement.files;
    for (const key in files) {
      if (!isNaN(parseInt(key, 10))) {
        filesToGo.add(files[key]);
      }
    }
    this.filesAdded.emit(filesToGo);
  }
}
