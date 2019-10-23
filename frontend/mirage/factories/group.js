import { Factory, trait, } from 'ember-cli-mirage';

export default Factory.extend({
  name: (i)=> `Group ${i+1}`,

  withSeries: trait({
    afterCreate(group, server) {
      server.createList('series', 8, { group, name:`${group} | series`, });
    }
  })
});
