import { Injectable } from "@angular/core";
import { createEffect, Actions, ofType } from '@ngrx/effects';
import { uploadFile } from 'src/app/shared/ngrx/actions';
import { of } from 'rxjs';
import { tap, map } from 'rxjs/operators';

@Injectable()
export class UploadEffects {
    // uploadFile$ = createEffect(()=> this.actions$.pipe(
    //     ofType(uploadFile),
    //     tap((action)=>{
    //         console.log(action)
    //     }),
    //     map(()=>{
    //         return 
    //     })
    //     // mergeMap(()=> {
    //     //     return of();
    //     // })

    // ))
    constructor(private actions$ : Actions) {}
}