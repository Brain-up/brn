export interface Contacts {
  type: string;
  value: string;
}

export interface Contributor {
  active?: boolean;
  company?: string;
  companyEn?: string;
  contacts: Contacts[];
  contribution: number;
  description: string;
  descriptionEn?: string;
  id: number;
  github_user_id?: string,
  name: string;
  nameEn?: string;
  pictureUrl: string;
  type: string;
}
export const contributorTypes = ['DEVELOPER', 'SPECIALIST', 'DESIGNER', 'QA', 'OTHER'];


