let _cloudBaseUrl: string | null = null;

export function setCloudBaseUrl(url: string): void {
  _cloudBaseUrl = url.endsWith('/') ? url.slice(0, -1) : url;
}

export function getCloudBaseUrl(): string | null {
  return _cloudBaseUrl;
}

export function urlForImage(fileUrl: string | null | undefined): string | null {
  if (fileUrl === null || fileUrl === undefined) {
    return null;
  }
  if (fileUrl.startsWith('http')) {
    return fileUrl;
  }
  if (fileUrl.startsWith('/public/')) {
    return fileUrl;
  }
  if (fileUrl.startsWith('/') && _cloudBaseUrl) {
    return `${_cloudBaseUrl}${fileUrl}`;
  }
  if (_cloudBaseUrl) {
    return `${_cloudBaseUrl}/${fileUrl}`;
  }
  return `/${fileUrl}`;
}

export function urlForAudio(fileUrl: string | null): string | null {
  if (fileUrl === null) {
    return null;
  }
  if (fileUrl.startsWith('http')) {
    return fileUrl;
  }
  if (fileUrl.startsWith('/') && _cloudBaseUrl) {
    return `${_cloudBaseUrl}${fileUrl}`;
  }
  return fileUrl;
}
