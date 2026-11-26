package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.explorer.makanza.data.AuthRepository
import com.explorer.makanza.models.LoginRequest
import com.explorer.makanza.models.RegisterRequest
import com.explorer.makanza.models.UserRole
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    initialIsRegister: Boolean = false,
    authRepository: AuthRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var isRegisterTab by remember { mutableStateOf(initialIsRegister) }

    // Register Form Fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 450.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRegisterTab) Strings.get("tab_register", language) else Strings.get("tab_login", language),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = if (isRegisterTab) 1 else 0) {
                    Tab(
                        selected = !isRegisterTab,
                        onClick = { isRegisterTab = false; errorMessage = null },
                        text = { Text(Strings.get("tab_login", language)) }
                    )
                    Tab(
                        selected = isRegisterTab,
                        onClick = { isRegisterTab = true; errorMessage = null },
                        text = { Text(Strings.get("nav_register", language)) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isRegisterTab) {
                    // Role Selector
                    Text(
                        text = Strings.get("label_role", language),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.STUDENT,
                            onClick = { selectedRole = UserRole.STUDENT },
                            label = { Text("🎓 ${Strings.get("role_student", language)}") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.PROPERTY_OWNER,
                            onClick = { selectedRole = UserRole.PROPERTY_OWNER },
                            label = { Text("🏢 ${Strings.get("role_owner", language)}") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(Strings.get("label_full_name", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Strings.get("label_email", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Strings.get("label_phone", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Strings.get("label_password", language)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(Strings.get("label_confirm_password", language)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Strings.get("label_email", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Strings.get("label_password", language)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = if (language == "ar") "يرجى ملء جميع الحقول المطلوبة" else "Please fill all required fields"
                            return@Button
                        }

                        if (isRegisterTab) {
                            if (fullName.isBlank() || phone.isBlank()) {
                                errorMessage = if (language == "ar") "يرجى ملء جميع حقول التسجيل" else "Please fill all registration fields"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = if (language == "ar") "كلمات السر غير متطابقة" else "Passwords do not match"
                                return@Button
                            }

                            isSubmitting = true
                            scope.launch {
                                authRepository.register(
                                    RegisterRequest(
                                        fullName = fullName.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        role = selectedRole
                                    )
                                ).fold(
                                    onSuccess = {
                                        isSubmitting = false
                                        navController.popBack()
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        errorMessage = err.message ?: "Registration failed"
                                    }
                                )
                            }
                        } else {
                            isSubmitting = true
                            scope.launch {
                                authRepository.login(
                                    LoginRequest(
                                        email = email.trim(),
                                        password = password
                                    )
                                ).fold(
                                    onSuccess = {
                                        isSubmitting = false
                                        navController.popBack()
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        errorMessage = err.message ?: "Login failed"
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isRegisterTab) Strings.get("nav_register", language) else Strings.get("nav_login", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
