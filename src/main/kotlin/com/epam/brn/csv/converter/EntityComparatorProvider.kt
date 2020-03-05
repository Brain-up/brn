package com.epam.brn.csv.converter

interface EntityComparatorProvider<Entity> {
    fun entityComparator(): (Entity) -> Int
}
