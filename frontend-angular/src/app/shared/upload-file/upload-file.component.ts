import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-upload-file',
  templateUrl: './upload-file.component.html',
  styleUrls: ['./upload-file.component.scss']
})
export class UploadFileComponent implements OnInit {
  @Input() disabled: boolean;

  constructor() {
  }

  ngOnInit() {
  }

}
