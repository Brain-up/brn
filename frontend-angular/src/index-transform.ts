import { TargetOptions } from '@angular-builders/custom-webpack';

export default (targetOptions: TargetOptions, indexHtml: string): string => {
  return indexHtml.replace(/\$cacheOff/g, Date.now().toString());
};
