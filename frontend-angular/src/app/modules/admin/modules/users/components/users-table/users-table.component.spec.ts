import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { UsersTableComponent } from './users-table.component';

describe('UsersTableComponent', () => {
  let fixture: ComponentFixture<UsersTableComponent>;
  let component: UsersTableComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UsersTableComponent],
      imports: [TranslateModule.forRoot()],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(UsersTableComponent);
    component = fixture.componentInstance;
    hostElement = fixture.nativeElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit sort by name', () => {
    const sortByNameEventEmitSpy = spyOn(component.sortByNameEvent, 'emit');

    const sortIconElem = hostElement.querySelector<HTMLElement>(
      '.name-column-title mat-icon',
    );
    sortIconElem.click();

    expect(sortByNameEventEmitSpy).toHaveBeenCalledWith('desc');
  });

  it('should test input property data if null', () => {
    component.data = null;
    fixture.detectChanges();
    expect(component.usersTableData).toBe(undefined);
  });
});
