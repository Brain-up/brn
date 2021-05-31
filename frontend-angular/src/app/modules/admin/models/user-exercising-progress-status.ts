export type UserExercisingProgressStatusType = 'BAD' | 'GOOD' | 'GREAT';

export const USER_EXERCISING_PROGRESS_STATUS_COLOR: { [key in UserExercisingProgressStatusType]: string } = {
  BAD: '#F38698',
  GOOD: '#FBD051',
  GREAT: '#47CD8A',
};
