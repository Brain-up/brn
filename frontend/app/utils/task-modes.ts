export const MODES = {
  LISTEN: 'listen',
  INTERACT: 'interact',
  TASK: 'task',
} as const;

export type Mode = typeof MODES[keyof typeof MODES];
