import re

with open("app/src/main/java/com/example/ui/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Remove Gemini AI Card
gemini_card_pattern = r"                    // Gemini AI Card \(Icon Only with Premium Gradient\)\s*Card\(\s*modifier = Modifier\s*\.size\(48\.dp\),\s*shape = RoundedCornerShape\(24\.dp\),\s*colors = CardDefaults\.cardColors\(\s*containerColor = Color\.Transparent\s*\),\s*border = BorderStroke\(1\.dp, Color\(0xFF8B5CF6\)\.copy\(alpha = 0\.3f\)\),\s*onClick = \{\s*rootNavController\.navigate\(\"ai_chat\"\)\s*\}\s*\)\s*\{\s*Box\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.background\(\s*brush = androidx\.compose\.ui\.graphics\.Brush\.linearGradient\(\s*colors = listOf\(\s*Color\(0xFF3B82F6\), // Gemini Blue\s*Color\(0xFF8B5CF6\), // Gemini Purple\s*Color\(0xFFEC4899\)\s*// Gemini Pink\s*\)\s*\)\s*\),\s*contentAlignment = Alignment\.Center\s*\)\s*\{\s*Icon\(\s*imageVector = Icons\.Default\.AutoAwesome,\s*contentDescription = \"Gemini AI\",\s*tint = Color\.White,\s*modifier = Modifier\.size\(22\.dp\)\s*\)\s*\}\s*\}"

content = re.sub(gemini_card_pattern, "", content, flags=re.DOTALL)

# Also update the comment
content = content.replace("// Google, AI Mode & Gemini AI Buttons side-by-side", "// Google & AI Mode Buttons side-by-side")

# Remove Gemini AI from tools list
content = content.replace("        Pair(\"Gemini AI\", Icons.Filled.SmartToy),\n", "")
content = content.replace("                            \"Gemini AI\" -> rootNavController.navigate(\"ai_chat\")\n", "")

with open("app/src/main/java/com/example/ui/home/HomeScreen.kt", "w") as f:
    f.write(content)

