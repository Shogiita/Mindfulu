package com.example.mindfulu

class MockDB {
    companion object {
        val users = mutableListOf(
            User("woriorich", "richard", "richard17@gmail.com","rich1234"),
            User("shogiita", "billie", "billie10@gmail.com","bill1234"),
            User("jopians", "jovian", "jovian01@gmail.com","jopi1234"),
            User("EDStong", "edward", "edward13@gmail.com","tong1234")
        )

        fun addUser(u:User){
            users.add(u)
        }
    }
}