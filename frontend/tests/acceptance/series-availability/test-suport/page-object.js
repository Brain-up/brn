import { create, visitable } from 'ember-cli-page-object';

const page = create({
  goToSeriesPage: visitable('/groups/1/series/1'),
});

export default page;
