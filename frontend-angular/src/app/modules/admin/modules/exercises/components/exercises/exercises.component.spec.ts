import { ExercisesComponent } from './exercises.component';

describe('ExercisesComponent', () => {
  let component: ExercisesComponent;
  let exercisesApiServiceMock;
  let changeDetectorRefMock;

  beforeEach(async () => {
    exercisesApiServiceMock = jasmine.createSpyObj('ExercisesApiService', ['getExercisesBySubGroupId']);
    changeDetectorRefMock = jasmine.createSpyObj('AdminService', ['detectChanges']);

    component = new ExercisesComponent(exercisesApiServiceMock, changeDetectorRefMock);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
