package com.epam.brn.csv.converter

interface EntityPersister<Entity> {
    fun persistEntity(entity: Entity)
}
