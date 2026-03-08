# Snapp iOS App

## Building

1. From the **project root** (Snapp), install the shared framework and CocoaPods:
   ```bash
   ./gradlew :shared:podInstall
   ```
2. Open `iosApp/iosApp.xcworkspace` in Xcode (not the `.xcodeproj`).
3. Build and run (Cmd+R).

If you see errors in Xcode about **LayoutViewModel**, **LayoutStateSnapshot**, **getLayoutViewModel**, or **collectLayoutStateSnapshot**, the shared Kotlin module is out of date. Run from the project root:

```bash
./gradlew :shared:clean :shared:podInstall
```

Then in Xcode: **Product → Clean Build Folder** (Shift+Cmd+K), then build again (Cmd+B).
