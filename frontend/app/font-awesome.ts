import { library, config } from '@fortawesome/fontawesome-svg-core';

import {
  faHeadphonesAlt,
  faBookmark,
  faRedoAlt,
  faPauseCircle,
  faImage,
  faAlignJustify,
  faPlay,
  faPlayCircle,
  faTimesCircle,
  faCheckCircle,
  faChevronLeft,
} from '@fortawesome/free-solid-svg-icons';

// Disable auto CSS injection into <head>.
// The CSS is imported in app/styles/app.css via @import for correct cascade order.
config.autoAddCss = false;

// Register all icons used throughout the app with the FA library.
// This allows components to reference icons by string name, e.g. @icon="play".
library.add(
  faHeadphonesAlt,
  faBookmark,
  faRedoAlt,
  faPauseCircle,
  faImage,
  faAlignJustify,
  faPlay,
  faPlayCircle,
  faTimesCircle,
  faCheckCircle,
  faChevronLeft,
);
