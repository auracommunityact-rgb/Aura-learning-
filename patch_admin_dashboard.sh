#!/bin/bash
awk '
/Row\(/ { rowCount++ }
rowCount == 4 && /Spacer\(modifier = Modifier.height\(16.dp\)\)/ {
    print "            Row("
    print "                modifier = Modifier"
    print "                    .fillMaxWidth()"
    print "                    .padding(horizontal = 16.dp),"
    print "                horizontalArrangement = Arrangement.spacedBy(8.dp)"
    print "            ) {"
    print "                CompactActionCard("
    print "                    title = \"Manage Quizzes\","
    print "                    icon = androidx.compose.material.icons.Icons.Filled.School,"
    print "                    modifier = Modifier.weight(1f)"
    print "                ) {"
    print "                    navController.navigate(\"admin_manage_quizzes\")"
    print "                }"
    print "            }"
    print "            Spacer(modifier = Modifier.height(16.dp))"
    next
}
{ print }
' app/src/main/java/com/example/ui/admin/AdminDashboardScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/admin/AdminDashboardScreen.kt
