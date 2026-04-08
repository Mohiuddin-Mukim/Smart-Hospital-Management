document.addEventListener('DOMContentLoaded', () => {
    fetchMyAppointments();
});

async function fetchMyAppointments() {
    const listContainer = document.getElementById('patient-appointment-list');
    try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch('http://localhost:8080/api/v1/appointments/my', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Failed to fetch");

        let appointments = await response.json();

        // সর্টিং
        appointments.sort((a, b) => {
            const dateA = new Date(a.date + 'T' + apptTime(a.time));
            const dateB = new Date(b.date + 'T' + apptTime(b.time));
            return dateB - dateA;
        });

        document.getElementById('total-count').innerText = appointments.length;

        if (appointments.length === 0) {
            listContainer.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">No appointments found.</td></tr>`;
            return;
        }

        // ১. আগে টেবিল রেন্ডার করুন
        renderTable(appointments);

        // ২. তারপর লাইভ সিরিয়ালের জন্য চেক করুন (টেবিল মোছার দরকার নেই)
        initLiveQueue(appointments);

    } catch (error) {
        listContainer.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-red-400">Error: ${error.message}</td></tr>`;
    }
}

function apptTime(t) { return t.includes(':') ? t : t + ":00"; }

function renderTable(list) {
    const container = document.getElementById('patient-appointment-list');
    const today = new Date().toISOString().split('T')[0];

    container.innerHTML = list.map(app => {
        const isToday = app.date === today;
        const canCancel = (app.status === 'PENDING') && (new Date(app.date) >= new Date().setHours(0,0,0,0));

        return `
        <tr class="border-b hover:bg-gray-50 transition">
            <td class="p-4">
                <p class="font-bold text-gray-800">${app.doctorName}</p>
                <p class="text-xs text-blue-600 font-semibold uppercase">${app.specialization || 'General'}</p>
            </td>
            <td class="p-4 text-sm text-gray-700">
                ${app.date} <br> <span class="font-bold text-gray-400">${app.time.substring(0,5)}</span>
            </td>
            <td class="p-4">
                <span class="px-3 py-1 rounded-full text-[10px] font-bold uppercase ${getStatusClass(app.status)}">
                    ${app.status}
                </span>
            </td>
            <td class="p-4">
                <div class="flex items-center justify-center gap-3">
        ${(isToday && (app.status === 'BOOKED' || app.status === 'PAID')) ? `
            <button onclick="showTokenDetails(${app.id})" class="text-orange-500 hover:text-orange-700" title="View Token">
                <i class="fas fa-ticket-alt text-xl"></i>
            </button>
        ` : ''}
        
        <button onclick="viewReceipt(${app.id})" class="text-emerald-500 hover:text-emerald-700" title="View Receipt">
            <i class="fas fa-eye text-xl"></i>
        </button>

        <button onclick="downloadReceipt(${app.id})" class="text-blue-600 hover:text-blue-800" title="Download Receipt">
            <i class="fas fa-file-pdf text-xl"></i>
        </button>

        ${canCancel ? `
            <button onclick="cancelAppointment(${app.id})" class="text-red-400 hover:text-red-600" title="Cancel">
                <i class="fas fa-trash-alt text-lg"></i>
            </button>
        ` : ''}
    </div>
            </td>
        </tr>`;
    }).join('');
}

let stompClient = null;
let subscribedDocs = new Set();

function connectToQueue(docId) {
    // অলরেডি এই ডাক্তারের কিউতে সাবস্ক্রাইব করা থাকলে আর নতুন করে কানেক্ট করবে না
    if (subscribedDocs.has(docId)) return;

    const socket = new SockJS('http://localhost:8080/ws-queue');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    // কানেক্ট করার চেষ্টা
    stompClient.connect({}, function (frame) {
        console.log('Connected to Queue for Doctor: ' + docId);
        subscribedDocs.add(docId); // সাকসেস হলে সেটে অ্যাড হবে

        // 🔥 সমাধান: কানেক্ট হওয়ার ঠিক পরেই সাবস্ক্রাইব করুন
        stompClient.subscribe(`/topic/doctor/${docId}`, function (message) {
            const currentSerial = message.body;
            console.log("New Serial Received via WS:", currentSerial);
            updateLiveDisplay(currentSerial);
        });
    }, function(error) {
        console.error("STOMP connection error:", error);
        // কানেকশন ফেইল করলে সেট থেকে বাদ দিন যেন পরে আবার ট্রাই করতে পারে
        subscribedDocs.delete(docId);
    });
}

function initLiveQueue(appointments) {
    const today = new Date().toISOString().split('T')[0];

    appointments.forEach(appt => {
        if (appt.date === today && (appt.status === 'BOOKED' || appt.status === 'PAID')) {
            console.log("Initializing Live Queue for Doctor ID:", appt.doctorId);

            loadLiveServingNumber(appt.id);

            connectToQueue(appt.doctorId);
        }
    });
}



async function showTokenDetails(appointmentId) {
    try {
        const res = await fetch(`http://localhost:8080/api/v1/patients/${appointmentId}/token-status`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });
        const tokenData = await res.json();

        // এখানে আপনি চাইলে একটি ছোট পপআপ বা এলার্ট দেখাতে পারেন
        alert(`আপনার সিরিয়াল নম্বর: ${tokenData.serialNo}\nবর্তমান স্ট্যাটাস: ${tokenData.status}`);
    } catch (e) {
        console.error("Error fetching token status", e);
    }
}



function updateLiveDisplay(serial) {
    const display = document.getElementById('live-serial-display');
    if(display) {
        display.innerText = serial.padStart(2, '0'); // '8' কে '08' দেখাবে

        // ভিজ্যুয়াল হাইলাইট
        display.classList.remove('text-orange-600');
        display.classList.add('text-green-600', 'scale-110');

        setTimeout(() => {
            display.classList.remove('text-green-600', 'scale-110');
            display.classList.add('text-orange-600');
        }, 1000);
    }
}

function isToday(dateString) {
    return dateString === new Date().toISOString().split('T')[0];
}

function getStatusClass(status) {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-700';
        case 'BOOKED':
        case 'PAID': return 'bg-green-100 text-green-700 border border-green-200';
        case 'CANCELLED':
        case 'REJECTED': return 'bg-red-100 text-red-700';
        case 'COMPLETED': return 'bg-blue-100 text-blue-700';
        default: return 'bg-gray-100 text-gray-700';
    }
}

async function cancelAppointment(appointmentId) {
    if (!confirm("আপনি কি নিশ্চিত যে এই অ্যাপয়েন্টমেন্টটি বাতিল করতে চান?")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/v1/appointments/${appointmentId}/cancel`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        if (res.ok) {
            alert("Appointment Cancelled successfully.");
            fetchMyAppointments();
        } else {
            alert("বাতিল করা সম্ভব হয়নি।");
        }
    } catch (e) { alert("Error connecting to server."); }
}

async function downloadReceipt(appointmentId) {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`http://localhost:8080/api/v1/payments/${appointmentId}/receipt`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || "Failed to download receipt");
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Receipt_Appointment_${appointmentId}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    } catch (error) {
        console.error("Download Error:", error);
        alert("রিসিট ডাউনলোড করা সম্ভব হয়নি: " + error.message);
    }
}


async function viewReceipt(appointmentId) {
    if (!appointmentId) {
        alert("Appointment ID পাওয়া যায়নি!");
        return;
    }
    try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`http://localhost:8080/api/v1/payments/${appointmentId}/receipt`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || "Failed to load receipt");
        }

        // পিডিএফ ব্লব তৈরি এবং প্রিভিউ ওপেন
        const blob = await response.blob();
        const file = new Blob([blob], { type: 'application/pdf' });
        const fileURL = URL.createObjectURL(file);

        // নতুন ট্যাবে প্রিভিউ দেখাবে
        window.open(fileURL, '_blank');

        // মেমোরি ক্লিনআপ
        setTimeout(() => URL.revokeObjectURL(fileURL), 10000);

    } catch (error) {
        console.error("View Error:", error);
        alert("রিসিট দেখা সম্ভব হয়নি: " + error.message);
    }
}







async function loadLiveServingNumber(appointmentId) {
    try {
        const response = await fetch(`http://localhost:8080/api/v1/patients/${appointmentId}/live-status`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            const data = await response.json();
            const display = document.getElementById('live-serial-display');

            // ডাটাবেসে currentlyServing এ যা আছে তা এখানে দেখাবে
            display.innerText = data.currentlyServing > 0 ? data.currentlyServing.toString().padStart(2, '0') : "--";
        }
    } catch (error) {
        console.error("Error loading live status:", error);
    }
}



function showPrescriptions() {
    // অন্য সেকশন হাইড করে এটি দেখাবে
    document.getElementById('appointment-section').classList.add('hidden');
    document.querySelector('section').classList.add('hidden'); // অ্যাপয়েন্টমেন্ট টেবিল হাইড
    document.getElementById('prescription-section').classList.remove('hidden');

    fetchPrescriptions();
}

function showAppointments() {
    document.getElementById('prescription-section').classList.add('hidden');
    document.getElementById('appointment-section').classList.remove('hidden');
}

async function fetchPrescriptions() {
    const tbody = document.getElementById('prescription-list-body');
    tbody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">Loading prescriptions...</td></tr>`;

    try {
        const token = localStorage.getItem('accessToken');
        // আমরা অ্যাপয়েন্টমেন্ট লিস্ট থেকেই COMPLETED গুলো আলাদা করতে পারি
        // অথবা আলাদা API থাকলে সেটি কল করতে পারি।
        const res = await fetch('http://localhost:8080/api/v1/appointments/my', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const appointments = await res.json();
        const completedOnes = appointments.filter(app => app.status === 'COMPLETED');

        if (completedOnes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">No prescriptions found yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = completedOnes.map(app => `
            <tr class="hover:bg-gray-50 transition">
                <td class="p-4 font-bold text-gray-800">${app.doctorName}</td>
                <td class="p-4 text-sm text-gray-600">${app.date}</td>
                <td class="p-4 text-sm italic text-gray-500">${app.reason || 'General Checkup'}</td>
                <td class="p-4 text-center">
                    <div class="flex items-center justify-center gap-2">
                        <button onclick="viewPrescription(${app.prescriptionId})" 
                            class="bg-emerald-500 text-white px-3 py-2 rounded-lg text-xs font-bold hover:bg-emerald-600 shadow-md transition flex items-center gap-1">
                        <i class="fas fa-eye"></i> View
                        </button>
                    
                        <button onclick="downloadPrescription(${app.prescriptionId})" 
                            class="bg-blue-600 text-white px-3 py-2 rounded-lg text-xs font-bold hover:bg-blue-700 shadow-md transition flex items-center gap-1">
                        <i class="fas fa-download"></i> Download
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-red-400">Error loading data.</td></tr>`;
    }
}

async function downloadPrescription(prescriptionId) {
    console.log("Downloading Prescription ID:",prescriptionId);
    if(!prescriptionId){
        alert("Prescription ID পাওয়া যায়নি!");
        return;
    }
    try {
        const token = localStorage.getItem('accessToken');
        // আপনার কন্ট্রোলারের পাথ: /api/v1/prescriptions/{id}/download
        // দ্রষ্টব্য: এখানে {id} মানে অ্যাপয়েন্টমেন্ট আইডি বা প্রেসক্রিপশন আইডি যা আপনার সার্ভার রিসিভ করে
        const response = await fetch(`http://localhost:8080/api/v1/prescriptions/${prescriptionId}/download`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Could not download the prescription.");

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Prescription_Appt_${prescriptionId}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    } catch (error) {
        console.error("Download error:", error);
        alert("Prescription ডাউনলোড করা সম্ভব হয়নি। সম্ভবত এখনো জেনারেট হয়নি।");
    }
}



async function viewPrescription(prescriptionId) {
    if (!prescriptionId) {
        alert("Prescription ID পাওয়া যায়নি!");
        return;
    }
    try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`http://localhost:8080/api/v1/prescriptions/${prescriptionId}/download`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Could not load the prescription.");

        // এখানে আমরা 'application/pdf' ব্লব তৈরি করছি
        const blob = await response.blob();
        const file = new Blob([blob], { type: 'application/pdf' });

        // ফাইলটি একটি URL হিসেবে তৈরি করে নতুন ট্যাবে ওপেন করছি
        const fileURL = URL.createObjectURL(file);
        window.open(fileURL, '_blank');

        // মেমোরি ক্লিনআপ (অপশনাল কিন্তু ভালো প্র্যাকটিস)
        setTimeout(() => URL.revokeObjectURL(fileURL), 10000);

    } catch (error) {
        console.error("View error:", error);
        alert("Prescription দেখা সম্ভব হয়নি।");
    }
}