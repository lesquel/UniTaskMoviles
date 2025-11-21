package com.example.unitask.data.repository

import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime

object SampleData {

    // Retorna un conjunto de asignaturas de ejemplo con profesores y colores.
    fun subjects(): List<Subject> = listOf(
        Subject(
            id = "subject-architecture",
            name = "Arquitectura de Software",
            colorHex = "#4285F4",
            teacher = "Ing. Torres"
        ),
        Subject(
            id = "subject-sistemas",
            name = "Sistemas Operativos",
            colorHex = "#DB4437",
            teacher = "Mtra. Gómez"
        ),
        Subject(
            id = "subject-bases",
            name = "Bases de Datos Avanzadas",
            colorHex = "#0F9D58",
            teacher = "Dr. López"
        )
    )

    // Retorna tareas de ejemplo con fechas relativas a la referencia actual.
    fun tasks(reference: LocalDateTime = LocalDateTime.now()): List<Task> {
        val base = reference.withSecond(0).withNano(0)
        val subjects = subjects()
        return listOf(
            Task(
                id = "task-arquitectura-ent1",
                title = "Arquitectura Limpia - Entrega 1",
                subjectId = subjects[0].id,
                dueDateTime = base.plusHours(18)
            ),
            Task(
                id = "task-operativos-lab",
                title = "Laboratorio de Sistemas - Informe",
                subjectId = subjects[1].id,
                dueDateTime = base.plusHours(30)
            ),
            Task(
                id = "task-bases-consulta",
                title = "Consulta SQL Avanzada",
                subjectId = subjects[2].id,
                dueDateTime = base.plusHours(52)
            ),
            Task(
                id = "task-arquitectura-lectura",
                title = "Lectura Capítulo 4",
                subjectId = subjects[0].id,
                dueDateTime = base.plusDays(3).minusHours(2)
            ),
            Task(
                id = "task-operativos-quiz",
                title = "Quiz de Planificación",
                subjectId = subjects[1].id,
                dueDateTime = base.plusDays(2)
            ),
            Task(
                id = "task-bases-proyecto",
                title = "Proyecto Final - Modelo Lógico",
                subjectId = subjects[2].id,
                dueDateTime = base.plusDays(4)
            ),
            Task(
                id = "task-arquitectura-refactor",
                title = "Refactor Sprint Actual",
                subjectId = subjects[0].id,
                dueDateTime = base.plusDays(5)
            ),
            Task(
                id = "task-operativos-exposicion",
                title = "Exposición Kernel Linux",
                subjectId = subjects[1].id,
                dueDateTime = base.plusDays(6)
            )
        )
    }
}
