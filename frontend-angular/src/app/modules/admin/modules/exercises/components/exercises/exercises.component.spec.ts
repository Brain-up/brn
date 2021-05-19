import { ExercisesComponent } from './exercises.component';

describe('ExercisesComponent', () => {
  let component: ExercisesComponent;
  let adminApiServiceMock;
  let changeDetectorRefMock;

  beforeEach(() => {
    adminApiServiceMock = jasmine.createSpyObj('AdminApiService', ['getExercisesBySubGroupId']);
    changeDetectorRefMock = jasmine.createSpyObj('ChangeDetectorRef', ['detectChanges']);

    component = new ExercisesComponent(adminApiServiceMock, changeDetectorRefMock);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
