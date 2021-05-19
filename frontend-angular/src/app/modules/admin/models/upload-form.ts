export class UploadForm {
  public action: string;
  public input: {
    file: string;
    policy: string;
    key: string;
    acl: string;
    'x-amz-signature': string;
    'x-amz-meta-uuid': string;
    'x-amz-server-side-encryption': string;
    'x-amz-credential': string;
    'x-amz-algorithm': string;
    'x-amz-date': string;
  }[];
}
