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
  return `/audio/${fileUrl.replace('default', 'no_noise')}`;
}