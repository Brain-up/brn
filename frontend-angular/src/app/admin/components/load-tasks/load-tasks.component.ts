import {Component, OnInit} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {Group, Series} from '../../model/model';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {map, take, tap, withLatestFrom} from 'rxjs/operators';

@Component({
  selector: 'app-load-tasks',
  templateUrl: './load-tasks.component.html',
  styleUrls: ['./load-tasks.component.scss']
})
export class LoadTasksComponent implements OnInit {
  groups$: Observable<Group[]>;
  tasksGroup: FormGroup;
  private allSeries: BehaviorSubject<Series> = new BehaviorSubject<Series>(null);
  series$: Observable<Series>;

  constructor(private adminAPI: AdminService,
              private fb: FormBuilder) {
  }

  ngOnInit() {
    this.tasksGroup = this.fb.group({
      group: ['', Validators.required],
      serie: ['', Validators.required]
    });
    this.groups$ = this.adminAPI.getGroups();
    this.adminAPI.getSeries().pipe(
      tap(series => this.allSeries.next(series)),
      take(1)
    )
      .subscribe();
    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
      withLatestFrom(this.allSeries),
      map(([group, series]) => series.group === group.id ? series : null),
    );
  }

}
