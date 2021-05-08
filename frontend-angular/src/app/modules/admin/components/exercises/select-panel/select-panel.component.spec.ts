import { SelectPanelComponent } from './select-panel.component';
import { AdminService } from '../../../services/admin/admin.service';

describe('SelectPanelComponent', () => {
  let component: SelectPanelComponent;
  let adminServiceMock;

  beforeEach(() => {
    adminServiceMock = jasmine.createSpyObj('AdminService',
      ['getGroups', 'getSeriesByGroupId', 'getSubgroupsBySeriesId', 'getExercisesBySubGroupId']
    );

    component = new SelectPanelComponent(adminServiceMock);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
