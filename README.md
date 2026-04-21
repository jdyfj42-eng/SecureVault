# 🔐 SecureVault — Kotlin Native (AIDE Ready)

تطبيق Android Native مبني بـ Kotlin + XML يعمل مباشرة في AIDE.

---

## 🚀 فتح المشروع في AIDE

1. فك ضغط `SecureVault.zip`
2. افتح **AIDE**
3. اضغط **Open** ← اختر مجلد `SecureVault`
4. AIDE سيتعرف على المشروع تلقائياً
5. اضغط **▶ Run** للبناء والتشغيل

---

## 📁 هيكل المشروع

```
app/src/main/
├── java/com/securevault/
│   ├── activities/
│   │   ├── SplashActivity.kt     شاشة البداية
│   │   ├── SetupActivity.kt      إعداد Master Password
│   │   ├── LockActivity.kt       شاشة القفل + بصمة
│   │   ├── MainActivity.kt       الشاشة الرئيسية
│   │   ├── AddEditActivity.kt    إضافة / تعديل
│   │   ├── DetailActivity.kt     عرض التفاصيل
│   │   └── GeneratorActivity.kt  مولّد كلمات المرور
│   ├── adapters/
│   │   └── PasswordAdapter.kt    RecyclerView
│   ├── models/
│   │   └── PasswordEntry.kt      نموذج البيانات
│   └── services/
│       ├── EncryptionService.kt  AES-256 + PBKDF2
│       └── DatabaseHelper.kt     SQLite
└── res/
    ├── layout/   واجهات XML
    ├── drawable/ خلفيات وأيقونات
    ├── values/   ألوان وأنماط
    └── menu/     قوائم
```

---

## 🔒 الأمان

| الميزة | التقنية |
|--------|---------|
| تشفير | AES-256-CBC |
| اشتقاق المفتاح | PBKDF2WithHmacSHA256 (100k iteration) |
| تخزين آمن | EncryptedSharedPreferences |
| مصادقة | Master Password + Biometrics |
| قاعدة البيانات | SQLite محلي |

---

## ✨ الميزات

- ✅ إضافة / تعديل / حذف كلمات المرور
- ✅ تشفير AES-256 لكل كلمة مرور
- ✅ فتح بالبصمة
- ✅ مولّد كلمات مرور قوية
- ✅ 8 فئات مع أيقونات
- ✅ المفضلة ⭐
- ✅ بحث سريع
- ✅ إحصائيات (Total / Favorites / Categories)
- ✅ مؤشر قوة كلمة المرور
- ✅ نسخ بضغطة واحدة
- ✅ حذف مباشر
- ✅ واجهة Cyberpunk داكنة احترافية
