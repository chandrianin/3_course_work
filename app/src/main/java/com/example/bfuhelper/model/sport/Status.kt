package com.example.bfuhelper.model.sport

enum class Status {
    Absence {
        override fun toString(): String {
            return "Отсутствовал"
        }
    },
    Visit {
        override fun toString(): String {
            return "Присутствовал"
        }
    },
    Disease {
        override fun toString(): String {
            return "Уважительная\nпричина"
        }

    },
    Future {
        override fun toString(): String {
            return "Будущее занятие"
        }
    }
}