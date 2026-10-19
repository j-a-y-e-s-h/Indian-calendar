# build.ps1 - Automated builder for CustomHinduWidget
$ErrorActionPreference = "Stop"

$sdk = "C:\Users\Admin\AppData\Local\Android\Sdk"
$buildTools = "$sdk\build-tools\36.0.0"
$platformJar = "$sdk\platforms\android-36\android.jar"
$jdk = "C:\Users\Admin\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin"
$keystore = "c:\Users\Admin\Desktop\HinduCalendar_APK\keystore\test.keystore"

Write-Host "==> Cleaning build artifacts..."
if (Test-Path "CustomHinduWidget\build") {
    Remove-Item -Recurse -Force "CustomHinduWidget\build"
}
New-Item -ItemType Directory -Path "CustomHinduWidget\build\gen" -Force | Out-Null
New-Item -ItemType Directory -Path "CustomHinduWidget\build\classes" -Force | Out-Null
New-Item -ItemType Directory -Path "CustomHinduWidget\build\dex" -Force | Out-Null
New-Item -ItemType Directory -Path "output" -Force | Out-Null

Write-Host "==> 1. Compiling resources..."
& "$buildTools\aapt2.exe" compile --dir "CustomHinduWidget\res" -o "CustomHinduWidget\build\resources.zip"

Write-Host "==> 2. Linking resources and generating R.java..."
& "$buildTools\aapt2.exe" link -I $platformJar `
    --manifest "CustomHinduWidget\AndroidManifest.xml" `
    -o "CustomHinduWidget\build\unaligned.apk" `
    --java "CustomHinduWidget\build\gen" `
    --auto-add-overlay `
    -A "CustomHinduWidget\assets" `
    "CustomHinduWidget\build\resources.zip"

Write-Host "==> 3. Compiling Java sources..."
$javaSources = (Get-ChildItem -Path "CustomHinduWidget\src\com\custom\hinduwidget\*.java", "CustomHinduWidget\build\gen\com\custom\hinduwidget\R.java").FullName
& "$jdk\javac.exe" -encoding UTF-8 -cp $platformJar -d "CustomHinduWidget\build\classes" -source 11 -target 11 $javaSources

Write-Host "==> 4. Generating classes.dex with d8..."
$classes = (Get-ChildItem -Recurse -Path "CustomHinduWidget\build\classes\*.class").FullName
$classes | Out-File -FilePath "CustomHinduWidget\build\classes.args" -Encoding ASCII
& "$buildTools\d8.bat" --output "CustomHinduWidget\build\dex" --min-api 26 "@CustomHinduWidget\build\classes.args"

Write-Host "==> 5. Packaging classes.dex into APK..."
python -c "
import zipfile
with zipfile.ZipFile('CustomHinduWidget/build/unaligned.apk', 'a') as z:
    z.write('CustomHinduWidget/build/dex/classes.dex', 'classes.dex')
"

Write-Host "==> 6. Zipaligning APK..."
& "$buildTools\zipalign.exe" -p 4 "CustomHinduWidget\build\unaligned.apk" "CustomHinduWidget\build\aligned.apk"

Write-Host "==> 7. Signing APK..."
& "$buildTools\apksigner.bat" sign --ks $keystore --ks-pass pass:android --ks-key-alias testkey --out "output\com.custom.hinduwidget.apk" "CustomHinduWidget\build\aligned.apk"

Write-Host "==> Build complete: output\com.custom.hinduwidget.apk"
