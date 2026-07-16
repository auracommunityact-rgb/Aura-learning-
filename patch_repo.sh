awk '
/    \}\n\}/ {
    print ""
    next
}
{ print }
' app/src/main/java/com/example/data/repository/AuraRepository.kt > temp.kt
mv temp.kt app/src/main/java/com/example/data/repository/AuraRepository.kt
