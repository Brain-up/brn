package com.epam.brn.csv.converter

interface Uploader<Csv, Entity> : EntityComparatorProvider<Entity>, CsvToEntityConverter<Csv, Entity>,
    ObjectReaderProvider<Csv>, EntityPersister<Entity>
