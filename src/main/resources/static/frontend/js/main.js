document.addEventListener('DOMContentLoaded', () => {
    renderNavbar();
    fetchNotice();
});

function renderNavbar() {
    const container = document.getElementById('navbar-container');
    if (!container) return;

    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    const displayRole = role ? role.replace('ROLE_', '') : '';

    // ১. বর্তমানে কোন পেজে আছি তা বের করার লজিক
    const currentPage = window.location.pathname.split("/").pop() || "index.html";

    // হেল্পার ফাংশন: একটি পেজ একটিভ কি না চেক করার জন্য
    const isActive = (path) => currentPage === path ? 'text-blue-600 font-bold border-b-2 border-blue-600' : 'text-gray-600 hover:text-blue-600 transition';
    const isActiveMobile = (path) => currentPage === path ? 'bg-blue-50 text-blue-600 font-bold' : 'text-gray-600 hover:bg-blue-50 transition';

    // ২. অথ বাটন লজিক
    let authButtons = token ? `
        <div class="flex items-center space-x-3">
            <span class="hidden sm:inline-block bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider">
                ${displayRole}
            </span>
            <button id="logoutBtn" class="text-red-500 font-semibold hover:text-red-700 text-sm flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-red-50 transition">
                <i class="fas fa-sign-out-alt"></i> <span class="hidden sm:inline">Logout</span>
            </button>
        </div>
    ` : `
        <div class="flex items-center space-x-4">
            <a href="login.html" class="text-gray-600 text-sm font-semibold hover:text-blue-600 transition">Login</a>
            <a href="signup.html" class="bg-blue-600 text-white px-5 py-2 rounded-full text-sm font-bold hover:bg-blue-700 shadow-md shadow-blue-100 transition transform hover:-translate-y-0.5">Sign Up</a>
        </div>
    `;

    // ৩. নেভবার স্ট্রাকচার
    container.innerHTML = `
        <nav class="bg-white/80 backdrop-blur-md shadow-sm sticky top-0 z-50 border-b border-gray-100">
            <div class="max-w-7xl mx-auto px-4">
                <div class="flex justify-between h-20 items-center">
                    <div class="flex items-center">
                        <a href="index.html" class="text-2xl font-black text-blue-600 tracking-tighter">T.<span class="text-blue-950">Mukimii</span><span class="text-blue-400"></span></a>
                    </div>
                    
                    <div class="hidden md:flex space-x-8 items-center h-full">
                        <a href="index.html" class="flex items-center h-full px-1 ${isActive('index.html')}">Home</a>
                        <a href="doctors.html" class="flex items-center h-full px-1 ${isActive('doctors.html')}">Doctors</a>
                        ${token ? `<a href="${getDashboardUrl(role)}" class="flex items-center h-full px-1 ${isActive(getDashboardUrl(role))}">Dashboard</a>` : ''}
                    </div>

                    <div class="flex items-center space-x-2">
                        ${authButtons}
                        <button id="mobile-menu-btn" class="md:hidden text-gray-600 focus:outline-none p-2 bg-gray-50 rounded-lg ml-2">
                            <i class="fas fa-bars text-xl"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div id="mobile-menu" class="hidden md:hidden bg-white border-t border-gray-100 pb-4 shadow-xl overflow-hidden animate-in slide-in-from-top duration-300">
                <a href="index.html" class="block px-6 py-4 text-sm font-medium ${isActiveMobile('index.html')}">Home</a>
                <a href="doctors.html" class="block px-6 py-4 text-sm font-medium ${isActiveMobile('doctors.html')}">Doctors</a>
                ${token ? `<a href="${getDashboardUrl(role)}" class="block px-6 py-4 text-sm font-bold ${isActiveMobile(getDashboardUrl(role))}">Dashboard</a>` : ''}
            </div>
        </nav>
    `;

    // ইভেন্ট লিসেনার্স
    document.getElementById('logoutBtn')?.addEventListener('click', handleLogout);

    const menuBtn = document.getElementById('mobile-menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');

    menuBtn?.addEventListener('click', () => {
        mobileMenu.classList.toggle('hidden');
        const icon = menuBtn.querySelector('i');
        icon.classList.toggle('fa-bars');
        icon.classList.toggle('fa-times');
    });
}

function getDashboardUrl(role) {
    if (!role) return 'login.html';
    const r = role.toUpperCase();
    if (r === 'ROLE_ADMIN' || r === 'ADMIN') return 'admin-dashboard.html';
    if (r === 'ROLE_DOCTOR' || r === 'DOCTOR') return 'doctor-dashboard.html';
    return 'patient-dashboard.html';
}

function handleLogout() {
    localStorage.clear();
    alert('Logged out successfully!');
    window.location.href = 'index.html';
}




async function fetchNotice() {
    try {
        // ফুল পাথ (Full URL) ব্যবহার করুন কারণ আপনার ব্যাকএন্ড ৮০৮০ পোর্টে চলছে
        const response = await fetch('http://localhost:8080/api/v1/public/active-notices');

        // যদি রেসপন্স ওকে না হয় (যেমন ৪-৪ বা ৫০০) তবে এরর থ্রো করবে
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const notices = await response.json();

        if (Array.isArray(notices) && notices.length > 0) {
            const bar = document.getElementById('notice-bar');
            const textElement = document.getElementById('notice-text');

            // সব নোটিশ একসাথে জুড়ে দেওয়া
            textElement.innerText = notices.map(n => n.content).join(' | ');
            bar.classList.remove('hidden');
        }
    } catch (error) {
        console.error("Notice fetch error:", error);
    }
}