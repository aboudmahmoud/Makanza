# 🏠 مكانزا (Makanza) - سكن الطلاب بمدينة بني سويف

**مكانزا (Makanza)** هي منصة إلكترونية شاملة مبنية باستخدام **Kotlin Multiplatform (KMP)** و **Compose Multiplatform** و **Ktor Framework** لخدمة طلاب جامعة بني سويف وجامعة النهضة (NUB) والمستثمرين وملاك العقارات بمدينة بني سويف.

---

## 🔑 Android Release Keystore & Signed APK Credentials

تم إنشاء مفتاح التوقيع الرقمي (Release Keystore) وتوليد حزمة التطبيق الموقعة الرقمياً جاهزة للتثبيت والتوزيع (**Signed Release APK**):

### 📌 بيانات مفتاح التوقيع الرقمي (Keystore Info)
- **ملف Keystore**: `makanza-release-key.jks` *(محفوظ محلياً فقط - not committed to git)*
- **Key Alias (اسم المفتاح)**: `makanza-release-key`
- **Store Password (كلمة سر المخزن)**: *(stored in `local.properties` as `release.storePassword` / `release.keyPassword`)*
- **Key Password (كلمة سر المفتاح)**: *(stored in `local.properties` as `release.storePassword` / `release.keyPassword`)*
- **فترة الصلاحية (Validity)**: `10,000 days (~27 years)`
- **التشفير (Algorithm)**: `RSA 2048-bit (SHA384withRSA)`
- **بيانات المالك (DName)**: `CN=Makanza, OU=Android, O=MakanzaEG, L=BeniSuef, ST=BeniSuef, C=EG`

---

### 📦 مسار وتفاصيل تطبيق أندرويد الموقع (Signed Release APK)
- **مسار الملف الجاهز للتثبيت**:
  - `Makanza-Release-Signed.apk` *(في المجلد الرئيسي للمشروع)*
  - `app/androidApp/build/outputs/apk/release/androidApp-release.apk`
- **حجم الملف**: `11.32 MB`
- **أمر إعادة بناء التطبيق الموقع تلقائياً**:
  ```bash
  ./gradlew :app:androidApp:assembleRelease
  ```

---

## 🌟 مميزات المشروع والمنصة

### 1. 🏫 تغطية شاملة لجامعات بني سويف والكليات
- **جامعة بني سويف** (كلية الطب، كلية الصيدلة، كلية الهندسة، كلية العلوم، كلية التمريض، المجمع الطبي)
- **جامعة النهضة NUB** (الصيدلة، طب الأسنان، الهندسة، إلخ)

### 2. 🎛️ نظام فلاتر مستقل ومتطور
- **فلتر الجامعات**: اختيار الجامعة (جامعة بني سويف / جامعة النهضة).
- **فلتر الكليات**: اختيار الكلية أو المجمع الطبي الأقرب للسكن.
- **فلتر مدة ونظام الإيجار**: (`شهري` / `بالترم` / `يومي` / `سنوي`).
- **فلتر نوع السكن والطلاب**: (`👨 طُلاّب فقط` / `👩 طالبات فقط` / `الكل`).

### 3. 👤 خاصية الزائر (Guest Mode)
- تتيح للزائر تصفح العقارات، نشر إعلانات سكن جديدة، أو إرسال طلبات حجز مباشرة بدون الحاجة لتسجيل دخول مسبق.

### 4. ⚡ وضع البيانات التجريبية المستقل (Hybrid Offline Mock Mode)
- يحتوي التطبيق على نظام `MockDataStore` يعمل تلقائياً وبشكل كامل عند عدم الاتصال بالسيرفر أو توقفه، مع إمكانية الاتصال المباشر مع سيرفر Ktor عند تشغيله.

### 5. ⚙️ لوحة تحكم الإدارة (Admin Dashboard)
- **حساب الإدارة**: `admin@makanza.eg` / `Admin123!`
- توفر إحصائيات فورية للمنصة، وإمكانية اعتماد أو رفض الإعلانات وإدارة الحجوزات والمستخدمين.

---

## ⚙️ تشغيل المشروع على مختلف المنصات

### 1. تشغيل سيرفر الخلفية (Ktor Backend)
```bash
./gradlew :server:run
```
- يعتمد السيرفر على قاعدة بيانات `makanzadb` المنشأة بـ Exposed ORM و H2 Database.

### 2. تشغيل تطبيق أندرويد (Android App)
```bash
./gradlew :app:androidApp:assembleDebug
# أو بناء النسخة الموقعة للتوزيع:
./gradlew :app:androidApp:assembleRelease
```

### 3. تشغيل تطبيق سطح المكتب (Desktop App)
```bash
./gradlew :app:desktopApp:run
```

### 4. تشغيل تطبيق الويب (Kotlin / Wasm Web App)
```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

---

## 🔐 حسابات الدخول التجريبية (Demo Credentials)

| نوع الحساب | البريد الإلكتروني | كلمة السر |
| :--- | :--- | :--- |
| **مدير النظام (Admin)** | `admin@makanza.eg` | `Admin123!` |
| **مالك عقار (Owner)** | `owner@makanza.eg` | `Owner123!` |
| **طالب جامعي (Student)** | `student@makanza.eg` | `Student123!` |
