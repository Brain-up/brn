const STORAGE_KEY_PREFIX = 'brn:audiometry-history';
const MAX_ENTRIES = 50;

function storageKey(userId?: string): string {
  return userId ? `${STORAGE_KEY_PREFIX}:${userId}` : STORAGE_KEY_PREFIX;
}

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

export function loadHistory(userId?: string): AudiometryHistoryEntry[] {
  try {
    const raw = localStorage.getItem(storageKey(userId));
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed;
  } catch {
    return [];
  }
}

export function saveHistoryEntry(entry: AudiometryHistoryEntry, userId?: string): void {
  const history = loadHistory(userId);
  history.unshift(entry);
  if (history.length > MAX_ENTRIES) {
    history.length = MAX_ENTRIES;
  }
  try {
    localStorage.setItem(storageKey(userId), JSON.stringify(history));
  } catch {
    // localStorage quota exceeded — silently ignore
  }
}

export function clearHistory(userId?: string): void {
  localStorage.removeItem(storageKey(userId));
}

export function generateEntryId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
