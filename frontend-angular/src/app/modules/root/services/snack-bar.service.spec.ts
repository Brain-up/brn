import { TestBed } from '@angular/core/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BehaviorSubject } from 'rxjs';
import { SnackBarService } from './snack-bar.service';

describe('SnackBarService', () => {
  let service: SnackBarService;
  let matSnackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [MatSnackBarModule],
      providers: [{ provide: MatSnackBar, useValue: spy }],
    });

    service = TestBed.inject(SnackBarService);
    matSnackBarSpy = TestBed.get<MatSnackBar>(MatSnackBar);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call success with string and display the message for 2sec', () => {
    let message = 'Success!';
    service.success(message);
    expect(matSnackBarSpy.open).toHaveBeenCalled();
  });

  it('should call success with string observable and display the message for 2sec', () => {
    let message$ = new BehaviorSubject('Success!');
    service.success(message$);
    expect(matSnackBarSpy.open).toHaveBeenCalled();
  });

  it('should call error with string and display the message for 2sec', () => {
    let error = 'Error!';
    service.error(error);
    expect(matSnackBarSpy.open).toHaveBeenCalled();
  });

  it('should call error with string observable and display the message for 2sec', () => {
    let error$ = new BehaviorSubject('Error!');
    service.error(error$);
    expect(matSnackBarSpy.open).toHaveBeenCalled();
  });
});
