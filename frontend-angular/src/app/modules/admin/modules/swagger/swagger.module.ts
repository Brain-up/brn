import { SwaggerRoutingModule } from './swagger-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SwaggerComponent } from './swagger.component';

@NgModule({
    imports: [CommonModule, SwaggerRoutingModule, SwaggerComponent],
})
export class SwaggerModule {}
