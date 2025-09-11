package com.sogo.golf.msl.features.sogo_home.presentation.components

import com.sogo.golf.msl.shared_components.ui.components.CustomCheckbox
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.msl.MslGolfer
import com.sogo.golf.msl.features.home.presentation.HomeViewModel
import com.sogo.golf.msl.features.login.presentation.CenteredYellowButton
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextFieldColors


/**
 * This class represents a Golfer data confirmation sheet.
 * It handles displaying and collecting golfer information that comes from the SimpleGolf API.
 * It will be displayed only if the value of isConfirmedMslGolferData in the "golfers" document is FALSE or missing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GolferDataConfirmationSheet(
    viewModel: HomeViewModel = hiltViewModel(),
    mslGolfer: MslGolfer, //this is from SOGO's database. however its data was populated via MSL's API
    sogoGolfer: com.sogo.golf.msl.domain.model.mongodb.SogoGolfer? = null, // Existing SOGO golfer data from Room
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current // For Toast messages

    val sogoGolferDataState by viewModel.sogoGolferDataState.collectAsState()
    val golferData = sogoGolferDataState.sogoGolfer
    val countryDataState by viewModel.countryDataState.collectAsState()

    // 🎯 INTELLIGENT FIELD POPULATION
    // Priority: SOGO golfer data > MSL golfer data
    
    var firstName by remember { 
        mutableStateOf(
            sogoGolfer?.firstName?.takeIf { it.isNotBlank() } ?: mslGolfer.firstName
        ) 
    }
    val isFirstNameError = remember { mutableStateOf(false) }
    val firstNameErrorMessage = remember { mutableStateOf("") }

    var lastName by remember { 
        mutableStateOf(
            sogoGolfer?.lastName?.takeIf { it.isNotBlank() } ?: mslGolfer.surname
        ) 
    }
    val isLastNameError = remember { mutableStateOf(false) }
    val lastNameErrorMessage = remember { mutableStateOf("") }

    var email by remember { 
        mutableStateOf(
            sogoGolfer?.email?.takeIf { it.isNotBlank() } ?: mslGolfer.email
        ) 
    }
    val isEmailError = remember { mutableStateOf(false) }
    val emailErrorMessage = remember { mutableStateOf("") }

    // Use SOGO golfer state.shortName if available, otherwise MSL golfer data
    var state by remember { 
        mutableStateOf(
            sogoGolfer?.state?.shortName?.takeIf { it.isNotBlank() } ?: mslGolfer.state
        ) 
    }

    // Use SOGO golfer postCode if available, otherwise MSL golfer data  
    var postcode by remember { 
        mutableStateOf(
            sogoGolfer?.postCode?.takeIf { it.isNotBlank() } ?: mslGolfer.postCode
        ) 
    }
    val isPostcodeError = remember { mutableStateOf(false) }
    val postcodeErrorMessage = remember { mutableStateOf("") }

    var mobile by remember { 
        mutableStateOf(
            sogoGolfer?.mobileNo?.takeIf { it.isNotBlank() } 
                ?: sogoGolfer?.phone?.takeIf { it.isNotBlank() } 
                ?: mslGolfer.mobileNo
        ) 
    }
    val isMobileError = remember { mutableStateOf(false) }
    val mobileErrorMessage = remember { mutableStateOf("") }

    // Use SOGO golfer gender if available, otherwise MSL golfer data
    // Convert SOGO gender format: "m" -> "Male", "f" -> "Female"
    var gender by remember { 
        mutableStateOf(
            when (sogoGolfer?.gender?.takeIf { it.isNotBlank() }) {
                "m" -> "Male"
                "f" -> "Female"
                else -> mslGolfer.gender
            }
        ) 
    }
    val isGenderError = remember { mutableStateOf(false) }
    val genderErrorMessage = remember { mutableStateOf("") }

    // 🎯 INTELLIGENT DATE OF BIRTH POPULATION
    // Try to parse date from sogoGolfer first, then mslGolfer
    val initialDateMillis = remember {
        // Try SOGO golfer date first (ISO format: "1990-05-15T00:00:00.000Z")
        sogoGolfer?.dateOfBirth?.let { isoDate ->
            try {
                val instant = Instant.parse(isoDate)
                instant.toEpochMilli()
            } catch (e: Exception) {
                null
            }
        } ?:
        // Fallback to MSL golfer date (format: "15/05/1990")
        mslGolfer.dateOfBirth.let { ddMmYyyy ->
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                val localDate = org.threeten.bp.LocalDate.parse(ddMmYyyy, formatter)
                localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    var dateOfBirthMillis by remember { mutableStateOf(initialDateMillis) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )
    val openDialog = remember { mutableStateOf(false) }

    val formattedDateOfBirth = remember {
        derivedStateOf {
            dateOfBirthMillis?.let {
                val instant = Instant.ofEpochMilli(it)
                val dateTime = instant.atZone(ZoneId.of("UTC"))
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                formatter.format(dateTime)
            } ?: ""
        }
    }
    val isDobError = remember { mutableStateOf(false) }
    val dobErrorMessage = remember { mutableStateOf("") }

    var isTermsChecked by remember { mutableStateOf(false) }
    var isCreateOrUpdateSogoGolfAccount by remember { mutableStateOf(true) }

    val uriHandler = LocalUriHandler.current

    // Australian states list
    val australianStates = listOf("ACT", "NSW", "NT", "QLD", "SA", "TAS", "VIC", "WA")

    // Initial validation using LaunchedEffect
    LaunchedEffect(firstName, lastName, email, postcode, mobile, gender) {
        isFirstNameError.value = firstName.isNullOrBlank()
        firstNameErrorMessage.value = if (firstName.isNullOrBlank()) "First name must not be empty" else ""

        isLastNameError.value = lastName.isNullOrBlank()
        lastNameErrorMessage.value = if (lastName.isNullOrBlank()) "Last name must not be empty" else ""

        isEmailError.value = !email.isNullOrBlank() && !viewModel.isValidEmail(email!!)
        emailErrorMessage.value = if (!email.isNullOrBlank() && !viewModel.isValidEmail(email!!)) "Invalid email" else ""

        isPostcodeError.value = !postcode.isNullOrBlank() && !viewModel.isValidAustralianPostcode(postcode!!.trim())
        postcodeErrorMessage.value = if (!postcode.isNullOrBlank() && !viewModel.isValidAustralianPostcode(postcode!!.trim())) "Invalid postcode" else ""

        isMobileError.value = !mobile.isNullOrBlank() && !viewModel.isValidMobileNumber(mobile!!)
        mobileErrorMessage.value = if (!mobile.isNullOrBlank() && !viewModel.isValidMobileNumber(mobile!!)) {
            if (mobile?.length == 10 && !mobile!!.startsWith("0"))
                "Mobile number must start with 0"
            else "Mobile number is not valid"
        } else ""

        isGenderError.value = gender.isNullOrBlank()
        genderErrorMessage.value = if (gender.isNullOrBlank()) "Gender must be selected" else ""
    }

    val isFormValid = remember(firstName, lastName, email, state, postcode, mobile, gender, isTermsChecked, isCreateOrUpdateSogoGolfAccount,
        isFirstNameError.value, isLastNameError.value, isPostcodeError.value, isMobileError.value, isGenderError.value, dateOfBirthMillis, isDobError.value
        ) {
        (firstName?.isNotBlank() == true) && !isFirstNameError.value &&
                (lastName?.isNotBlank() == true) && !isLastNameError.value &&
                (email?.isNotBlank() == true) && !isEmailError.value &&
                (state?.isNotBlank() == true) && // Check for null and blank state
                (postcode?.isNotBlank() == true) && !isPostcodeError.value &&
                (mobile?.isNotBlank() == true) && !isMobileError.value && // Check for null and blank mobile
                (gender?.isNotBlank() == true && !isGenderError.value) && // Check for null and blank gender
                isTermsChecked &&
                isCreateOrUpdateSogoGolfAccount &&
                dateOfBirthMillis?.let {
                    val dob = DateUtils.getLocalDateFromMillis(it)
                    if (!DateUtils.isAgeBetween10And110(dob)) {
                        isDobError.value = true
                        dobErrorMessage.value = "Age must be between 10 and 110 years."
                        false
                    } else {
                        isDobError.value = false
                        dobErrorMessage.value = ""
                        true
                    }
                } ?: false
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Confirm Your Details", style = MaterialTheme.typography.headlineMedium, color = mslBlue)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = firstName ?: "",
            onValueChange = { newValue ->
                firstName = newValue.trim()
                isFirstNameError.value = newValue.isBlank()
                firstNameErrorMessage.value = if (newValue.isBlank()) "First name must not be empty" else ""
            },
            maxLines = 1,
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = isFirstNameError.value, // Access the value here
            supportingText = { if (isFirstNameError.value) Text(firstNameErrorMessage.value, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)}),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = mslBlack,
                unfocusedTextColor = mslBlack,
                focusedBorderColor = mslBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = mslBlue,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName ?: "",
            onValueChange = { newValue ->
                lastName = newValue.trim()
                isLastNameError.value = newValue.isBlank()
                lastNameErrorMessage.value = if (newValue.isBlank()) "Last name must not be empty" else ""
            },
            maxLines = 1,
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = isLastNameError.value, // Access the value here
            supportingText = { if (isLastNameError.value) Text(lastNameErrorMessage.value, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)}),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = mslBlack,
                unfocusedTextColor = mslBlack,
                focusedBorderColor = mslBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = mslBlue,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email ?: "",
            onValueChange = { newValue ->
                email = newValue.trim()
                isEmailError.value = !viewModel.isValidEmail(newValue)
                emailErrorMessage.value = if (!viewModel.isValidEmail(newValue)) "Invalid email" else ""
            },
            maxLines = 1,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = isEmailError.value, // Access the value here
            supportingText = { if (isEmailError.value) Text(emailErrorMessage.value, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)}),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = mslBlack,
                unfocusedTextColor = mslBlack,
                focusedBorderColor = mslBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = mslBlue,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // State Dropdown - Fixed for Pixel 5 compatibility
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state?.uppercase() ?: "",
                onValueChange = {},
                readOnly = true,
                maxLines = 1,
                label = { Text("State") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // Simplified menuAnchor for better compatibility
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = mslBlack,
                    unfocusedTextColor = mslBlack,
                    focusedBorderColor = mslBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = mslBlue,
                    unfocusedLabelColor = Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth() // Ensure full width for better visibility
            ) {
                australianStates.forEach { stateOption ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = stateOption,
                                color = mslBlack,
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        onClick = {
                            state = stateOption
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = postcode ?: "",
            onValueChange = { newValue ->
                postcode = newValue
                val trimmed = newValue.trim()
                isPostcodeError.value = !viewModel.isValidAustralianPostcode(trimmed)
                postcodeErrorMessage.value = if (!viewModel.isValidAustralianPostcode(trimmed)) "Invalid postcode" else ""
            },
            label = { Text("Postcode") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            isError = isPostcodeError.value, // Access the value here
            supportingText = { if (isPostcodeError.value) Text(postcodeErrorMessage.value, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)}),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = mslBlack,
                unfocusedTextColor = mslBlack,
                focusedBorderColor = mslBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = mslBlue,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mobile ?: "",
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() || it.isWhitespace() }
                mobile = filteredValue // ✅ Always update the field with filtered input
                isMobileError.value = !viewModel.isValidMobileNumber(filteredValue)
                mobileErrorMessage.value = if (isMobileError.value) "Invalid mobile number" else ""
            },
            maxLines = 1,
            label = { Text("Mobile") },
            modifier = Modifier.fillMaxWidth(),
            isError = isMobileError.value, // Access the value here
            supportingText = { if (isMobileError.value) Text(mobileErrorMessage.value, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { keyboardController?.hide() } // Move focus to the next field
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = mslBlack,
                unfocusedTextColor = mslBlack,
                focusedBorderColor = mslBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = mslBlue,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date of Birth
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = formattedDateOfBirth.value,
                onValueChange = { _ -> },
                label = { Text("Date of Birth") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)}),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = if (formattedDateOfBirth.value.isNotEmpty()) mslBlack else Color.Gray,
                    disabledTextColor = if (formattedDateOfBirth.value.isNotEmpty()) mslBlack else Color.Gray,
                    disabledLabelColor = if (formattedDateOfBirth.value.isNotEmpty()) mslBlack else Color.Gray,
                    disabledLeadingIconColor = Color.Gray, // Optional: if using leading icons
                    disabledTrailingIconColor = Color.Gray // Optional: if using trailing icons
                ),
                supportingText = {
                    if (isDobError.value) {
                        Text(dobErrorMessage.value, color = Color.Red)
                    }
                }
            )

            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { openDialog.value = true }
            ) {
                Text("Select Date")
            }
        }

        if (openDialog.value) {
            DatePickerDialog(
                onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    Button(onClick = {
                        openDialog.value = false
                        dateOfBirthMillis = datePickerState.selectedDateMillis

                        // Validate Date of Birth
                        dateOfBirthMillis?.let {
                            val dob = DateUtils.getLocalDateFromMillis(it)
                            if (!DateUtils.isAgeBetween10And110(dob)) {
                                isDobError.value = true
                                dobErrorMessage.value = "Age must be > 10 and < 110 yrs"
                            } else {
                                isDobError.value = false
                                dobErrorMessage.value = ""
                            }
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = { openDialog.value = false }) { Text("Cancel") }
                },
                content = { // Use content lambda
                    DatePicker(state = datePickerState)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Gender", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            GenderOption(
                label = "Male",
                value = "Male",
                selected = gender == "Male",
                onSelect = { selectedValue -> gender = selectedValue }
            )

            Spacer(modifier = Modifier.width(40.dp))

            GenderOption(
                label = "Female",
                value = "Female",
                selected = gender == "Female",
                onSelect = { selectedValue -> gender = selectedValue }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomCheckbox(
                checked = isTermsChecked,
                onCheckedChange = { isTermsChecked = it }
            )

            val annotatedString = buildAnnotatedString {
                append("I agree to ")
                pushStringAnnotation(tag = "URL", annotation = "https://content.micropower.com.au/golf/scoring/terms")
                withStyle(style = SpanStyle(color = mslBlue)) {
                    append("Terms and Conditions. ")
                }
                pop()
            }

            TermsAndConditionsText()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomCheckbox(
                checked = true, //no choice
                onCheckedChange = {}
            )
            Text("Create or update my SOGO Golf account",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    //.clickable {
                        //isCreateOrUpdateSogoGolfAccount = !isCreateOrUpdateSogoGolfAccount
                    //}
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CenteredYellowButton(
            text = if (loading) "Saving..." else "Save",
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            enabled = isFormValid && !loading,
            onClick = {
                // Validate postcode-state combination before saving
                val currentPostcode = postcode?.trim()
                val currentState = state
                if (!currentPostcode.isNullOrBlank() && !currentState.isNullOrBlank()) {
                    if (!viewModel.isPostcodeValidForState(currentPostcode, currentState)) {
                        Toast.makeText(
                            context, 
                            "Postcode $currentPostcode does not match state $currentState. Please check your details.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@CenteredYellowButton
                    }
                }
                
                loading = true
                viewModel.viewModelScope.launch {
                    val success = viewModel.processGolferConfirmationData(
                        firstName = firstName ?: "",
                        lastName = lastName ?: "",
                        currentEmail = email ?: "",
                        state = state!!,
                        dateOfBirth = java.util.Date(dateOfBirthMillis!!),
                        currentPostcode = postcode ?: "",
                        currentMobile = mobile ?: "",
                        sogoGender = if (gender == "Male") "m" else "f",
                        existingSogoGolfer = sogoGolfer
                    )
                    loading = false

                    if (success) {
                        Toast.makeText(context, if (sogoGolfer != null) "Golfer updated successfully!" else "Golfer created successfully!", Toast.LENGTH_LONG)
                            .show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, if (sogoGolfer != null) "Failed to update golfer. Please try again." else "Failed to create golfer. Please try again.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        )
    }

}

private fun normalizeState(state: String?): String? {
    return state?.lowercase()?.trim()
}

// Updated GenderOption with automatic value mapping
@Composable
fun GenderOption(
    label: String,
    value: String,
    selected: Boolean,
    onSelect: (String) -> Unit // Pass selected value back to parent
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onSelect(value) }
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelect(value) },
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(label, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun TermsAndConditionsText() {
    val uriHandler = LocalUriHandler.current
    val url = "https://content.micropower.com.au/golf/scoring/terms"

    val annotatedString = buildAnnotatedString {
        append("By using this app, you agree to our ")

        val start = length
        append("Terms & Conditions")
        val end = length

        // Apply clickable style
        addStyle(
            style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
            start = start,
            end = end
        )

        // Add clickable annotation for URL
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = start,
            end = end
        )
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Normal, // Removes bold
            color = Color.DarkGray // Change text color
        ),
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                uriHandler.openUri(url) // Open the URL when clicked
            }
    )
}
