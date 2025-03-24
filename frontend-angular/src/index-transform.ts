import { TargetOptions } from '@angular-builders/custom-webpack';

export default (_targetOptions: TargetOptions, indexHtml: string): string => {
  return indexHtml.replace(/\$cacheOff/g, Date.now().toString());
};
