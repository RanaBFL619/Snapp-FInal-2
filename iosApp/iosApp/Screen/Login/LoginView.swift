import SwiftUI

/// Login screen matching Figma: hero section ({Snapp}, Your AI Assistant, stats) + Sign In form.
/// Receives `authVM` from the environment. Navigation to authenticated zone is handled by RootView.
struct LoginView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @EnvironmentObject var toast: ToastManager

    @State private var email: String = ""
    @State private var password: String = ""
    @State private var emailError: String? = nil
    @State private var passwordError: String? = nil
    @State private var showPassword: Bool = false
    @State private var rememberMe: Bool = false

    @State private var unauthPath: [UnauthRoute] = []

    private let blueAccent = Color(red: 59/255, green: 130/255, blue: 246/255)   // #3B82F6
    private let purpleAccent = Color(red: 139/255, green: 92/255, blue: 246/255) // #8B5CF6
    private let grayLabel = Color(red: 0.45, green: 0.45, blue: 0.45)
    private let borderGray = Color(red: 0.9, green: 0.9, blue: 0.9)

    private var isLoading: Bool {
        if case .loading = authVM.authState { return true }
        return false
    }

    var body: some View {
        NavigationStack(path: $unauthPath) {
            ScrollView(.vertical, showsIndicators: true) {
                VStack(spacing: 16) {
                    // Card 1 (top): tall highlighted card; background circles are decorative only, text above them
                    heroSection
                        .frame(maxWidth: .infinity)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 28))
                        .shadow(color: .black.opacity(0.08), radius: 20, x: 0, y: 6)

                    // Card 2 (bottom): black head strap + login form
                    formSection
                        .frame(maxWidth: .infinity)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .shadow(color: .black.opacity(0.06), radius: 16, x: 0, y: 4)
                }
                .padding(.horizontal, 20)
                .padding(.top, 24)
                .padding(.bottom, 40)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(
                LinearGradient(
                    colors: [
                        Color(red: 0.96, green: 0.97, blue: 1.0),   // blue-50
                        Color(red: 0.98, green: 0.96, blue: 1.0),   // purple-50
                        Color(red: 1.0, green: 0.97, blue: 0.98)    // pink-50
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
            )
            .scrollDismissesKeyboard(.interactively)
            .navigationDestination(for: UnauthRoute.self) { route in
                switch route {
                case .forgotPassword:
                    ForgotPasswordView()
                case .resetPassword(let token):
                    Text("Reset Password — token: \(token)")
                }
            }
            .onAppear {
                if case .error(let msg) = authVM.authState {
                    toast.show(LoginErrorSanitizer.userFriendly(msg), isSuccess: false)
                    authVM.clearError()
                }
            }
            .onChange(of: authVM.authState) { newState in
                if case .error(let msg) = newState {
                    toast.show(LoginErrorSanitizer.userFriendly(msg), isSuccess: false)
                    authVM.clearError()
                }
            }
        }
    }

    // MARK: - Card 1: Hero – decorative background circles (not layout); text above; card is tall
    private var heroSection: some View {
        ZStack(alignment: .topLeading) {
            // Decorative background balls – smaller, correct position; text draws on top
            Circle()
                .fill(Color.gray.opacity(0.12))
                .frame(width: 60, height: 60)
                .offset(x: 60, y: 40) // top-left: arc visible, top of circle slightly above "{Snapp}"

            Circle()
                .fill(Color.gray.opacity(0.1))
                .frame(width: 65, height: 65)
                .offset(x: 250, y: 280) // right side: roughly level with description text, cut off by right edge

            // Content on top of background circles
            VStack(spacing: 28) {
                Image("AppLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 50)
                    .frame(maxWidth: .infinity)

                Text("Your AI Assistant")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.black)

                Text("Smart automation for next-gen customer engagement and relationship intelligence.")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundColor(grayLabel)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.horizontal, 20)

                // Stats only (no circle in layout – circle is background)
                HStack(spacing: 48) {
                    statItem(value: "24/7", label: "Always On", color: blueAccent)
                    statItem(value: "3.2x", label: "Faster", color: purpleAccent)
                }
                .padding(.top, 16)
            }
            .padding(.top, 56)
            .padding(.bottom, 64)
            .padding(.horizontal, 32)
            .frame(maxWidth: .infinity)
        }
        .frame(maxWidth: .infinity)
    }

    private func statItem(value: String, label: String, color: Color) -> some View {
        VStack(spacing: 6) {
            Text(value)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(color)
            Text(label)
                .font(.system(size: 13, weight: .regular))
                .foregroundColor(grayLabel)
        }
    }

    // MARK: - Card 2: Black head strap + login form
    private var formSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Black head strap at top of second card (full width)
            Rectangle()
                .fill(Color.black)
                .frame(height: 6)
                .frame(maxWidth: .infinity)

            VStack(alignment: .leading, spacing: 20) {
            // Top texts: center-aligned with generous spacing (per design)
            VStack(spacing: 10) {
                Text("Sign In")
                    .font(.system(size: 24, weight: .semibold))
                    .foregroundColor(.black)
                Text("Enter your credentials to continue")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundColor(grayLabel)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 32)
            .padding(.bottom, 28)

            // Email Address
            VStack(alignment: .leading, spacing: 6) {
                Text("Email Address")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(grayLabel)
                KeyboardFriendlyTextField(placeholder: "Enter your email", text: $email, isSecure: false, useSystemBorder: false)
                    .disabled(isLoading)
                    .onChange(of: email) { _ in emailError = nil }
                    .padding(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(borderGray, lineWidth: 1)
                    )
                    .background(Color.white)
                    .cornerRadius(8)
                if let err = emailError {
                    Text(err)
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            // Password
            VStack(alignment: .leading, spacing: 6) {
                Text("Password")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(grayLabel)
                passwordField
                if let err = passwordError {
                    Text(err)
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            // Remember me + Forgot password
            HStack {
                Toggle(isOn: $rememberMe) {
                    Text("Remember me")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundColor(grayLabel)
                }
                .toggleStyle(CheckboxToggleStyle())
                Spacer()
                Button("Forgot password?") {
                    unauthPath.append(.forgotPassword)
                }
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.black)
                .disabled(isLoading)
            }

            // Sign In button (black)
            Button(action: onSignIn) {
                HStack {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(0.9)
                            .padding(.trailing, 8)
                    }
                    Text(isLoading ? "Signing in…" : "Sign In")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .background(Color.black)
            .cornerRadius(10)
            .disabled(isLoading)
            .opacity(isLoading ? 0.8 : 1)

            // Don't have an account? Signup (centered)
            HStack(spacing: 4) {
                Spacer(minLength: 0)
                Text("Don't have an account?")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundColor(grayLabel)
                Button("Signup") {
                    // Signup route can be added later
                }
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(blueAccent)
                Spacer(minLength: 0)
            }
            .padding(.top, 12)
            }
            .padding(24)
        }
        .frame(maxWidth: .infinity)
    }

    private var passwordField: some View {
        HStack {
            KeyboardFriendlyTextField(
                placeholder: "Enter your password",
                text: $password,
                isSecure: !showPassword,
                useSystemBorder: false
            )
            .disabled(isLoading)
            .onChange(of: password) { _ in passwordError = nil }
            .padding(10)
            Button(action: { showPassword.toggle() }) {
                Image(systemName: showPassword ? "eye.slash" : "eye")
                    .foregroundColor(grayLabel)
                    .frame(width: 44, height: 44)
            }
            .disabled(isLoading)
        }
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(borderGray, lineWidth: 1)
        )
        .background(Color.white)
        .cornerRadius(8)
    }

    private func onSignIn() {
        authVM.clearError()
        guard validate() else { return }
        authVM.login(username: email, password: password)
    }

    @discardableResult
    private func validate() -> Bool {
        emailError = email.trimmingCharacters(in: .whitespaces).isEmpty
            ? "Email is required"
            : nil
        passwordError = password.isEmpty
            ? "Password is required"
            : password.count < 6
                ? "Password must be at least 6 characters"
                : nil
        return emailError == nil && passwordError == nil
    }
}

// MARK: - Checkbox-style toggle (no switch, square box)
struct CheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        Button(action: {
            configuration.$isOn.wrappedValue = !configuration.$isOn.wrappedValue
        }) {
            HStack(spacing: 8) {
                ZStack {
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(Color.gray.opacity(0.6), lineWidth: 1.5)
                        .frame(width: 20, height: 20)
                    if configuration.isOn {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.black)
                            .frame(width: 20, height: 20)
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                configuration.label
            }
        }
        .buttonStyle(.plain)
    }
}
