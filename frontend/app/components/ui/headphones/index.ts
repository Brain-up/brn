import { action } from '@ember/object';
import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { isEmpty } from '@ember/utils';
import Store from '@ember-data/store';
import HeadphoneModel from 'brn/models/headphone';

interface IComponentArguments {
  onSubmit: (selectedHeadphones: HeadphoneModel) => void;
  onCancel: () => void;
}

export default class UiHeadphonesComponent extends Component<IComponentArguments> {
  @service('store') store!: Store;

  @tracked isLoading!: boolean;
  @tracked availableHeadphones!: HeadphoneModel[];
  @tracked selectedHeadphones!: HeadphoneModel;

  @tracked newHeadphonesName!: string;

  get hasAvailableHeadphonse(): boolean {
    return this.availableHeadphones?.length > 0;
  }

  @action async loadHeadphones(): Promise<void> {
    this.isLoading = true;
    this.availableHeadphones = (
      await this.store.findAll('headphone')
    ).toArray();
    if (this.availableHeadphones.length > 0)
      this.selectedHeadphones = this.availableHeadphones[0];
    this.isLoading = false;
  }

  @action async addHeadphones(): Promise<void> {
    const isValidHeadphones =
      !isEmpty(this.newHeadphonesName) &&
      !this.availableHeadphones.isAny('name', this.newHeadphonesName);
    if (!isValidHeadphones) return;

    const newHeadphone = this.store.createRecord('headphone', {
      name: this.newHeadphonesName,
    });
    newHeadphone.save();
    this.availableHeadphones = [...this.availableHeadphones, newHeadphone];
    this.newHeadphonesName = '';
  }

  @action selectHeadphones(selectedHeadPhones: HeadphoneModel): void {
    this.selectedHeadphones = selectedHeadPhones;
  }
}
