sed -i 's/emptyList()/run { e.printStackTrace(); emptyList() }/g' app/src/main/java/com/example/data/repository/AuraRepository.kt
