/* eslint-disable @typescript-eslint/no-empty-function */
export default function() {
  this.passthrough('/write-coverage');

  // These comments are here to help you get started. Feel free to delete them.

  /*
    Config (with defaults).

    Note: these only affect routes defined *after* them!
  */

  // this.urlPrefix = '';    // make this `http://localhost:8081`, for example, if your API is on a different server
  // this.namespace = '';    // make this `/api`, for example, if your API is namespaced
  // this.timing = 400;      // delay for each request, automatically set to 0 during testing

  /*
    Shorthand cheatsheet:

    this.get('/posts');
    this.post('/posts');
    this.put('/posts/:id'); // or this.patch
    this.del('/posts/:id');

    https://www.ember-cli-mirage.com/docs/route-handlers/shorthands
  */
  this.namespace = 'api'
  this.timing = 10;
  this.get('/users/current', ()=>{
    return {
      data: {
        firstName: 'First-Name',
        lastName: 'Last-Name',
        email: 'em@il'
      }
    }
  });
  this.get('/statistics/study/week', () => {});
  this.get('/statistics/study/year', () => {} );
  this.resource('groups');
  this.resource('series');
  this.resource('exercises');
  this.resource('tasks');
  this.resource('study-history');
  
}
