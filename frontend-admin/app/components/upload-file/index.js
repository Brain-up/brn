import Component from "@glimmer/component";
import { action } from "@ember/object";
import { inject as service } from "@ember/service";

export default class UploadFileComponent extends Component {
  @service("uploader") uploader;
  @action
  upload(file) {
    file.upload("/api/files", {
      fileKey: "taskFile",
      headers: this.uploader.getAuthHeaders(),
      data: {
        seriesId: this.selectedSeria.id
      }
    });
  }
}
