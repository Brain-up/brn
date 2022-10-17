import { User, UserMapped, UserWithNoAnalytics } from './user.model';
import { Contributor } from '@admin/models/contrubutor.model';

export class GetUsers {
  data: User[] | UserWithNoAnalytics[] | UserMapped[];
  errors: string[];
  meta: string[];
}

export class GetContributors {
  data: Contributor[];
  errors: string[];
  meta: string[];
}
