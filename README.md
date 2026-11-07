# Graph Studio (APK)

برنامه یک WebView است که فایل `app/src/main/assets/index.html` را نشان می‌دهد.

## ساخت APK
1. همه‌ی فایل‌ها (با پوشه‌ی `.github`) را در یک ریپوی GitHub بگذار (شاخه‌ی main).
2. تب **Actions** ← workflow به نام **Build Graph Studio APK** خودکار اجرا می‌شود (یا Run workflow).
3. بعد از سبز شدن، پایین صفحه‌ی همان اجرا در بخش **Artifacts** فایل `Graph-Studio-APK` را دانلود کن؛ داخلش `app-debug.apk` است.

## آپدیت برنامه
فقط `app/src/main/assets/index.html` را با نسخه‌ی جدید جایگزین کن و commit کن.
