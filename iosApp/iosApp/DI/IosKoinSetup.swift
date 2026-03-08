import Foundation
import shared

enum IosKoinSetup {
    static func doInitKoin() {
        SnappKoin.shared.doInitKoin(platformContext: nil as Any?)
    }
}
