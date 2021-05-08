import { ExercisesComponent } from './exercises.component';
import { AdminService } from '../../services/admin/admin.service';

describe('ExercisesComponent', () => {
  let component: ExercisesComponent;
  let adminServiceMock;
  let changeDetectorRefMock;

  beforeEach(async () => {
    adminServiceMock = jasmine.createSpyObj('AdminService',
      ['getGroups', 'getSeriesByGroupId', 'getSubgroupsBySeriesId', 'getExercisesBySubGroupId']
    );
    changeDetectorRefMock = jasmine.createSpyObj('AdminService', ['detectChanges']);

    component = new ExercisesComponent(adminServiceMock, changeDetectorRefMock);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
