import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {
  public readonly mainTabs = [
    { label: 'Users', link: 'users' },
    { label: 'Exercises', link: 'exercises' },
    { label: 'Resources', link: 'resources' },
    { label: 'Upload file', link: 'upload-file' },
  ];
}
