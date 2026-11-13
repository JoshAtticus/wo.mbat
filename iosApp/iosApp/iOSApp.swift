import UIKit
import WombatShared

private func crashLogURL() -> URL? {
    FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?
        .appendingPathComponent("last_crash.txt")
}

final class CrashReportViewController: UIViewController {
    private let reportText: String
    private var shareButton: UIButton?

    init(reportText: String) {
        self.reportText = reportText
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(white: 0.08, alpha: 1.0)

        let titleLabel = UILabel()
        titleLabel.text = "wo.mbat Crash Report"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 20)
        titleLabel.textColor = UIColor(red: 1.0, green: 0.42, blue: 0.42, alpha: 1.0)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false

        let shareBtn = UIButton(type: .system)
        shareBtn.setTitle("AirDrop / Share", for: .normal)
        shareBtn.titleLabel?.font = UIFont.boldSystemFont(ofSize: 15)
        shareBtn.backgroundColor = UIColor(red: 0.39, green: 0.40, blue: 0.95, alpha: 1.0)
        shareBtn.setTitleColor(.white, for: .normal)
        shareBtn.layer.cornerRadius = 8
        shareBtn.contentEdgeInsets = UIEdgeInsets(top: 10, left: 14, bottom: 10, right: 14)
        shareBtn.addTarget(self, action: #selector(shareTapped), for: .touchUpInside)
        shareBtn.translatesAutoresizingMaskIntoConstraints = false
        self.shareButton = shareBtn

        let copyButton = UIButton(type: .system)
        copyButton.setTitle("Copy", for: .normal)
        copyButton.titleLabel?.font = UIFont.systemFont(ofSize: 15, weight: .semibold)
        copyButton.backgroundColor = UIColor(white: 0.22, alpha: 1.0)
        copyButton.setTitleColor(.white, for: .normal)
        copyButton.layer.cornerRadius = 8
        copyButton.contentEdgeInsets = UIEdgeInsets(top: 10, left: 14, bottom: 10, right: 14)
        copyButton.addTarget(self, action: #selector(copyTapped), for: .touchUpInside)
        copyButton.translatesAutoresizingMaskIntoConstraints = false

        let continueButton = UIButton(type: .system)
        continueButton.setTitle("Clear & Launch", for: .normal)
        continueButton.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        continueButton.setTitleColor(.white, for: .normal)
        continueButton.layer.borderColor = UIColor.darkGray.cgColor
        continueButton.layer.borderWidth = 1
        continueButton.layer.cornerRadius = 8
        continueButton.contentEdgeInsets = UIEdgeInsets(top: 10, left: 14, bottom: 10, right: 14)
        continueButton.addTarget(self, action: #selector(continueTapped), for: .touchUpInside)
        continueButton.translatesAutoresizingMaskIntoConstraints = false

        let textView = UITextView()
        textView.text = reportText
        textView.isEditable = false
        textView.backgroundColor = UIColor(white: 0.14, alpha: 1.0)
        textView.textColor = UIColor(white: 0.92, alpha: 1.0)
        textView.font = UIFont.monospacedSystemFont(ofSize: 12, weight: .regular)
        textView.layer.cornerRadius = 8
        textView.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(titleLabel)
        view.addSubview(shareBtn)
        view.addSubview(copyButton)
        view.addSubview(continueButton)
        view.addSubview(textView)

        let guide = view.safeAreaLayoutGuide
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: guide.topAnchor, constant: 16),
            titleLabel.leadingAnchor.constraint(equalTo: guide.leadingAnchor, constant: 16),
            titleLabel.trailingAnchor.constraint(equalTo: guide.trailingAnchor, constant: -16),

            shareBtn.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 12),
            shareBtn.leadingAnchor.constraint(equalTo: guide.leadingAnchor, constant: 16),

            copyButton.centerYAnchor.constraint(equalTo: shareBtn.centerYAnchor),
            copyButton.leadingAnchor.constraint(equalTo: shareBtn.trailingAnchor, constant: 10),

            continueButton.centerYAnchor.constraint(equalTo: shareBtn.centerYAnchor),
            continueButton.leadingAnchor.constraint(equalTo: copyButton.trailingAnchor, constant: 10),

            textView.topAnchor.constraint(equalTo: shareBtn.bottomAnchor, constant: 12),
            textView.leadingAnchor.constraint(equalTo: guide.leadingAnchor, constant: 16),
            textView.trailingAnchor.constraint(equalTo: guide.trailingAnchor, constant: -16),
            textView.bottomAnchor.constraint(equalTo: guide.bottomAnchor, constant: -16)
        ])
    }

    @objc private func shareTapped() {
        var items: [Any] = []
        if let url = crashLogURL(), FileManager.default.fileExists(atPath: url.path) {
            items.append(url)
        } else {
            items.append(reportText)
        }
        let activityVC = UIActivityViewController(activityItems: items, applicationActivities: nil)
        if let popover = activityVC.popoverPresentationController {
            popover.sourceView = shareButton ?? view
            popover.sourceRect = (shareButton ?? view).bounds
        }
        present(activityVC, animated: true)
    }

    @objc private func copyTapped() {
        UIPasteboard.general.string = reportText
    }

    @objc private func continueTapped() {
        if let url = crashLogURL() {
            try? FileManager.default.removeItem(at: url)
        }
        view.window?.rootViewController = MainViewControllerKt.MainViewController()
    }
}

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        NSSetUncaughtExceptionHandler { exception in
            let report = """
            === UNCAUGHT OBJC EXCEPTION ===
            Name: \(exception.name.rawValue)
            Reason: \(exception.reason ?? "nil")
            UserInfo: \(exception.userInfo ?? [:])

            CallStackSymbols:
            \(exception.callStackSymbols.joined(separator: "\n"))
            """
            if let url = crashLogURL() {
                try? report.write(to: url, atomically: true, encoding: .utf8)
            }
        }

        let window = UIWindow(frame: UIScreen.main.bounds)
        if let url = crashLogURL(),
           FileManager.default.fileExists(atPath: url.path),
           let crashText = try? String(contentsOf: url, encoding: .utf8),
           !crashText.isEmpty {
            window.rootViewController = CrashReportViewController(reportText: crashText)
        } else {
            window.rootViewController = MainViewControllerKt.MainViewController()
        }
        window.makeKeyAndVisible()
        self.window = window
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        MainViewControllerKt.handleWombatDeepLink(url: url.absoluteString)
        return true
    }

    func application(
        _ application: UIApplication,
        continue userActivity: NSUserActivity,
        restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void
    ) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
           let url = userActivity.webpageURL {
            MainViewControllerKt.handleWombatDeepLink(url: url.absoluteString)
            return true
        }
        return false
    }
}
