export function urlForImage(fileUrl) {
  return `/${fileUrl}`;
}
export function urlForAudio(fileUrl) {
  return `/audio/${fileUrl.replace('default', 'no_noise')}`;
}