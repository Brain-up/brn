package com.epam.brn.service

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseSeries
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ExerciseSeriesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class LoadHandBook(
    @Autowired val groupRepository: ExerciseGroupRepository,
    @Autowired val seriesRepository: ExerciseSeriesRepository,
    @Autowired val exerciseRepository: ExerciseRepository
) {

    @PostConstruct
    fun loadData() {
        val group = ExerciseGroup(name = "речевые упражения", description = "речевые упражения")
        groupRepository.save(group)
        val series1 = ExerciseSeries(name = "распознование слов", description = "распознование слов", exerciseGroup = group)
        seriesRepository.save(series1)
        val series2 = ExerciseSeries(name = "диахоничкеское слушание", description = "диахоничкеское слушание", exerciseGroup = group)
        seriesRepository.save(series2)
    }
}