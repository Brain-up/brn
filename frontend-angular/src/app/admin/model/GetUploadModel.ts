export interface GetUploadModel {
    data: {
        action: string;
        input: Array<{
            policy: string
        }
            & { 'x-amz-signature': string }
            & { 'x-amz-signature': string }
            & { 'key': string }
            & { 'acl': string }
            & { 'x-amz-meta-uuid': string }
            & { 'x-amz-server-side-encryption': string }
            & { 'x-amz-credential': string }
            & { 'x-amz-algorithm': string }
            & { 'file': string }
            & { 'x-amz-date': string }>
    }
}