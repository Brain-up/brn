export type Group = {
  id: string;
  name: string;
  seriesIds: string[];
};

export type Series = {
  id: string;
  name: string;
  group: string;
  exercises: string[];
};
