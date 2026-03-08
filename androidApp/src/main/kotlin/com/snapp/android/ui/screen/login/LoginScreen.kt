package com.snapp.android.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.snapp.android.R
import com.snapp.android.viewmodel.AuthViewModel
import com.snapp.presentation.state.AuthUiState
import com.snapp.presentation.util.AuthMessages
import org.koin.androidx.compose.koinViewModel

private val BlueAccent = Color(0xFF3B82F6)
private val PurpleAccent = Color(0xFF8B5CF6)
private val GrayLabel = Color(0xFF737373)
private val BorderGray = Color(0xFFE5E5E5)
private val HeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF5F7FF),  // blue-50
        Color(0xFFFAF5FF),  // purple-50
        Color(0xFFFFF5F9)   // pink-50
    )
)

/** Permissive email check so valid addresses (e.g. with subdomains, dots in local part) are not rejected. */
private fun isValidEmail(s: String): Boolean {
    if (s.isBlank()) return false
    val at = s.indexOf('@')
    if (at <= 0 || at >= s.length - 1) return false
    val domain = s.substring(at + 1)
    if (!domain.contains('.')) return false
    return true
}

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    viewModel: AuthViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // One-shot auth feedback: Error only (Success navigates away; no Toast there to avoid disposal crashes)
    LaunchedEffect(authState) {
        try {
            when (val state = authState) {
                is AuthUiState.Error -> {
                    val message = state.message
                    Handler(Looper.getMainLooper()).post {
                        try {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        } catch (_: Throwable) { }
                    }
                }
                else -> {}
            }
        } catch (_: Throwable) { }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val isLoading = authState is AuthUiState.Loading

    fun validate(): Boolean {
        val trimmedEmail = email.trim()
        emailError = when {
            trimmedEmail.isBlank() -> "Email is required"
            !isValidEmail(trimmedEmail) -> "Please enter a valid email address"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        return emailError == null && passwordError == null
    }

    fun onSubmit() {
        focusManager.clearFocus()
        if (validate()) {
            viewModel.clearError()
            viewModel.login(email.trim(), password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeroGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 40.dp)
        ) {
            // Card 1 (top): tall card; decorative background circles only – text drawn on top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(color = Color.White)
            ) {
                // Decorative circles: card is clipped, so keep offsets small so circles stay partially
                // inside the card; large offsets move them outside and they get clipped (no visible change).
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(80.dp)
                        .offset(x = 48.dp, y = 38.dp)
                        .background(
                            color = Color.Gray.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                ) {}
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(72.dp)
                        .offset(x = (-2).dp, y = (120).dp)
                        .background(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {}
                // Content on top of background circles – more height, proper text alignment (Figma)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 72.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.snapp_logo),
                        contentDescription = "Snapp logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = "Your AI Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = "Smart automation for next-gen customer engagement and relationship intelligence.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayLabel,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "24/7", label = "Always On", color = BlueAccent)
                        StatItem(value = "3.2x", label = "Faster", color = PurpleAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 2 (bottom): black head strap + login form – more height (Figma)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(24.dp))
            ) {
                // Black head strap at top of second card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.Black)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp, bottom = 32.dp)
                ) {
                    // Top texts: center-aligned with generous spacing (Figma)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            ),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Enter your credentials to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayLabel
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        label = { Text("Email Address", color = GrayLabel) },
                        placeholder = { Text("Enter your email", color = GrayLabel.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderGray,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedLabelColor = GrayLabel,
                            unfocusedLabelColor = GrayLabel
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        label = { Text("Password", color = GrayLabel) },
                        placeholder = { Text("Enter your password", color = GrayLabel.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    painter = painterResource(id = if (showPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = GrayLabel
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderGray,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedLabelColor = GrayLabel,
                            unfocusedLabelColor = GrayLabel
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = Color.Black,
                                    uncheckedColor = BorderGray
                                )
                            )
                            Text(
                                text = "Remember me",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayLabel
                            )
                        }
                        TextButton(
                            onClick = onForgotPassword,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Forgot password?",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onSubmit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Signing in…" else "Sign In",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Signup row: centered as one line (Figma)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayLabel
                        )
                        TextButton(
                            onClick = onSignUp,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Signup",
                                color = BlueAccent,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GrayLabel
        )
    }
}
