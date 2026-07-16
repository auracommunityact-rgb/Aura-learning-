awk '
/suspend fun getCourses/ {
    print
    getline
    print
    getline
    print
    getline
    print "        } catch (e: Exception) {"
    print "            e.printStackTrace()"
    print "            android.util.Log.e(\"AuraRepository\", \"Error fetching courses\", e)"
    print "            emptyList()"
    print "        }"
    getline
    getline
    next
}
{ print }
' app/src/main/java/com/example/data/repository/AuraRepository.kt > temp.kt
mv temp.kt app/src/main/java/com/example/data/repository/AuraRepository.kt
