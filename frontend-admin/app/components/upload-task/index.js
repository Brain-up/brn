import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
export default class UploadTaskComponent extends Component {
    @service uploader;
    @action
    onGroupChange() {
        
    }
    @action
    upload(file) {
        file.upload('/api/loadTasksFile', {
            fileKey: 'taskFile',
            headers: this.uploader.getAuthHeaders()
        });
        // console.log(...arguments);
    }
}
