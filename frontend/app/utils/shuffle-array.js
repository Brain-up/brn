import deepEqual from 'brn/utils/deep-equal';

export default function shuffleArray(a) {
  const shuffled = [...a];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return deepEqual(shuffled, a) ? shuffleArray(a) : shuffled;
}
