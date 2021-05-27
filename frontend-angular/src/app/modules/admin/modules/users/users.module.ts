import { AdminApiService } from '@admin/services/api/admin-api.service';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { TogglerModule } from '@shared/components/toggler/toggler.module';
import { UsersRoutingModule } from './users-routing.module';
import { UsersComponent } from './users.component';

@NgModule({
  declarations: [UsersComponent],
  imports: [UsersRoutingModule, TranslateModule, TogglerModule],
  providers: [{ provide: AdminApiService, useFactory: () => new AdminApiServiceFake() }],
})
export class UsersModule {}
