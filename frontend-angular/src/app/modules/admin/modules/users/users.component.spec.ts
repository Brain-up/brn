import { RouterTestingModule } from '@angular/router/testing';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { User } from '@root/models/auth-token';
import { DataShareService } from '@shared/services/data-share.service';
import { Subject } from 'rxjs';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  const usersNumber = 5;
  const responseDelayInMs = 0;
  const tickInMs = responseDelayInMs + 100;
  let mockRouter = {
    navigate: jasmine.createSpy('navigate')
  } 

  const fakeActivatedRoute = {
    snapshot: { data: {} },
  } as ActivatedRoute;

  let fixture: ComponentFixture<UsersComponent>;
  let component: UsersComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UsersComponent],
      imports: [TranslateModule.forRoot(), RouterTestingModule],
      providers: [
        {
          provide: AdminApiService,
          useFactory: () =>
            new AdminApiServiceFake({ responseDelayInMs, usersNumber }),
        },
        { provide: ActivatedRoute, useValue: fakeActivatedRoute },
        { provide: DataShareService },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
  });

  it('should load user data in ngOnInit', fakeAsync(() => {
    component.ngOnInit();

    tick(tickInMs);

    expect(component.userList.length).not.toBe(undefined);
  }));

  it('unsubscribes when destoryed', () => {
    component[`destroyer`] = new Subject();
    const spyDestroy = spyOn(Subject.prototype, 'next');
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });
});
