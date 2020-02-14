import { createAction, props } from '@ngrx/store';

export const uploadFile = createAction('[Upload Component] UploadFile', props<{files: Set<File>}>());