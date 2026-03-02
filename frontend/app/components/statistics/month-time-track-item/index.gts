import type { TOC } from '@ember/component/template-only';
import { t } from 'ember-intl';

interface MonthTimeTrackData {
  progress: string;
  time: string;
  days: number;
  month: string;
  year: number;
}

interface Signature {
  Args: {
    isSelected: boolean;
    data: MonthTimeTrackData;
  };
  Element: HTMLDivElement;
}

const StatisticsMonthTimeTrackItem: TOC<Signature> = <template>
  <div
    class="
      {{if @isSelected "selected" ""}}
      month-time-track-item"
    data-test-month-track-item
    ...attributes
  >
    <div data-test-calendar class="calendar bg-PROGRESS-{{@data.progress}}">
      <div class="header">
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
      </div>
      <div class="content">
        <b class="time">
          {{@data.time}}
        </b>
        <br />
        <span class="days">
          {{t "profile.statistics.month_time_track.days_period" days=@data.days}}
        </span>
      </div>
    </div>
    <div class="date">
      <div class="month">
        {{@data.month}}
      </div>
      <div class="year">
        {{@data.year}}
      </div>
    </div>
  </div>
</template>;

export default StatisticsMonthTimeTrackItem;
