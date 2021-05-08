import { SelectPanelComponent } from './select-panel.component';

describe('SelectPanelComponent', () => {
  let component: SelectPanelComponent;
  let groupApiServiceMock;
  let seriesApiServiceMock;
  let subGroupApiServiceMock;
  let exercisesApiServiceMock;

  beforeEach(() => {
    groupApiServiceMock = jasmine.createSpyObj('GroupApiService', ['getGroups']);
    seriesApiServiceMock = jasmine.createSpyObj('SeriesApiService', ['getSeriesByGroupId']);
    subGroupApiServiceMock = jasmine.createSpyObj('SubGroupApiService', ['getSubgroupsBySeriesId']);
    exercisesApiServiceMock = jasmine.createSpyObj('ExercisesApiService', ['getExercisesBySubGroupId']);

    component = new SelectPanelComponent(groupApiServiceMock, seriesApiServiceMock, subGroupApiServiceMock, exercisesApiServiceMock);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
