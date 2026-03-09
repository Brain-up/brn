import { module, test } from 'qunit';
import { visit, click, currentURL, settled } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import { setupMSW } from '../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | group navigation flow', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  hooks.beforeEach(async function () {
    await authenticateSession();

    const groups = [
      { order: 1, id: '1', locale: 'ru-ru', name: 'Non-Speech', description: 'Non-speech exercises', series: ['1', '3'] },
      { order: 2, id: '2', locale: 'ru-ru', name: 'Speech', description: 'Speech exercises', series: ['2'] },
    ];

    const series = [
      {
        id: '1',
        name: 'Frequency',
        group: 1,
        type: 'SINGLE_SIMPLE_WORDS',
        level: 1,
        description: 'Frequency exercises',
        active: true,
        subGroups: ['1', '2'],
      },
      {
        id: '2',
        name: 'Words',
        group: 2,
        type: 'SINGLE_SIMPLE_WORDS',
        level: 1,
        description: 'Word exercises',
        active: true,
        subGroups: ['3'],
      },
      {
        id: '3',
        name: 'Duration',
        group: 1,
        type: 'SINGLE_SIMPLE_WORDS',
        level: 2,
        description: 'Duration exercises',
        active: true,
        subGroups: [],
      },
    ];

    const subgroups = [
      {
        seriesId: '1',
        id: '1',
        level: 1,
        name: 'Family',
        pictureUrl: 'pictures/theme/family.svg',
        description: 'Family words',
        withPictures: false,
        exercises: [],
      },
      {
        seriesId: '1',
        id: '2',
        level: 2,
        name: 'Home',
        pictureUrl: 'pictures/theme/home.svg',
        description: 'Home words',
        withPictures: false,
        exercises: [],
      },
      {
        seriesId: '2',
        id: '3',
        level: 1,
        name: 'Animals',
        pictureUrl: 'pictures/theme/animals.svg',
        description: 'Animal words',
        withPictures: false,
        exercises: [],
      },
    ];

    server.get('groups', () => ({ data: groups }));
    server.get('groups/:id', (request) => ({
      data: groups.find((g) => g.id === request.params.id),
    }));
    server.get('series', (request) => {
      const targetGroup = request.queryParams.groupId;
      if (targetGroup) {
        const group = groups.find((g) => g.id === targetGroup);
        const seriesIds = group ? group.series : [];
        return { data: series.filter((s) => seriesIds.includes(s.id)) };
      }
      return { data: series };
    });
    server.get('series/:id', (request) => ({
      data: series.find((s) => s.id === request.params.id),
    }));
    server.get('subgroups', (request) => {
      const targetSeries = request.queryParams.seriesId;
      if (targetSeries) {
        return {
          data: subgroups.filter((sg) => sg.seriesId === targetSeries),
        };
      }
      return { data: subgroups };
    });
    server.get('exercises', () => ({ data: [] }));
    server.post('exercises/byIds', () => ({ data: [] }));
  });

  test('clicking a group from the groups page shows exercise type selection', async function (assert) {
    await visit('/groups');
    await settled();

    assert.equal(currentURL(), '/groups', 'starts on the groups page');

    // There should be group links on the page
    const groupLinks = document.querySelectorAll('a[href*="/groups/"]');
    assert.true(groupLinks.length > 0, 'group links are present on the page');

    // Click the first group link
    const firstGroupLink = groupLinks[0];
    await click(firstGroupLink);
    await settled();

    const url = currentURL();
    // Should stay on the group index page (exercise type selection), NOT auto-redirect to a series
    assert.true(
      url.match(/\/groups\/\d+$/) !== null,
      `should navigate to group index page for exercise type selection (got ${url})`,
    );

    // Should NOT be at a series page (no /series/ in the URL)
    assert.false(
      url.includes('/series/'),
      `should not auto-redirect to a series page (got ${url})`,
    );

    // Exercise type cards (series cards) should be visible
    const exerciseTypeCards = document.querySelectorAll('[data-test-exercise-type-card]');
    assert.true(
      exerciseTypeCards.length > 0,
      `exercise type cards should be visible (found ${exerciseTypeCards.length})`,
    );
  });

  test('clicking an exercise type card navigates to the series page with subgroup cards', async function (assert) {
    await visit('/groups/1');
    await settled();

    assert.true(
      currentURL().match(/\/groups\/1$/) !== null,
      `should be on group index page (got ${currentURL()})`,
    );

    // Exercise type cards should be visible
    const exerciseTypeCards = document.querySelectorAll('[data-test-exercise-type-card]');
    assert.true(
      exerciseTypeCards.length > 0,
      `exercise type cards should be visible (found ${exerciseTypeCards.length})`,
    );

    // Click the first exercise type card
    await click('[data-test-exercise-type-card]');
    await settled();

    const url = currentURL();
    // Should now be on a series page
    assert.true(
      url.match(/\/groups\/1\/series\/\d+$/) !== null,
      `should navigate to series page (got ${url})`,
    );

    // Subgroup cards should be visible (LinkTo with route="group.series.subgroup")
    const subgroupLinks = document.querySelectorAll('a[href*="/subgroup/"]');
    assert.true(
      subgroupLinks.length > 0,
      `subgroup cards should be visible on the series page (found ${subgroupLinks.length})`,
    );
  });

  test('visiting /groups/1 directly shows exercise type selection (not auto-redirect)', async function (assert) {
    await visit('/groups/1');
    await settled();

    const url = currentURL();
    // Should stay on group index, NOT redirect to a series
    assert.true(
      url.match(/\/groups\/1$/) !== null,
      `should stay on group index page (got ${url})`,
    );

    // Exercise type cards should be visible
    const exerciseTypeCards = document.querySelectorAll('[data-test-exercise-type-card]');
    assert.true(
      exerciseTypeCards.length > 0,
      `exercise type cards should be visible (found ${exerciseTypeCards.length})`,
    );
  });

  test('group with no series redirects back to groups page', async function (assert) {
    // Override the series endpoint to return empty for group 2
    server.get('series', (request) => {
      const targetGroup = request.queryParams.groupId;
      if (targetGroup === '99') {
        return { data: [] };
      }
      return { data: [] };
    });
    server.get('groups/:id', () => ({
      data: { order: 99, id: '99', locale: 'ru-ru', name: 'Empty', description: '', series: [] },
    }));

    await visit('/groups/99');
    await settled();

    assert.equal(currentURL(), '/groups', 'redirects to groups page when no series exist');
  });
});
