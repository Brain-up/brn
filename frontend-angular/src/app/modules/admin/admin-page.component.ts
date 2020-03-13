import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LOAD_FILE_PATH, LOAD_TASKS_FILE} from '../shared/app-path';
import {animate, group, query, style, transition, trigger} from '@angular/animations';

export const slideInAnimation =
  trigger('routeAnimations', [
    transition('LoadAll => *', [
      query(':enter, :leave',
        style({position: 'fixed', width: '100%'}),
        {optional: true}),
      group([
        query(':enter', [
          style({transform: 'translateX(-100%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(0%)'}))
        ], {optional: true}),
        query(':leave', [
          style({transform: 'translateX(0%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(100%)'}))
        ], {optional: true}),
      ])
    ]),
    transition('Admin => *', [
      query(':enter, :leave',
        style({position: 'fixed', width: '100%'}),
        {optional: true}),
      group([
        query(':enter', [
          style({transform: 'translateX(100%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(0%)'}))
        ], {optional: true}),
        query(':leave', [
          style({transform: 'translateX(0%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(-100%)'}))
        ], {optional: true}),
      ])
    ]),
    transition('LoadAll => LoadTasks', [
      query(':enter, :leave',
        style({position: 'fixed', width: '100%'}),
        {optional: true}),
      group([
        query(':enter', [
          style({transform: 'translateX(100%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(0%)'}))
        ], {optional: true}),
        query(':leave', [
          style({transform: 'translateX(0%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(-100%)'}))
        ], {optional: true}),
      ])
    ]),
    transition('LoadTasks => LoadAll', [
      query(':enter, :leave',
        style({position: 'fixed', width: '100%'}),
        {optional: true}),
      group([
        query(':enter', [
          style({transform: 'translateX(-100%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(0%)'}))
        ], {optional: true}),
        query(':leave', [
          style({transform: 'translateX(0%)'}),
          animate('0.5s ease-in-out',
            style({transform: 'translateX(100%)'}))
        ], {optional: true}),
      ])
    ]),
  ]);

@Component({
  selector: 'app-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss'],
  animations: [slideInAnimation],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPageComponent implements OnInit {
  opened = false;
  constructor(private router: Router) {
  }

  ngOnInit() {

  }

  navigate(path: 'file' | 'tasks') {
    this.router.navigate([path === 'file' ? LOAD_FILE_PATH : LOAD_TASKS_FILE]);
  }


}
