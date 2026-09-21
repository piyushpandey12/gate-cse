package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.GateRepository
import com.example.network.AiTutorService

class GateApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        GateRepository(
            subjectDao = database.subjectDao(),
            questionDao = database.questionDao(),
            attemptDao = database.attemptDao(),
            masteryDao = database.masteryDao(),
            resourceDao = database.resourceDao(),
            revisionDao = database.revisionDao(),
            flashcardDao = database.flashcardDao(),
            formulaDao = database.formulaDao(),
            noteDao = database.noteDao(),
            studyDao = database.studyDao(),
            testDao = database.testDao(),
            userDao = database.userDao(),
            examDao = database.examDao(),
            database = database
        )
    }
    val aiTutorService by lazy { AiTutorService() }
}
