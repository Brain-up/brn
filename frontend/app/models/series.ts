import Model, {
  belongsTo,
  hasMany,
  attr,
  type HasMany,
} from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import Exercise from './exercise';
import Group from './group';
import { cached } from 'tracked-toolbox';
import SubgroupModel from './subgroup';
export default class Series extends Model {
  declare [Type]: 'series';
  @attr('string') name!: string;
  @attr('string') description!: string;
  @attr('number') level!: number;
  @attr('string') kind!: string;
  @belongsTo('group', { async: false, inverse: 'series' }) group?: Group | null;
  @hasMany('subgroup', { async: false, inverse: null })
  subGroups!: HasMany<SubgroupModel>;
  @hasMany('exercise', { async: false, inverse: 'series' }) exercises!: HasMany<Exercise>;
  get children() {
    return this.exercises;
  }
  get parent() {
    return this.group;
  }
  set parent(value) {
    this.group = value;
  }
  get sortedExercises() {
    return Array.from(this.exercises).sort((a, b) => a.order - b.order);
  }
  get sortedChildren() {
    return this.sortedExercises;
  }

  @cached
  get groupedByNameExercises(): Record<string, Exercise[]> {
    return Array.from(this.exercises).reduce((resultObj, currentExercise) => {
      const { name } = currentExercise;
      const targetGroup = resultObj[name];
      resultObj[name] = targetGroup
        ? targetGroup.concat([currentExercise]).sort((a, b) => a.order - b.order)
        : [currentExercise];

      return resultObj;
    }, {} as Record<string, Exercise[] | undefined>) as Record<
      string,
      Exercise[]
    >;
  }
}

