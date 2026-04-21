const AUTH_URL = "http://localhost:8080/api/v1/auth";
const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
let allDoctors = [];

const userRole = localStorage.getItem('userRole');
const token = localStorage.getItem('accessToken');

if (!token || (userRole !== 'ROLE_ADMIN' && userRole !== 'ADMIN')) {
    alert('Access Denied or Session Expired!');
    window.location.href = 'index.html';
}

document.addEventListener('DOMContentLoaded', () => {
    fetchDoctorsForSelector();
    generateAdminScheduleSections();
    updateDashboardStats();
    loadAppointmentChart();
    loadAuditLogs();
    loadDoctorPerformance();
    loadMedicineAndDiagnosisCharts();
    loadPendingApprovals();
    loadCurrentNotice();

    // ইভেন্ট লিসেনার্স
    document.getElementById('adminScheduleForm')?.addEventListener('submit', saveScheduleByAdmin);
    document.getElementById('addDoctorForm')?.addEventListener('submit', createDoctor);

    // --- সার্চ ফিল্টার লজিক (সরাসরি DOMContentLoaded এর ভেতর) ---
    const searchInput = document.getElementById('doctor-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase().trim();
            console.log("Searching for:", term);

            const filtered = allDoctors.filter(doc =>
                doc.name.toLowerCase().includes(term) ||
                doc.specialization.toLowerCase().includes(term)
            );
            renderDoctorOptions(filtered);
        });
    }


    // --- ইউজার টেবিল সার্চ ---
    const userSearchInput = document.getElementById('user-table-search');
    if (userSearchInput) {
        userSearchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase().trim();
            const rows = document.querySelectorAll('#user-table-body tr');

            rows.forEach(row => {
                const text = row.innerText.toLowerCase();
                if (text.includes(term)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    }



    // ডাক্তার সিলেক্ট করলে যা হবে
    document.getElementById('doctor-selector')?.addEventListener('change', async (e) => {
        const doctorId = e.target.value;
        const configSection = document.getElementById('admin-schedule-config');
        if (!doctorId) {
            configSection.classList.add('hidden');
            return;
        }
        document.getElementById('selected-doc-name').innerText = e.target.options[e.target.selectedIndex].text;
        configSection.classList.remove('hidden');
        await fetchAndPopulateDoctorSchedule(doctorId);
    });
});


// function showSection(sectionId) {
//     document.querySelectorAll('main section').forEach(s => s.classList.add('hidden'));
//     const targetSection = document.getElementById(`${sectionId}-section`);
//     if (targetSection) {
//         targetSection.classList.remove('hidden');
//         document.getElementById('section-title').innerText = sectionId.replace(/-/g, ' ').toUpperCase();
//     }
// }


async function fetchDoctorsForSelector() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/doctors', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.status === 401) return handleLogout();
        allDoctors = await response.json();
        console.log("Doctors Loaded:", allDoctors);
        renderDoctorOptions(allDoctors);
    } catch (err) {
        console.error("Error loading doctors:", err);
    }
}

function renderDoctorOptions(doctors) {
    const selector = document.getElementById('doctor-selector');
    if (!selector) return;
    selector.innerHTML = '<option value="">-- Choose a Doctor --</option>' +
        doctors.map(doc => `<option value="${doc.id}">Dr. ${doc.name} (${doc.specialization})</option>`).join('');
}


function generateAdminScheduleSections() {
    const list = document.getElementById('admin-days-list');
    if (!list) return;
    list.innerHTML = days.map(day => `
        <div class="day-group bg-gray-50 p-4 rounded-xl border border-gray-200" data-day="${day}">
            <div class="flex justify-between items-center mb-4">
                <h4 class="font-bold text-blue-800 text-lg">${day}</h4>
                <button type="button" onclick="addAdminSession('${day}')" class="text-sm bg-blue-100 text-blue-600 px-3 py-1 rounded-lg hover:bg-blue-200 font-semibold">+ Add Session</button>
            </div>
            <div id="admin-sessions-${day}" class="space-y-3"></div>
        </div>
    `).join('');
}


function addAdminSession(day) {
    addAdminSessionWithData(day, { startTime: "09:00", endTime: "13:00", slotDuration: 20, maxPatients: 12, isActive: true });
}

function addAdminSessionWithData(day, data) {
    const container = document.getElementById(`admin-sessions-${day}`);
    if (!container) return;
    const sessionHtml = `
        <div class="session-row bg-white p-4 rounded-lg shadow-sm border border-gray-100 mb-3 relative">
            <button type="button" onclick="this.parentElement.remove()" class="absolute top-2 right-2 text-red-400 hover:text-red-600"><i class="fas fa-times-circle text-xl"></i></button>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-2">
                <input type="time" class="start-time p-2 border rounded-lg text-sm" value="${data.startTime.substring(0,5)}">
                <input type="time" class="end-time p-2 border rounded-lg text-sm" value="${data.endTime.substring(0,5)}">
                <input type="number" class="duration p-2 border rounded-lg text-sm" value="${data.slotDuration}">
                <input type="number" class="max-patients p-2 border rounded-lg text-sm" value="${data.maxPatients}">
            </div>
            <div class="mt-2"><label><input type="checkbox" class="is-active h-4 w-4" ${data.isActive ? 'checked' : ''}> Active</label></div>
        </div>`;
    container.insertAdjacentHTML('beforeend', sessionHtml);
}


// async function fetchAndPopulateDoctorSchedule(doctorId) {
//     try {
//         const res = await fetch(`http://localhost:8080/api/v1/appointments/schedule/${doctorId}`, {
//             headers: { 'Authorization': `Bearer ${token}` }
//         });
//         if (res.status === 401) return handleLogout();
//         const existingSchedule = res.ok ? await res.json() : [];
//         days.forEach(day => document.getElementById(`admin-sessions-${day}`).innerHTML = '');
//         if (existingSchedule && existingSchedule.length > 0) {
//             existingSchedule.forEach(slot => addAdminSessionWithData(slot.dayOfWeek, slot));
//         } else {
//             days.forEach(day => addAdminSession(day));
//         }
//     } catch (e) {
//         days.forEach(day => addAdminSession(day));
//     }
// }

async function fetchAndPopulateDoctorSchedule(doctorId) {
    try {
        const res = await fetch(`http://localhost:8080/api/admin/appointments/schedule/${doctorId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // আগে সব ক্লিনিং করে নিন
        days.forEach(day => {
            document.getElementById(`admin-sessions-${day}`).innerHTML = '';
        });

        if (res.ok) {
            const existingSchedule = await res.json();
            if (existingSchedule && existingSchedule.length > 0) {
                // ডাটা থাকলে সেটা দেখাবে (৪র্থ ছবির মতো)
                existingSchedule.forEach(slot => addAdminSessionWithData(slot.dayOfWeek, slot));
            } else {
                // ডাটা না থাকলে কিছুই করবে না, শুধু খালি কার্ড থাকবে (৩য় ছবির মতো)
                console.log("No existing schedule for this doctor.");
            }
        }
    } catch (e) {
        console.error("Fetch error:", e);
    }
}



async function saveScheduleByAdmin(e) {

    e.preventDefault();
    const doctorId = document.getElementById('doctor-selector').value;
    const scheduleData = [];
    document.querySelectorAll('#admin-days-list .day-group').forEach(group => {
        const day = group.getAttribute('data-day');
        group.querySelectorAll('.session-row').forEach(row => {
            if (row.querySelector('.is-active').checked) {
                scheduleData.push({
                    dayOfWeek: day,
                    startTime: row.querySelector('.start-time').value + ":00",
                    endTime: row.querySelector('.end-time').value + ":00",
                    slotDuration: parseInt(row.querySelector('.duration').value),
                    maxPatients: parseInt(row.querySelector('.max-patients').value),
                    isActive: true
                });
            }
        });
    });
    try {
        const response = await fetch(`http://localhost:8080/api/admin/appointments/schedule/${doctorId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(scheduleData)
        });
        if (response.ok) alert("Schedule Updated!"); else alert("Failed to update.");
    } catch (err) { alert("Server Error."); }
}


async function createDoctor(e) { }






async function fetchAllUsers() {
    const tableBody = document.getElementById('user-table-body');
    if (!tableBody) return;

    tableBody.innerHTML = `<tr><td colspan="4" class="text-center py-4"><i class="fas fa-spinner animate-spin"></i> Loading...</td></tr>`;

    try {
        // সঠিক URL: কন্ট্রোলারের সাথে মিলিয়ে /api/v1/users দেওয়া হলো
        const response = await fetch('http://localhost:8080/api/v1/users', {
            headers: {
                'Authorization': `Bearer ${token}`, // টোকেন অবশ্যই পাঠাতে হবে
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401) return handleLogout();

        if (!response.ok) throw new Error("Failed to fetch users");

        const users = await response.json();
        renderUserTable(users);
    } catch (err) {
        console.error("User fetch error:", err);
        tableBody.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-red-500">ইউজার লিস্ট লোড করা সম্ভব হয়নি।</td></tr>`;
    }
}


function renderUserTable(users) {
    const tableBody = document.getElementById('user-table-body');
    const loggedInAdminEmail = localStorage.getItem('adminEmail') || 'superadmin@hospital.com'; // আপনার স্টোরেজ অনুযায়ী

    tableBody.innerHTML = users.map(user => `
        <tr class="border-b hover:bg-gray-50 transition">
            <td class="p-4 font-medium text-gray-800">${user.name || 'N/A'}</td>
            <td class="p-4 text-gray-600">${user.email}</td>
            <td class="p-4">
                <span class="px-2 py-1 rounded-full text-xs font-bold ${getRoleClass(user.role)}">
                    ${user.role}
                </span>
            </td>
            <td class="p-4">
                ${(user.role === 'ADMIN' || user.role === 'ROLE_ADMIN' || user.email === loggedInAdminEmail)
        ? '<span class="text-gray-400 italic text-xs">Protected</span>'
        : `<button onclick="deleteUser(${user.id})" class="text-red-500 hover:text-red-700 transition">
                        <i class="fas fa-trash-alt"></i>
                       </button>`
    }
            </td>
        </tr>
    `).join('');
}


function getRoleClass(role) {
    switch(role) {
        case 'ADMIN': case 'ROLE_ADMIN': return 'bg-purple-100 text-purple-700';
        case 'DOCTOR': case 'ROLE_DOCTOR': return 'bg-blue-100 text-blue-700';
        default: return 'bg-green-100 text-green-700'; // Patients
    }
}


function showSection(sectionId) {
    // ১. সব মেইন সেকশন এবং ডিভ হাইড করা (Safe approach)
    // এটি main এর ভেতরের সব সরাসরি চাইল্ডকে হাইড করবে যাদের আইডি আছে
    document.querySelectorAll('main > section, main > div').forEach(s => {
        if (s.id) {
            s.classList.add('hidden');
        }
    });

    // ২. সিলেক্টেড সেকশন শো করা
    // logic: stats দিলে stats-section দেখাবে, pending-requests দিলে pending-requests-section দেখাবে
    const targetId = sectionId.includes('-section') ? sectionId : `${sectionId}-section`;
    const target = document.getElementById(targetId);

    if (target) {
        target.classList.remove('hidden');

        // টাইটেল ম্যাপ (আপনার আগের লিস্ট ঠিক আছে, শুধু নতুনটা যোগ করলাম)
        const titles = {
            'stats': 'Dashboard Overview',
            'add-doctor': 'Register New Doctor',
            'manage-schedules': 'Manage Schedules',
            'all-users': 'System Users',
            'pending-requests': 'Profile Update Requests', // নতুন টাইটেল
            'manage-notice': 'Notice Management'
        };

        const titleElement = document.getElementById('section-title');
        if (titleElement) {
            titleElement.innerText = titles[sectionId] || 'Dashboard';
        }

        // ডাটা লোড করা (আপনার আগের লজিকগুলো অক্ষুণ্ণ আছে)
        if(sectionId === 'all-users') fetchAllUsers();
        if(sectionId === 'pending-requests') loadPendingApprovals();
        if(sectionId === 'manage-notice') {
            document.getElementById('manage-notice-section').classList.remove('hidden');
            document.getElementById('section-title').innerText = "Notice Management";
            loadCurrentNotice();
        }
    }

    // ৩. সাইডবার একটিভ স্টাইল আপডেট (আপনার কোডটিই থাকছে)
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('bg-blue-800', 'border-blue-400');
        link.classList.add('border-transparent');

        const onclickAttr = link.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(sectionId)) {
            link.classList.add('bg-blue-800', 'border-blue-400');
            link.classList.remove('border-transparent');
        }
    });
}


async function deleteUser(id) {
    if (!confirm("আপনি কি নিশ্চিতভাবে এই ইউজারকে ডিঅ্যাক্টিভেট করতে চান?")) return;

    try {
        const response = await fetch(`http://localhost:8080/api/v1/users/${id}/deactivate`, {
            method: 'PATCH', // আপনার কন্ট্রোলারে @PatchMapping আছে
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            alert("ইউজার সফলভাবে ডিঅ্যাক্টিভেট করা হয়েছে।");
            fetchAllUsers(); // টেবিল রিফ্রেশ
        } else {
            alert("ডিঅ্যাক্টিভেট করতে সমস্যা হয়েছে।");
        }
    } catch (err) {
        console.error("Delete error:", err);
    }
}



function handleLogout() {
    localStorage.clear();
    alert("Session Expired!");
    window.location.href = "index.html";
}





async function updateDashboardStats() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/dashboard/summary', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();

            // HTML এর স্ট্যাট কার্ডগুলো আপডেট করা
            // এখানে ID গুলো আমরা নিচে HTML এ ঠিক করে নেব
            document.getElementById('stat-total-doctors').innerText = data.totalDoctors;
            document.getElementById('stat-total-patients').innerText = data.totalPatients;
            document.getElementById('stat-total-prescriptions').innerText = data.totalPrescriptions;
            document.getElementById('stat-today-appointments').innerText = data.totalAppointmentsToday;

            if(document.getElementById('stat-total-revenue')) {
                document.getElementById('stat-total-revenue').innerText = `৳ ${data.totalRevenue.toLocaleString()}`;
            }
        }
    } catch (err) {
        console.error("Error fetching dashboard summary:", err);
    }
}


async function updatePerformanceTable() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/dashboard/doctor-performance', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const performanceData = await response.json();
            console.log("Performance Data:", performanceData);
            // এখানে চাইলে চার্ট বা টেবিল দেখাতে পারেন (নিচে সাজেশন দিচ্ছি)
        }
    } catch (err) {
        console.error("Error fetching performance data:", err);
    }
}




async function loadAppointmentChart() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/dashboard/appointment-trends', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        const labels = data.map(item => item.date);
        const counts = data.map(item => item.count);

        const ctx = document.getElementById('appointmentChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Appointments',
                    data: counts,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4, // পয়েন্টের সাইজ
                    pointHoverRadius: 8, // হোভার করলে পয়েন্ট বড় হবে
                    pointHoverBackgroundColor: '#2563eb',
                    pointHoverBorderColor: '#fff',
                    pointHoverBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: {
                    duration: 2000, // ২ সেকেন্ড ধরে এনিমেশন হবে
                    easing: 'easeInOutQuart'
                },
                interaction: {
                    intersect: false, // মাউস লাইনের আশেপাশে নিলেই ডেটা দেখাবে
                    mode: 'index',
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1e293b', // Dark background
                        padding: 12,
                        titleFont: { size: 14, weight: 'bold' },
                        bodyFont: { size: 13 },
                        displayColors: false // ছোট কালার বক্সটি হাইড করবে
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.05)', drawBorder: false },
                        ticks: { stepSize: 1, color: '#64748b' }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#64748b' }
                    }
                }
            }
        });
    } catch (err) {
        console.error("Chart load error:", err);
    }
}




let currentAuditPage = 0;
let allAuditLogs = [];

async function loadAuditLogs(page = 0) {
    try {
        const response = await fetch(`http://localhost:8080/api/v1/admin/audit/global?page=${page}&size=15`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json();

        // চেক করছি ডাটা কি আসলেই অ্যারে কি না
        if (Array.isArray(data)) {
            allAuditLogs = data;
            renderAuditTable(allAuditLogs);
            document.getElementById('audit-page-num').innerText = `Page ${page + 1}`;
        } else {
            console.error("Data received is not an array:", data);
            allAuditLogs = []; // খালি অ্যারে সেট করে দিচ্ছি যাতে ফিল্টার এরর না দেয়
        }
    } catch (err) {
        console.error("Audit load error:", err);
        allAuditLogs = [];
    }
}

function renderAuditTable(logs) {
    const tbody = document.getElementById('audit-log-body');
    if (!tbody) return;

    // মেক সিওর logs একটা অ্যারে
    if (!Array.isArray(logs) || logs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="p-4 text-center text-gray-500">No logs found</td></tr>`;
        return;
    }

    tbody.innerHTML = logs.map(log => `
        <tr class="border-b hover:bg-blue-50/30 transition duration-150">
            <td class="p-4 font-medium text-gray-700 text-xs">${log.entity}</td>
            <td class="p-4">
                <span class="px-2 py-0.5 rounded-[4px] text-[10px] font-bold ${getActionClass(log.revisionType)}">
                    ${log.revisionType}
                </span>
            </td>
            <td class="p-4 text-gray-600 text-xs">${log.modifiedBy || 'System'}</td>
            <td class="p-4 text-gray-400 text-[11px]">${new Date(log.timestamp).toLocaleString()}</td>
        </tr>
    `).join('');
}


document.getElementById('audit-search')?.addEventListener('input', (e) => {
    const term = e.target.value.toLowerCase();

    // safe-check: allAuditLogs অ্যারে কি না
    if (Array.isArray(allAuditLogs)) {
        const filtered = allAuditLogs.filter(log =>
            (log.entity && log.entity.toLowerCase().includes(term)) ||
            (log.modifiedBy && log.modifiedBy.toLowerCase().includes(term))
        );
        renderAuditTable(filtered);
    }
});


function nextAuditPage() { currentAuditPage++; loadAuditLogs(currentAuditPage); }
function prevAuditPage() { if(currentAuditPage > 0) { currentAuditPage--; loadAuditLogs(currentAuditPage); } }

function getActionClass(type) {
    if (type === 'ADD') return 'bg-green-100 text-green-700';
    if (type === 'MOD') return 'bg-blue-100 text-blue-700';
    if (type === 'DEL') return 'bg-red-100 text-red-700';
    return 'bg-gray-100 text-gray-700';
}



async function loadDoctorPerformance() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/dashboard/doctor-performance', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        const labels = data.map(item => "Dr. " + item[0]);
        const counts = data.map(item => item[1]);

        new Chart(document.getElementById('doctorPerformanceChart'), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Prescriptions',
                    data: counts,
                    backgroundColor: [
                        'rgba(59, 130, 246, 0.7)',
                        'rgba(147, 51, 234, 0.7)',
                        'rgba(236, 72, 153, 0.7)',
                        'rgba(249, 115, 22, 0.7)'
                    ],
                    borderRadius: 8,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { display: false } },
                    x: { grid: { display: false } }
                }
            }
        });
    } catch (err) { console.error("Performance Chart Error:", err); }
}



async function loadMedicineAndDiagnosisCharts() {
    try {
        const res = await fetch('http://localhost:8080/api/v1/admin/dashboard/medicine-analytics', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();

        new Chart(document.getElementById('medicineDoughnutChart'), {
            type: 'doughnut',
            data: {
                labels: data.medicines.map(m => m.name || "Unknown"),
                datasets: [{
                    data: data.medicines.map(m => m.count),
                    backgroundColor: ['#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });

        new Chart(document.getElementById('diagnosisPieChart'), {
            type: 'pie',
            data: {
                labels: data.diagnosis.map(d => d.type),
                datasets: [{
                    data: data.diagnosis.map(d => d.count),
                    backgroundColor: ['#ef4444', '#3b82f6', '#f59e0b', '#6366f1', '#22c55e']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    } catch (err) { console.error("New Charts Error:", err); }
}



async function loadPendingApprovals() {
    const token = localStorage.getItem('accessToken');

    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/pending-profiles', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const requests = await response.json();
        const container = document.getElementById('pending-profiles-table');
        container.innerHTML = '';

        // ব্যাজ আপডেট (কয়টা রিকোয়েস্ট পেন্ডিং আছে)
        const badge = document.getElementById('request-badge');
        if(requests.length > 0) {
            badge.innerText = requests.length;
            badge.classList.remove('hidden');
        }

        requests.forEach(req => {
            container.innerHTML += `
                <tr class="border-b hover:bg-gray-50 align-top">
                    <td class="p-4">
                        <div class="font-bold text-blue-700 text-lg">${req.name}</div>
                        <div class="text-sm text-gray-500">Phone: ${req.phone}</div>
                        <div class="text-xs bg-blue-100 text-blue-600 inline-block px-2 py-1 rounded mt-1">ID: ${req.doctorId}</div>
                    </td>
                    <td class="p-4">
                        <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
                            <p><strong>Specialization:</strong> ${req.specialization}</p>
                            <p><strong>Degree:</strong> ${req.degree}</p>
                            <p><strong>Designation:</strong> ${req.designation}</p>
                            <p><strong>Experience:</strong> ${req.experienceYears} Years</p>
                            <p><strong>Fee:</strong> ৳${req.consultationFee}</p>
                            <p><strong>Room:</strong> ${req.roomNo}</p>
                        </div>
                        <div class="mt-2 text-xs text-gray-600 italic">
                            <strong>About:</strong> ${req.aboutDoctor || 'No bio provided'}
                        </div>
                    </td>
                    <td class="p-4 text-center">
                        <button onclick="approveDoctor(${req.id})" class="bg-green-600 hover:bg-green-700 text-white px-5 py-2 rounded-xl shadow-md transition transform hover:scale-105">
                            <i class="fas fa-check-circle mr-1"></i> Approve All
                        </button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        console.error("Error:", err);
    }
}

async function approveDoctor(requestId) {
    if(!confirm("Are you sure you want to approve this profile update?")) return;

    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch(`http://localhost:8080/api/v1/admin/approve-profile/${requestId}`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            alert("Doctor Profile Approved and Main Table Updated!");
            loadPendingApprovals();
            if(typeof fetchStats === 'function') fetchStats();
        } else {
            alert("Approval failed!");
        }
    } catch (error) {
        console.error("Error during approval:", error);
    }
}





async function postNotice() {
    const content = document.getElementById('noticeInput').value;
    const token = localStorage.getItem('accessToken');

    if (!content) return alert("Please enter notice content!");
    if (!token) {
        alert("Session expired. Please login again.");
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/add-notice', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            // JSON অবজেক্ট হিসেবে পাঠাচ্ছি
            body: JSON.stringify({ content: content })
        });

        if (response.ok) {
            alert("Notice Published successfully!");
            document.getElementById('noticeInput').value = '';
        } else if (response.status === 401 || response.status === 403) {
            alert("Access Denied! You must be an Admin.");
        } else {
            alert("Something went wrong. Status: " + response.status);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Server connection failed!");
    }
}




async function loadCurrentNotice() {
    const token = localStorage.getItem('accessToken');
    try {
        const response = await fetch('http://localhost:8080/api/v1/admin/current-notice', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const currentContent = await response.text();
            document.getElementById('noticeInput').value = currentContent;
        }
    } catch (error) {
        console.error("Error loading current notice:", error);
    }
}


