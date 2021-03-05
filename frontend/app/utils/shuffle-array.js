export default function shuffleArray(a, complexity = 1) {
  const shuffled = [...a];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  if (complexity > 1) {
    return shuffleArray(shuffled, complexity-1);
  }
  return shuffled;
}
