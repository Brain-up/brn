import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LOAD_FILE_PATH, LOAD_TASKS_FILE} from '../shared/app-path';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss']
})
export class AdminPageComponent implements OnInit {
  groups$: Observable<string[]>;

  constructor(private router: Router) {
  }

  ngOnInit() {

  }

  navigate(path: 'file' | 'tasks') {
    this.router.navigate([path === 'file' ? LOAD_FILE_PATH : LOAD_TASKS_FILE]);
  }


}
