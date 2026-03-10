import Component from '@glimmer/component';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import UiConfirmDialog from 'brn/components/ui/confirm-dialog';
import type IntlService from 'ember-intl/services/intl';
import type NetworkService from 'brn/services/network';
import type UserDataService from 'brn/services/user-data';
import type Router from '@ember/routing/router-service';

interface PatientDTO {
  id: string;
  name?: string;
  email?: string;
  created?: string;
  active?: boolean;
}

interface Signature {
  Args: {
    model: {
      patients: PatientDTO[];
    };
  };
}

class DoctorPatientsTemplate extends Component<Signature> {
  @service('intl') intl!: IntlService;
  @service('network') network!: NetworkService;
  @service('user-data') userData!: UserDataService;
  @service('router') router!: Router;

  @tracked showAddForm = false;
  @tracked patientId = '';
  @tracked error = '';
  @tracked patients: PatientDTO[] = [];
  @tracked initialized = false;
  @tracked patientPendingRemove: PatientDTO | null = null;

  get patientList(): PatientDTO[] {
    return this.initialized ? this.patients : this.args.model.patients;
  }

  @action
  toggleAddForm() {
    this.showAddForm = !this.showAddForm;
    this.patientId = '';
    this.error = '';
  }

  @action
  onPatientIdInput(e: Event & { target: HTMLInputElement }) {
    this.patientId = e.target.value;
    this.error = '';
  }

  @action
  async addPatient(e: Event) {
    e.preventDefault();
    const id = this.patientId.trim();
    if (!id) {
      this.error = this.intl.t('doctor.patients.id_required');
      return;
    }
    const userId = this.userData.userModel?.id;
    if (!userId) return;

    try {
      await this.network.addPatient(userId, id);
      this.showAddForm = false;
      this.patientId = '';
      this.error = '';
      await this.reloadPatients();
    } catch (error: any) {
      this.error = error.message || this.intl.t('doctor.patients.add_failed');
    }
  }

  @action
  requestRemovePatient(patient: PatientDTO) {
    this.patientPendingRemove = patient;
  }

  @action
  cancelRemovePatient() {
    this.patientPendingRemove = null;
  }

  @action
  async confirmRemovePatient() {
    const patient = this.patientPendingRemove;
    if (!patient) return;
    this.patientPendingRemove = null;
    const userId = this.userData.userModel?.id;
    if (!userId) return;

    try {
      await this.network.removePatient(userId, patient.id);
      await this.reloadPatients();
    } catch (error) {
      console.error('Failed to remove patient:', error);
    }
  }

  async reloadPatients() {
    const userId = this.userData.userModel?.id;
    if (!userId) return;
    this.patients = await this.network.getDoctorPatients(userId);
    this.initialized = true;
  }

  <template>
    {{pageTitle (t "doctor.patients.title")}}
    <div class="max-w-3xl mx-auto p-4 sm:p-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-4">{{t "doctor.patients.title"}}</h1>

      <button
        data-test-show-add-patient
        type="button"
        class="btn-press mb-4 px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
        {{on "click" this.toggleAddForm}}
      >
        + {{t "doctor.patients.add_patient"}}
      </button>

      {{#if this.showAddForm}}
        <form data-test-add-patient-form class="mb-4 p-4 bg-gray-50 border border-gray-200 rounded-lg" {{on "submit" this.addPatient}}>
          <div class="mb-3">
            <label class="block mb-1 text-sm font-medium text-gray-700" for="patient-id">
              {{t "doctor.patients.patient_id_label"}}
            </label>
            <input
              data-test-patient-id-input
              id="patient-id"
              type="text"
              value={{this.patientId}}
              class="focus:ring-indigo-500 focus:border-indigo-500 block w-full px-3 py-2 text-sm border border-gray-300 rounded-md"
              placeholder={{t "doctor.patients.patient_id_placeholder"}}
              {{on "input" this.onPatientIdInput}}
            />
          </div>
          {{#if this.error}}
            <p data-test-patient-error class="mb-2 text-xs text-red-500">{{this.error}}</p>
          {{/if}}
          <div class="flex gap-2">
            <button
              data-test-submit-patient
              type="submit"
              class="btn-press px-4 py-2 text-xs font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
            >
              {{t "doctor.patients.add_button"}}
            </button>
            <button
              type="button"
              class="btn-press px-4 py-2 text-xs font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
              {{on "click" this.toggleAddForm}}
            >
              {{t "doctor.patients.cancel"}}
            </button>
          </div>
        </form>
      {{/if}}

      {{#each this.patientList as |patient|}}
        <div data-test-patient-card class="mb-3 p-4 bg-white border border-gray-200 rounded-lg shadow-sm">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm font-semibold text-gray-800" data-test-patient-name>{{patient.name}}</p>
              {{#if patient.email}}
                <p class="text-xs text-gray-500">{{patient.email}}</p>
              {{/if}}
            </div>
            <button
              data-test-remove-patient
              type="button"
              aria-label={{t "doctor.patients.remove"}}
              class="btn-press text-sm text-red-500 hover:text-red-700 hover:bg-red-50 min-h-[44px] px-3 py-2 rounded"
              {{on "click" (fn this.requestRemovePatient patient)}}
            >
              {{t "doctor.patients.remove"}}
            </button>
          </div>
        </div>
      {{else}}
        <p class="text-sm text-gray-400">{{t "doctor.patients.empty"}}</p>
      {{/each}}

      {{#if this.patientPendingRemove}}
        <UiConfirmDialog
          @message={{t "doctor.patients.confirm_remove"}}
          @onConfirm={{this.confirmRemovePatient}}
          @onCancel={{this.cancelRemovePatient}}
          @destructive={{true}}
        />
      {{/if}}
    </div>
  </template>
}

export default RouteTemplate(DoctorPatientsTemplate);
