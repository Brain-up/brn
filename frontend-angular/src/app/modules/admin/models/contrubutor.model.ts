export interface Contacts {
  type: string;
  value: string;
}

export interface Contributor {
  company?: string;
  contacts: Contacts[];
  contribution: number;
  description: string;
  id: number;
  name: string;
  pictureUrl: string;
  type: string;
}


