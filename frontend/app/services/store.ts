import { useLegacyStore } from '@warp-drive-mirror/legacy';
import { JSONAPICache } from '@warp-drive-mirror/json-api';

export default useLegacyStore({
  linksMode: false,
  legacyRequests: true,
  cache: JSONAPICache,
});
