import { TestBed } from '@angular/core/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BehaviorSubject } from 'rxjs';
import { SnackBarService } from './snack-bar.service';

describe('SnackBarService', () => {
  let service: SnackBarService;
  const mockSnackbar = jasmine.createSpyObj(['open']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MatSnackBarModule],
      providers: [{ provide: MatSnackBar, useValue: mockSnackbar }],
    });
    service = TestBed.inject(SnackBarService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call success with string and display the message for 2sec', () => {
    const message = 'Success!';
    service.success(message);
    expect(mockSnackbar.open).toHaveBeenCalled();
  });

  it('should call success with string observable and display the message for 2sec', () => {
    const message$ = new BehaviorSubject('Success!');
    service.success(message$);
    expect(mockSnackbar.open).toHaveBeenCalled();
  });

  it('should call error with string and display the message for 2sec', () => {
    const error = 'Error!';
    service.error(error);
    expect(mockSnackbar.open).toHaveBeenCalled();
  });

  it('should call error with string observable and display the message for 2sec', () => {
    const error$ = new BehaviorSubject('Error!');
    service.error(error$);
    expect(mockSnackbar.open).toHaveBeenCalled();
  });
});
