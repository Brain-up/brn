/**
 * Utility functions for CompletionDependent logic.
 *
 * These functions extract the computed property logic from the CompletionDependent
 * base class into standalone utilities. They take explicit parameters rather than
 * relying on `this` context and DI, making them usable with both Model instances
 * and SchemaRecord instances.
 *
 * When Group, Exercise, and Task are eventually migrated to SchemaRecord,
 * these utilities will be used in extensions or components instead of
 * being computed properties on Model classes.
 */
import arrayPreviousItems from 'brn/utils/array-previous-items';
import type TasksManagerService from 'brn/services/tasks-manager';

/**
 * A minimal interface for entities that participate in completion tracking.
 * Both Model instances and SchemaRecord instances can satisfy this.
 */
export interface CompletionEntity {
  id?: string | null;
  available?: boolean;
  children?: unknown[];
  sortChildrenBy?: string;
  isCompleted?: boolean;
  isManuallyCompleted?: boolean;
  parent?: { sortedChildren?: unknown[] } | null;
}

/**
 * Sort an entity's children by the specified key.
 */
export function sortChildren(
  children: unknown[] | null | undefined,
  sortBy = 'order',
): unknown[] | null {
  if (!children) return null;
  return Array.from(children).filter(Boolean).sort((a: any, b: any) => {
    const aVal = a[sortBy];
    const bVal = b[sortBy];
    if (aVal < bVal) return -1;
    if (aVal > bVal) return 1;
    return 0;
  });
}

/**
 * Check if an entity is completed based on whether all its children are completed,
 * or if it has been manually marked as completed.
 */
export function isEntityCompleted(
  entity: CompletionEntity,
  tasksManager: TasksManagerService,
): boolean {
  if (entity.isManuallyCompleted) {
    return true;
  }
  if (tasksManager.completedTasks.length === 0) {
    return false;
  }
  const children = entity.children;
  const validChildren = children ? Array.from(children).filter(Boolean) : [];
  return validChildren.length > 0 &&
    validChildren.every((child: any) => child.isCompleted);
}

/**
 * Get all siblings from the parent's sorted children.
 */
export function getAllSiblings(entity: CompletionEntity): unknown[] {
  return (entity.parent as any)?.sortedChildren || [];
}

/**
 * Get siblings that come before this entity.
 */
export function getPreviousSiblings(entity: unknown, allSiblings: unknown[]): unknown[] {
  return arrayPreviousItems(entity, allSiblings);
}

/**
 * Get siblings that come after this entity.
 */
export function getNextSiblings(entity: unknown, allSiblings: unknown[]): unknown[] {
  return allSiblings.slice(allSiblings.indexOf(entity) + 1);
}

/**
 * Check if this entity is the first among its siblings.
 */
export function isFirst(entity: unknown, allSiblings: unknown[]): boolean {
  return getPreviousSiblings(entity, allSiblings).length === 0;
}

/**
 * Check if an entity can be interacted with (either it's available,
 * or it's the first, or all previous siblings are completed).
 */
export function canInteract(entity: CompletionEntity): boolean {
  if (entity.available) {
    return true;
  }
  const allSiblings = getAllSiblings(entity);
  const previousSiblings = getPreviousSiblings(entity, allSiblings);
  return (
    previousSiblings.length === 0 ||
    previousSiblings.every((sibling: any) => sibling.isCompleted)
  );
}
