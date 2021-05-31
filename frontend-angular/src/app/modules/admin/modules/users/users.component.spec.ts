import { SortType } from '@admin/models/sort';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { DEBOUNCE_TIME_IN_MS } from '@shared/constants/time-constants';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  const usersNumber = 5;
  const responseDelayInMs = 0;
  const tickInMs = responseDelayInMs + 100;

  let fixture: ComponentFixture<UsersComponent>;
  let component: UsersComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UsersComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: AdminApiService, useFactory: () => new AdminApiServiceFake({ responseDelayInMs, usersNumber }) },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
  });

  it('should load user data in ngOnInit', fakeAsync(() => {
    component.ngOnInit();

    tick(tickInMs);

    expect(component.usersData.users.length).toBe(usersNumber);
  }));

  it('check logic work of method toggleFavorite', fakeAsync(() => {
    const newIsFavoriteValue = true;
    component.toggleFavorite(newIsFavoriteValue);

    tick(tickInMs);

    expect(component.getUsersOptions.pageNumber).toBe(1);
    expect(component.getUsersOptions.isFavorite).toBe(newIsFavoriteValue);
    expect(component.usersData.users.length).toBe(usersNumber);
  }));

  it('check logic work of method sortByName', fakeAsync(() => {
    const newSortByNameValue: SortType = 'desc';
    component.sortByName(newSortByNameValue);

    tick(tickInMs);

    expect(component.getUsersOptions.pageNumber).toBe(1);
    expect(component.getUsersOptions.sortByName).toBe(newSortByNameValue);
    expect(component.usersData.users.length).toBe(usersNumber);
  }));

  it('check logic work of method selectPage', fakeAsync(() => {
    const newPageNumberValue = 2;
    component.selectPage(newPageNumberValue);

    tick(tickInMs);

    expect(component.getUsersOptions.pageNumber).toBe(newPageNumberValue);
    expect(component.usersData.users.length).toBe(usersNumber);
  }));

  it('check logic work of searchControl', fakeAsync(() => {
    component.ngOnInit();
    component.usersData = null;
    component.searchControl.setValue('dmi');

    tick(DEBOUNCE_TIME_IN_MS + tickInMs);

    expect(component.getUsersOptions.pageNumber).toBe(1);
    expect(component.usersData.users.length).toBe(usersNumber);
  }));
});
