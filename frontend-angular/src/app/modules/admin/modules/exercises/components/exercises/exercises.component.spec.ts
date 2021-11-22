import { Exercise, Task } from '@admin/models/exercise';
import { MatTableDataSource } from '@angular/material/table';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ExercisesComponent } from './exercises.component';

describe('ExercisesComponent', () => {
  let component: ExercisesComponent;
  let adminApiServiceMock;
  let changeDetectorRefMock;

  beforeEach(() => {
    adminApiServiceMock = jasmine.createSpyObj('AdminApiService', [
      'getExercisesBySubGroupId',
    ]);
    changeDetectorRefMock = jasmine.createSpyObj('ChangeDetectorRef', [
      'detectChanges',
    ]);

    component = new ExercisesComponent(
      adminApiServiceMock,
      changeDetectorRefMock,
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('should call ngoninit', () => {
    it('should set data source with displayedColumns', () => {
      const array = [
        'id',
        'seriesId',
        'name',
        'level',
        'noise',
        'noiseSound',
        'tasks',
        'available',
      ];
      component.ngOnInit();
      expect(component.displayedColumns).toEqual(array);
    });

    it('should not set initial exercises if an id is missing', () => {
      component.seriesName$ = new BehaviorSubject<string>('1234');
      component[`groupId$`] = new BehaviorSubject<string>('1234');
      component[`subGroupId$`] = new BehaviorSubject<number>(undefined);
      component.ngOnInit();
      expect(component.showExercises).toEqual(false);
    });

    it('should set initial exercises', () => {
      component.seriesName$ = new BehaviorSubject<string>('1234');
      component[`groupId$`] = new BehaviorSubject<string>('1234');
      component[`subGroupId$`] = new BehaviorSubject<number>(1234);
      component.ngOnInit();
      expect(component.showExercises).toEqual(true);
    });
  });

  it('should not set task matrix of no tasks', () => {
    const tasks: Task[] = [];
    expect(component.getMatrixFromTasks(tasks)).toEqual('');
  });

  it('should set task matrix', () => {
    const tasks: Task[] = [
      {
        id: 1234,
        level: 1234,
        exerciseType: 'type',
        name: 'name',
        serialNumber: 1234,
        answerOptions: [],
      },
    ];
    expect(component.getMatrixFromTasks(tasks)).toEqual('\n');
  });

  it('should set the excercises hidden', () => {
    component.showExercises = true;
    component[`hideExercisesTable`]();
    expect(component.showExercises).toEqual(false);
  });

  it('should unsubscribe when destoryed', () => {
    component[`subscription`] = new Subscription();
    const spyDestroy = spyOn(Subscription.prototype, 'unsubscribe');
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });

  it('should change group', () => {
    const nextSpy = spyOn(component[`groupId$`], 'next');
    component.onGroupChange('groupId');
    expect(nextSpy).toHaveBeenCalled();
  });

  it('should change series', () => {
    const nextSpy = spyOn(component[`seriesName$`], 'next');
    component.onSeriesChange('seriesName');
    expect(nextSpy).toHaveBeenCalled();
  });

  it('should change subgroup', () => {
    const nextSpy = spyOn(component[`subGroupId$`], 'next');
    component.onSubGroupChange(1234);
    expect(nextSpy).toHaveBeenCalled();
  });

  it('should log enable changed', () => {
    const consoleSpy = spyOn(window.console, 'log');
    component.isEnableChanged('exercise', true);
    expect(consoleSpy).toHaveBeenCalled();
  });
});
