import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {  Observable } from 'rxjs';

import { AdminService } from '../../services/admin/admin.service';
import { Exercise } from '../../model/exercise';

@Component({
  selector: 'app-exercises',
  templateUrl: './exercises.component.html',
  styleUrls: ['./exercises.component.scss']
})
export class ExercisesComponent implements OnInit {
  exercises$: Observable<Exercise[]>;
  private readonly LOG_SOURCE = 'ExercisesComponent';

  constructor(
    private adminService: AdminService,
    private route: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    // const subGroupId = this.route.snapshot.paramMap.get('id');

    // this.exercises$ = this.adminService.getExercisesBySubGroupId(subGroupId).pipe(
    //   catchError(err => {
    //     console.error(this.LOG_SOURCE, 'An error occurred during getExercisesBySubGroupId', err);
    //     return EMPTY;
    //   }),
    //   take(1)
    // );
  }

  onGroupChange(groupId: string) {
    console.log('groupId=', groupId);
  }

  onSeriesChange(seriesId: string) {
    console.log('seriesId=', seriesId);
  }

  onSubGroupChange(subGroupId: string) {
    console.log('subGroupId=', subGroupId);
  }
}
