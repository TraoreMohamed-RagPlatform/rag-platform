const translations = {
    fr: {
        dashboard: 'Tableau de bord', documents: 'Mes documents',
        chat: 'Chat IA', history: 'Historique',
        users: 'Utilisateurs', stats: 'Statistiques',
        settings: 'Parametres', logout: 'Deconnexion',
        upload: 'Uploader un document', welcome: 'Bienvenue sur RAG Platform',
        admin: 'Administration', principal: 'Principal',
        noDoc: 'Aucun document pour l instant',
        noQuestion: 'Aucune question pour l instant',
        activeAccounts: 'compte(s) actif(s)',
        quickActions: 'Actions rapides',
        startChat: 'Demarrer un chat',
        dir: 'ltr'
    },
    en: {
        dashboard: 'Dashboard', documents: 'My documents',
        chat: 'AI Chat', history: 'History',
        users: 'Users', stats: 'Statistics',
        settings: 'Settings', logout: 'Logout',
        upload: 'Upload document', welcome: 'Welcome to RAG Platform',
        admin: 'Administration', principal: 'Main',
        noDoc: 'No document yet',
        noQuestion: 'No question yet',
        activeAccounts: 'active account(s)',
        quickActions: 'Quick actions',
        startChat: 'Start a chat',
        dir: 'ltr'
    },
    ar: {
        dashboard: 'لوحة التحكم', documents: 'مستنداتي',
        chat: 'محادثة الذكاء الاصطناعي', history: 'السجل',
        users: 'المستخدمون', stats: 'الاحصائيات',
        settings: 'الاعدادات', logout: 'تسجيل الخروج',
        upload: 'رفع مستند', welcome: 'مرحبا بك في RAG Platform',
        admin: 'الادارة', principal: 'الرئيسية',
        noDoc: 'لا توجد مستندات',
        noQuestion: 'لا توجد اسئلة',
        activeAccounts: 'حساب (حسابات) نشط',
        quickActions: 'اجراءات سريعة',
        startChat: 'بدء محادثة',
        dir: 'rtl'
    }
};

function setLang(lang) {
    localStorage.setItem('lang', lang);
    const t = translations[lang];
    document.documentElement.dir = t.dir;

    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (t[key]) el.textContent = t[key];
    });

    document.querySelectorAll('.lang-btn-global').forEach(btn => {
        btn.style.background = 'none';
        btn.style.color = '#64748b';
        btn.style.fontWeight = 'normal';
    });
    const active = document.getElementById('btn-' + lang);
    if (active) {
        active.style.background = '#1e3a5f';
        active.style.color = '#fff';
        active.style.fontWeight = '600';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const lang = localStorage.getItem('lang') || 'fr';
    setLang(lang);
});