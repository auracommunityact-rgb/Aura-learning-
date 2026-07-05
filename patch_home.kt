@Composable
fun CoursesSection(navController: NavController) {
    val repository = com.example.data.repository.AuraRepository()
    val viewModel: com.example.ui.courses.CourseViewModel = viewModel(factory = com.example.ui.ViewModelFactory)
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = "Courses & Subjects", onSeeAllClick = { navController.navigate("courses") })
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseCardMini(course = course, onClick = { /* TODO */ })
                }
            }
        }
    }
}

@Composable
fun CourseCardMini(course: com.example.data.models.Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = course.thumbnailUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
