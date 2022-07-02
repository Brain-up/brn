import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import StudyConfigService from 'brn/services/study-config';

export default class ExerciseStudyConfigComponent extends Component {
    @service('study-config') studyConfig!: StudyConfigService;
    
}
