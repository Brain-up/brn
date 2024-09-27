import { fakeAsync, tick } from '@angular/core/testing';
import { Exercise, Task } from '@admin/models/exercise';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { ExercisesComponent } from './exercises.component';
import { values } from 'fp-ts/lib/Map';

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

    it('should not set initial exercises if an id is missing', fakeAsync(() => {
      component.showExercises = true;
      component.seriesName$.next('1234');
      component[`groupId$`].next('1234');
      component[`subGroupId$`].next(undefined);
      component.ngOnInit();
      tick();
      expect(component.showExercises).toEqual(true);
    }));
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
        answerOptions: [
          {
            id: 154,
            audioFileUrl: '/audio/ru-ru/filipp/1/2.ogg',
            word: 'test1',
            wordType: 'AUDIOMETRY_WORD',
            pictureFileUrl: '',
            soundsCount: 0,
          },
          {
            id: 150,
            audioFileUrl: '/audio/ru-ru/filipp/1/5.ogg',
            word: 'test2',
            wordType: 'AUDIOMETRY_WORD',
            pictureFileUrl: '',
            soundsCount: 0,
          },
        ],
      },
    ];
    expect(component.getMatrixFromTasks(tasks)).toEqual('test1 test2' + '\n');
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
