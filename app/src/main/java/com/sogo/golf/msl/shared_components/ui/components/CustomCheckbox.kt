
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGunMetal

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(
            checkedColor = mslBlue, // Your custom blue color
            uncheckedColor = mslGunMetal, // Your custom gunmetal color
            checkmarkColor = Color.White // White tick
        ),
        modifier = Modifier.size(24.dp) // Adjust size as needed
    )
}

//@Composable
//@Preview(showBackground = true)
//fun LightModeCheckboxPreview() {
//    SimpleGolfTheme(darkTheme = false) {
//        Row(modifier = Modifier.padding(16.dp)) {
//            CustomCheckbox(checked = true, onCheckedChange = {})
//            Text(
//                text = "Light Mode Checkbox",
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(start = 8.dp)
//            )
//        }
//    }
//}
//
//@Composable
//@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
//fun DarkModeCheckboxPreview() {
//    SimpleGolfTheme(darkTheme = true) {
//        Row(modifier = Modifier.padding(16.dp)) {
//            CustomCheckbox(checked = true, onCheckedChange = {})
//            Text(
//                text = "Dark Mode Checkbox",
//                color = MaterialTheme.colorScheme.primary, // Explicitly use light mode's primary color
//                modifier = Modifier.padding(start = 8.dp)
//            )
//        }
//    }
//}