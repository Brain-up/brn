export interface Resources {
    columnNumber?: number;
    description?: string;
    audioFileUrl?: string;
    soundsCount: number;
    word: string;
    wordType: string;
    wordPronounce?: string;
    id: number;
    pictureFileUrl?: string;
  }
  
  export interface UploadResourcesImage {
    data: string;
    errors: Array<any>;
    meta: Array<any>;
  }

  export interface UploadContributorImage {
    data: string;
    errors: Array<any>;
    meta: Array<any>;
  }
