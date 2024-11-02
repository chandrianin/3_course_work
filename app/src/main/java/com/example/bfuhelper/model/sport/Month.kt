package com.example.bfuhelper.model.sport

enum class Month : EnumsText {
    Jan {
        override fun text(): String {
            return "Январь"
        }
    },
    Febr {
        override fun text(): String {
            return "Февраль"
        }
    },
    March {
        override fun text(): String {
            return "Март"
        }
    },
    April {
        override fun text(): String {
            return "Апрель"
        }
    },
    May {
        override fun text(): String {
            return "Май"
        }
    },
    June {
        override fun text(): String {
            return "Июнь"
        }
    },
    July {
        override fun text(): String {
            return "Июль"
        }
    },
    August {
        override fun text(): String {
            return "Август"
        }
    },
    Sept {
        override fun text(): String {
            return "Сентябрь"
        }
    },
    Oct {
        override fun text(): String {
            return "Октябрь"
        }
    },
    Nov {
        override fun text(): String {
            return "Ноябрь"
        }
    },
    Dec {
        override fun text(): String {
            return "Декабрь"
        }
    }
}