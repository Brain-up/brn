import { User, UserMapped, UserWithNoAnalytics } from './user.model';

export class GetUsers {
  data: User[] | UserWithNoAnalytics[] | UserMapped[];
  errors: string[];
  meta: string[];
}
