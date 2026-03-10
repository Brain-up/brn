const STORAGE_KEY = 'brn:audiometry-history';
const MAX_ENTRIES = 50;

export interface AudiometryHistoryEntry {
  id: string;
  date: string;
  testId: string;
  testName: string;
  audiometryType: string;
  headphoneId: string;
  executionSeconds: number;
  leftEarThresholds: Record<number, number>;
  rightEarThresholds: Record<number, number>;
  ptaLeft: number | null;
  ptaRight: number | null;
  classificationLeft: string | null;
  classificationRight: string | null;
  speechResults?: { correct: number; total: number };
}

export function loadHistory(): AudiometryHistoryEntry[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed;
  } catch {
    return [];
  }
}

export function saveHistoryEntry(entry: AudiometryHistoryEntry): void {
  const history = loadHistory();
  history.unshift(entry);
  if (history.length > MAX_ENTRIES) {
    history.length = MAX_ENTRIES;
  }
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
  } catch {
    // localStorage quota exceeded — silently ignore
  }
}

export function clearHistory(): void {
  localStorage.removeItem(STORAGE_KEY);
}

export function generateEntryId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
