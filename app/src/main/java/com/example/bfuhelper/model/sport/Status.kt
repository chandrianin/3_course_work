package com.example.bfuhelper.model.sport

enum class Status : EnumsText {
    Absence {
        override fun text(): String {
            return "Прогул"
        }
    },
    Visit {
        override fun text(): String {
            return "Присутствовал"
        }
    },
    Disease {
        override fun text(): String {
            return "Отсутствовал по болезни"
        }

    },
    Future {
        override fun text(): String {
            return "Будущее занятие"
        }
    }
}