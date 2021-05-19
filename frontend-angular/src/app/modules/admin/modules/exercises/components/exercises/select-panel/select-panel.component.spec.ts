import { SelectPanelComponent } from './select-panel.component';

describe('SelectPanelComponent', () => {
  let component: SelectPanelComponent;
  let groupApiServiceMock;
  let seriesApiServiceMock;
  let subGroupApiServiceMock;
  let adminApiServiceMock;

  beforeEach(() => {
    groupApiServiceMock = jasmine.createSpyObj('GroupApiService', ['getGroups']);
    seriesApiServiceMock = jasmine.createSpyObj('SeriesApiService', ['getSeriesByGroupId']);
    subGroupApiServiceMock = jasmine.createSpyObj('SubGroupApiService', ['getSubgroupsBySeriesId']);
    adminApiServiceMock = jasmine.createSpyObj('AdminApiService', ['getExercisesBySubGroupId']);

    component = new SelectPanelComponent(
      groupApiServiceMock,
      seriesApiServiceMock,
      subGroupApiServiceMock,
      adminApiServiceMock
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
