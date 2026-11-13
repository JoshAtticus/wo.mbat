import SwiftUI
import WombatShared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    MainViewControllerKt.handleWombatDeepLink(url: url.absoluteString)
                }
        }
    }
}
