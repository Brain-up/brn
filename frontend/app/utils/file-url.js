export function urlForImage(fileUrl) {
  if (fileUrl === null) {
    return null;
  }
  if (fileUrl.startsWith('http')) {
    return fileUrl;
  }
  return `/${fileUrl}`;
}
export function urlForAudio(fileUrl) {
  if (fileUrl === null) {
    return null;
  }
  return fileUrl;
}
