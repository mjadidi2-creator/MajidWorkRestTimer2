# ساخت APK آنلاین با GitHub Actions

## مرحله 1: ساخت Repository
1. وارد github.com شو.
2. روی New repository بزن.
3. یک نام مثل `MajidWorkRestTimer` انتخاب کن.
4. Repository را Public یا Private بساز.

## مرحله 2: آپلود پروژه
1. فایل ZIP پروژه را Extract کن.
2. محتویات پوشه `MajidWorkRestTimer` را داخل Repository آپلود کن.
3. دقت کن فایل زیر هم وجود داشته باشد:

`.github/workflows/build-apk.yml`

## مرحله 3: اجرای Build
1. وارد Repository شو.
2. از بالا روی تب `Actions` بزن.
3. Workflow با نام `Build Android APK` را انتخاب کن.
4. روی `Run workflow` بزن.
5. چند دقیقه بعد Build تمام می‌شود.

## مرحله 4: دانلود APK
1. داخل همان صفحه اجرای Action برو.
2. پایین صفحه بخش `Artifacts` را پیدا کن.
3. فایل `MajidWorkRestTimer-debug-apk` را دانلود کن.
4. داخل آن، فایل APK قرار دارد.

## نکته مهم
این خروجی Debug APK است و برای تست روی گوشی مناسب است. برای انتشار رسمی در بازارها یا Google Play باید نسخه Release با امضای اختصاصی ساخته شود.
