import SwiftUI
import UIKit

/// A text field that clears the keyboard input assistant (SystemInputAssistantView) to avoid
/// "Unable to simultaneously satisfy constraints" with assistantHeight == 72 when the keyboard appears.
/// Use for login/password fields where the shortcut bar is not needed.
struct KeyboardFriendlyTextField: UIViewRepresentable {
    let placeholder: String
    @Binding var text: String
    var isSecure: Bool = false
    /// When false, use SwiftUI overlay for border (e.g. to match design system).
    var useSystemBorder: Bool = true

    func makeUIView(context: Context) -> UITextField {
        let field = UITextField()
        field.placeholder = placeholder
        field.isSecureTextEntry = isSecure
        field.autocorrectionType = .no
        field.autocapitalizationType = .none
        field.borderStyle = useSystemBorder ? .roundedRect : .none
        if !useSystemBorder {
            field.backgroundColor = .systemBackground
        }
        field.delegate = context.coordinator
        field.addTarget(context.coordinator, action: #selector(Coordinator.textDidChange), for: .editingChanged)
        // Remove input assistant bar to prevent SystemInputAssistantView height == 72 constraint conflict.
        field.inputAssistantItem.leadingBarButtonGroups = []
        field.inputAssistantItem.trailingBarButtonGroups = []
        field.inputAccessoryView = nil
        return field
    }

    func updateUIView(_ uiView: UITextField, context: Context) {
        if uiView.text != text {
            uiView.text = text
        }
        uiView.isSecureTextEntry = isSecure
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, UITextFieldDelegate {
        var parent: KeyboardFriendlyTextField

        init(_ parent: KeyboardFriendlyTextField) {
            self.parent = parent
        }

        @objc func textDidChange(_ textField: UITextField) {
            parent.text = textField.text ?? ""
        }
    }
}
