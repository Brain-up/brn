export function urlForImage(fileUrl: string | null | undefined): string | null {
  if (fileUrl === null) {
    return null;
  }
  if (fileUrl === undefined) {
    return null;
  }
  if (fileUrl.startsWith('http')) {
    return fileUrl;
  }
  return `/${fileUrl}`;
}
export function urlForAudio(fileUrl: string | null): string | null {
  if (fileUrl === null) {
    return null;
  }
  return fileUrl;
}
