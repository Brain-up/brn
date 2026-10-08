import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import AudiometryTestPlayer from 'brn/components/audiometry/test-player';
import type { Headphone } from 'brn/schemas/headphone';
import type { AudiometryTask } from 'brn/schemas/audiometry';
import type { TOC } from '@ember/component/template-only';

interface AudiometryTestData {
  id: string;
  name: string;
  description: string;
  audiometryType: string;
  audiometryTasks: AudiometryTask[];
}

interface Signature {
  Args: {
    model: {
      test: AudiometryTestData;
      headphones: Headphone[];
    };
  };
}

const tpl: TOC<Signature> = <template>
  <AudiometryTestPlayer
    @test={{@model.test}}
    @headphones={{@model.headphones}}
  />
</template>;

export default RouteTemplate(tpl);
