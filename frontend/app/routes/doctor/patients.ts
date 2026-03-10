import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type UserDataService from 'brn/services/user-data';

export default class DoctorPatientsRoute extends Route {
  @service('network') declare network: NetworkService;
  @service('user-data') declare userData: UserDataService;

  async model() {
    const userId = this.userData.userModel?.id;
    if (!userId) return { patients: [] };
    const patients = await this.network.getDoctorPatients(userId);
    return { patients: patients || [] };
  }
}
