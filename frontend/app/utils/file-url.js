export function urlForImage(fileUrl) {
  if (fileUrl === null) {
    return null;
  }
  return `/${fileUrl}`;
}
export function urlForAudio(fileUrl) {
  if (fileUrl === null) {
    return null;
  }
  return fileUrl;
}
