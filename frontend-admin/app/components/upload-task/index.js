import Component from "@glimmer/component";
import { action } from "@ember/object";
import { inject as service } from "@ember/service";
import { tracked } from "@glimmer/tracking";
export default class UploadTaskComponent extends Component {
  @service("uploader") uploader;
  @tracked groups = [];
  @tracked series = [];
  @tracked selectedGroup = null;
  @tracked selectedSeria = null;
  @action
  onGroupChange(group) {
    this.series = [];
    this.selectedSeria = null;
    this.selectedGroup = group;
    this.loadSeries(group.id);
  }
  @action
  onSeriaChange(seria) {
    this.selectedSeria = seria;
  }
  @action
  upload(file) {
    file.upload("/api/loadTasksFile", {
      fileKey: "taskFile",
      headers: this.uploader.getAuthHeaders(),
      data: {
        seriesId: this.selectedSeria.id
      }
    });
  }

  @action
  async loadGroups() {
    const result = await this.uploader.getGroups();
    this.groups = result.data;
  }

  @action
  async loadSeries(id) {
    const result = await this.uploader.getSeriesByGroupId(id);
    this.series = result.data;
  }
}
