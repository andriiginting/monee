import SwiftUI
#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    
    @State var isDarkMode: Bool = UITraitCollection.current.userInterfaceStyle == .dark

    init() {
#if canImport(FirebaseCore)
        let configName = isDebugBuild ? "GoogleService-Info-Debug" : "GoogleService-Info"
        if let path = Bundle.main.path(forResource: configName, ofType: "plist"),
           let options = FirebaseOptions(contentsOfFile: path) {
            FirebaseApp.configure(options: options)
        }
#endif
    }
    
	var body: some Scene {
		WindowGroup {
            ContentView(isDarkMode: $isDarkMode)
                            .onChange(of: scenePhase) { newScenePhase in
                                if newScenePhase == .active {
                                    let osTheme: UITraitCollection = UIScreen.main.traitCollection
                                    isDarkMode = osTheme.userInterfaceStyle == .dark
                                }
                            }
                            .preferredColorScheme(isDarkMode ? .dark : .light)
		}
	}
}

private var isDebugBuild: Bool {
#if DEBUG
    true
#else
    false
#endif
}

extension UIColor {
    convenience init(rgb: UInt32) {
        self.init(
            red: CGFloat((rgb & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgb & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgb & 0x0000FF) / 255.0,
            alpha: 1.0
        )
    }
}
