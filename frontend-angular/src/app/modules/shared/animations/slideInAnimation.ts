import { animate, group, query, style, transition, trigger } from '@angular/animations';

const transitionBody = [
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
];

export const slideInAnimation =
  trigger('routeAnimations', [
    transition('LoadAll <=> *', transitionBody),
    transition('Admin <=> *', transitionBody),
    transition('LoadAll => LoadTasks', transitionBody),
    transition('LoadTasks => LoadAll', transitionBody),
  ]);
