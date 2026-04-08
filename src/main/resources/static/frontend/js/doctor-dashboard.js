let selectedMedicinesList = [];
const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

document.addEventListener('DOMContentLoaded', () => {
    generateScheduleSections();
    fetchPendingRequests();
    fetchDailyAppointments();
    fetchDoctorInfo();

    const dateInput = document.getElementById('search-date-input');
    if(dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    const scheduleForm = document.getElementById('scheduleForm');
    if (scheduleForm) {
        scheduleForm.addEventListener('submit', saveScheduleByDoctor);
    }

    // প্রেসক্রিপশন ফর্ম সাবমিট হ্যান্ডলার
    const pForm = document.getElementById('prescriptionForm');
    if (pForm) {
        pForm.addEventListener('submit', function (e) {
            e.preventDefault();
            savePrescription();
        });
    }
});

async function callPatient(tokenId, appointmentId) {
    if (!tokenId) {
        alert("টোকেন জেনারেট হয়নি। পেশেন্টকে সিরিয়াল নিতে বলুন।");
        return;
    }

    if (!confirm("আপনি কি এই পেশেন্টকে কল করতে চান? এটি সিরিয়াল বোর্ডে আপডেট হবে।")) return;

    try {
        const response = await fetch(`http://localhost:8080/api/v1/doctors/tokens/${tokenId}/call`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
            }
        });

        if (response.ok) {
            // ১. কনসোলে বা UI তে ফিডব্যাক
            console.log(`Success: Patient with Token ${tokenId} called.`);

            // ২. প্রেসক্রিপশন মডাল ওপেন
            openPrescriptionModal(appointmentId);

            // ৩. টেবিল রিফ্রেশ (যাতে স্ট্যাটাস INSIDE দেখায়)
            fetchDailyAppointments();
        } else {
            const error = await response.text();
            alert("পেশেন্ট কল করা যায়নি: " + error);
        }
    } catch (error) {
        console.error("Error calling patient:", error);
        alert("সার্ভার কানেকশন এরর!");
    }
}

async function savePrescription() {
    const nextVisitValue = document.getElementById('p-next-visit').value;

    const prescriptionData = {
        appointmentId: parseInt(document.getElementById('modal-appointment-id').value),
        weight: document.getElementById('p-weight').value || "",
        bp: document.getElementById('p-bp').value || "",
        temperature: document.getElementById('p-temp').value || "",
        pulse: document.getElementById('p-pulse').value || "",
        diagnosis: document.getElementById('p-diagnosis').value || "",
        chiefComplaints: document.getElementById('p-complaints').value || "",

        // এখন সরাসরি ইনপুট থেকে ভ্যালু নেওয়া হচ্ছে
        clinicalFindings: document.getElementById('p-findings').value || "",
        advice: document.getElementById('p-advice').value || "",
        notes: document.getElementById('p-notes').value || "",

        // তারিখ থাকলে পাঠাবে, না থাকলে null
        nextVisitDate: nextVisitValue ? nextVisitValue : null,

        // মেডিসিন লিস্ট থেকে tempId বাদ দিয়ে পাঠানো
        medicines: selectedMedicinesList.map(({tempId, ...rest}) => rest)
    };

    console.log("Sending Data to Server:", prescriptionData);

    try {
        const res = await fetch('http://localhost:8080/api/v1/prescriptions/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
            },
            body: JSON.stringify(prescriptionData)
        });

        if (res.ok) {
            alert("✅ Prescription Saved Successfully!");
            location.reload();
        } else {
            const errorData = await res.json();
            console.error("Server Response Error:", errorData);
            alert("❌ Error: " + (errorData.error || "Bad Request - Check Console"));
        }
    } catch (error) {
        console.error("Connection Error:", error);
        alert("সার্ভারের সাথে কানেক্ট করা যাচ্ছে না।");
    }
}

async function searchMedicine(query) {
    const resultsDiv = document.getElementById('med-results');

    // ২ অক্ষরের কম হলে সার্চ করবে না
    if (query.length < 2) {
        resultsDiv.classList.add('hidden');
        return;
    }

    try {
        const token = localStorage.getItem('accessToken'); // টোকেনটি নিচ্ছি

        const res = await fetch(`http://localhost:8080/api/medicines/search?query=${query}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`, // যেহেতু .authenticated() দিয়েছেন, এটা মাস্ট
                'Content-Type': 'application/json'
            }
        });

        // যদি টোকেন এক্সপায়ার হয়ে যায় বা ভুল হয় (৪০১ এরর)
        if (res.status === 401) {
            console.error("Session expired or Unauthorized. Please login again.");
            return;
        }

        if (!res.ok) throw new Error("Search failed");

        const medicines = await res.json();

        // এরর হ্যান্ডলিং: medicines যদি array না হয় (নিরাপত্তার জন্য চেক)
        if (!Array.isArray(medicines)) {
            resultsDiv.classList.add('hidden');
            return;
        }

        resultsDiv.innerHTML = '';
        resultsDiv.classList.remove('hidden');

        medicines.forEach(med => {
            const div = document.createElement('div');
            div.className = "p-3 hover:bg-blue-50 cursor-pointer border-b text-sm";
            div.innerHTML = `<strong>${med.brandName}</strong> <span class="text-gray-500">(${med.genericName})</span>`;

            // ক্লিক করলে টেবিল এ অ্যাড হবে
            div.onclick = () => {
                addMedicineToTable(med);
                resultsDiv.classList.add('hidden'); // সিলেকশনের পর রেজাল্ট বক্স হাইড হবে
            };
            resultsDiv.appendChild(div);
        });

    } catch (error) {
        console.error("Medicine search error:", error);
    }
}

function addMedicineToTable(med) {
    const tableBody = document.getElementById('selected-medicines');
    const rowId = Date.now();

    const medObj = {
        brandId: med.brandId || med.id,
        brandName: med.brandName,
        medicineId: med.medicineId || 1,
        dosage: '',
        duration: '',
        instruction: '',
        days: 7,
        isContinued: false,
        tempId: rowId
    };

    const tr = document.createElement('tr');
    tr.id = `row-${rowId}`;
    tr.innerHTML = `
        <td class="p-2 text-sm font-bold">${med.brandName}</td>
        <td class="p-2"><input type="text" placeholder="1+0+1" class="border p-1 w-20 rounded text-xs" onchange="updateMed(${rowId}, 'dosage', this.value)"></td>
        <td class="p-2"><input type="text" placeholder="7 days" class="border p-1 w-20 rounded text-xs" onchange="updateMed(${rowId}, 'duration', this.value)"></td>
        <td class="p-2"><input type="text" placeholder="খাবার পর" class="border p-1 w-full rounded text-xs" onchange="updateMed(${rowId}, 'instruction', this.value)"></td>
        <td class="p-2 text-red-500 cursor-pointer" onclick="removeMed(${rowId})"><i class="fas fa-trash"></i></td>
    `;

    tableBody.appendChild(tr);
    selectedMedicinesList.push(medObj);

    document.getElementById('med-results').classList.add('hidden');
    document.getElementById('med-search').value = '';
}

function updateMed(id, field, value) {
    const item = selectedMedicinesList.find(m => m.tempId === id);
    if (item) item[field] = value;
}

function removeMed(id) {
    selectedMedicinesList = selectedMedicinesList.filter(m => m.tempId !== id);
    document.getElementById(`row-${id}`).remove();
}

function openPrescriptionModal(appointmentId) {
    document.getElementById('modal-appointment-id').value = appointmentId;
    document.getElementById('prescriptionModal').classList.remove('hidden');
}

function closePrescriptionModal() {
    document.getElementById('prescriptionModal').classList.add('hidden');
    document.getElementById('prescriptionForm').reset();
    selectedMedicinesList = [];
    document.getElementById('selected-medicines').innerHTML = '';
}

function getStatusClass(status) {
    const statusMap = {
        'PENDING': 'bg-yellow-100 text-yellow-700',
        'BOOKED': 'bg-green-100 text-green-700',
        'COMPLETED': 'bg-blue-100 text-blue-700',
        'CANCELLED': 'bg-red-100 text-red-700',
        'REJECTED': 'bg-gray-100 text-gray-700'
    };
    return statusMap[status] || 'bg-gray-100 text-gray-700';
}

function generateScheduleSections() {
    const list = document.getElementById('days-list');
    list.innerHTML = days.map(day => `
        <div class="day-group bg-gray-50 p-4 rounded-xl border border-gray-200" data-day="${day}">
            <div class="flex justify-between items-center mb-4">
                <h4 class="font-bold text-blue-800 text-lg">${day}</h4>
                <button type="button" onclick="addSession('${day}')" class="text-sm bg-blue-100 text-blue-600 px-3 py-1 rounded-lg hover:bg-blue-200 font-semibold">
                    + Add Session
                </button>
            </div>
            <div id="sessions-${day}" class="space-y-3"></div>
        </div>
    `).join('');

    // days.forEach(day => addSession(day));
    fetchAndPopulateMySchedule();
}


async function fetchAndPopulateMySchedule() {
    try {
        const res = await fetch(`http://localhost:8080/api/v1/doctor/appointments/schedule`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        if (res.ok) {
            const existingSchedule = await res.json();
            if (existingSchedule && existingSchedule.length > 0) {
                // ডাটা থাকলে লুপ চালিয়ে সেশনগুলো অ্যাড করা
                existingSchedule.forEach(slot => {
                    addSessionWithData(slot.dayOfWeek, slot);
                });
            }
        }
    } catch (e) {
        console.error("Error loading schedule:", e);
    }
}


function addSessionWithData(day, data) {
    const container = document.getElementById(`sessions-${day}`);
    const sessionHtml = `
        <div class="session-row bg-white p-4 rounded-lg shadow-sm border border-gray-100 mb-3 relative">
            <button type="button" onclick="this.parentElement.remove()" class="absolute top-2 right-2 text-red-400 hover:text-red-600">
                <i class="fas fa-times-circle text-xl"></i>
            </button>
            <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mt-2">
                <input type="time" class="start-time p-2 border rounded-lg text-sm" value="${data.startTime.substring(0,5)}">
                <input type="time" class="end-time p-2 border rounded-lg text-sm" value="${data.endTime.substring(0,5)}">
                <input type="number" class="duration p-2 border rounded-lg text-sm" value="${data.slotDuration}">
                <input type="number" class="max-patients p-2 border rounded-lg text-sm" value="${data.maxPatients}">
                <div class="flex items-end">
                    <input type="checkbox" class="is-active" ${data.isActive ? 'checked' : ''}> Active
                </div>
            </div>
        </div>`;
    container.insertAdjacentHTML('beforeend', sessionHtml);
}







function addSession(day) {
    const container = document.getElementById(`sessions-${day}`);
    const sessionHtml = `
        <div class="session-row bg-white p-4 rounded-lg shadow-sm border border-gray-100 mb-3 relative">
            <button type="button" onclick="this.parentElement.remove()" class="absolute top-2 right-2 text-red-400 hover:text-red-600">
                <i class="fas fa-times-circle text-xl"></i>
            </button>

            <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mt-2">
                <input type="time" class="start-time p-2 border rounded-lg text-sm" value="09:00">
                <input type="time" class="end-time p-2 border rounded-lg text-sm" value="13:00">
                <input type="number" class="duration p-2 border rounded-lg text-sm" value="20">
                <input type="number" class="max-patients p-2 border rounded-lg text-sm" value="12">
                <div class="flex items-end">
                    <input type="checkbox" class="is-active" checked>
                </div>
            </div>
        </div>
    `;
    container.insertAdjacentHTML('beforeend', sessionHtml);
}

async function fetchPendingRequests() {
    try {
        const res = await fetch('http://localhost:8080/api/v1/doctor/appointments/requests', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        const requests = await res.json();
        document.getElementById('request-count').innerText = `${requests.length} New`;

        const tableBody = document.getElementById('pending-requests-table');

        if (requests.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">No new requests</td></tr>`;
            return;
        }

        tableBody.innerHTML = requests.map(app => `
            <tr class="border-b hover:bg-gray-50 transition" id="request-row-${app.id}">
                <td class="p-4 font-medium text-gray-700">${app.patientName}</td>
                <td class="p-4 text-gray-600">${app.date} ${app.time.substring(0, 5)}</td>
                <td class="p-4 text-gray-600 italic text-sm">${app.reason || 'N/A'}</td>
                <td class="p-4 flex justify-center gap-2">
                    <button onclick="handleStatusUpdate(${app.id}, 'BOOKED')" 
                            class="bg-green-500 text-white px-3 py-1 rounded-lg text-sm font-bold hover:bg-green-600 transition shadow-sm">
                        Confirm
                    </button>
                    <button onclick="handleStatusUpdate(${app.id}, 'REJECTED')" 
                            class="bg-red-500 text-white px-3 py-1 rounded-lg text-sm font-bold hover:bg-red-600 transition shadow-sm">
                        Reject
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) { console.error("Error fetching requests:", e); }
}

async function fetchDailyAppointments() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const localDate = `${year}-${month}-${day}`;

    try {
        const res = await fetch(`http://localhost:8080/api/v1/doctor/appointments/daily?date=${localDate}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        const appointments = await res.json();
        renderAppointmentTable(appointments);
    } catch (e) { console.error(e); }
}

async function fetchAppointmentsBySelectedDate() {
    const selectedDate = document.getElementById('search-date-input').value;

    if (!selectedDate) {
        alert("দয়া করে একটি তারিখ সিলেক্ট করুন");
        return;
    }

    const tableBody = document.getElementById('filtered-appointment-table');
    // লোডিং স্টেট দেখানো
    tableBody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-blue-500"><i class="fas fa-spinner fa-spin mr-2"></i> Loading...</td></tr>`;

    try {
        const res = await fetch(`http://localhost:8080/api/v1/doctor/appointments/daily?date=${selectedDate}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        if (!res.ok) throw new Error("Fetch failed");

        const appointments = await res.json();
        renderFilteredTable(appointments); // নতুন টেবিল রেন্ডারার কল করা
    } catch (e) {
        console.error("Error:", e);
        tableBody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-red-500">Error fetching data. Try again.</td></tr>`;
    }
}

function renderFilteredTable(list) {
    const tableBody = document.getElementById('filtered-appointment-table');

    if (list.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">ওই তারিখে কোনো অ্যাপয়েন্টমেন্ট পাওয়া যায়নি।</td></tr>`;
        return;
    }

    tableBody.innerHTML = list.map(app => `
        <tr class="border-b hover:bg-gray-50 transition">
            <td class="p-4">
                <div class="font-medium text-gray-800">${app.patientName}</div>
                <div class="text-xs text-gray-400">ID: #${app.patientId}</div>
            </td>
            <td class="p-4 text-gray-600 font-semibold">${app.time.substring(0, 5)}</td>
            <td class="p-4">
                <span class="px-3 py-1 rounded-full text-xs font-bold ${getStatusClass(app.status)}">
                    ${app.status}
                </span>
            </td>
            <td class="p-4 text-gray-500 text-sm italic">
                ${app.reason || 'No reason specified'}
            </td>
        </tr>
    `).join('');
}



function renderAppointmentTable(list) {
    const tableBody = document.getElementById('doctor-appointment-table');

    if (list.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="4" class="p-10 text-center text-gray-400">No appointments for today.</td></tr>`;
        return;
    }

    tableBody.innerHTML = list.map(app => `
        <tr class="border-b hover:bg-gray-50 transition">
            <td class="p-4 font-medium">${app.patientName}</td>
            <td class="p-4 text-gray-600">${app.time.substring(0, 5)}</td>
            <td class="p-4">
                <span class="px-2 py-1 rounded-full text-xs font-bold ${getStatusClass(app.status)}">
                    ${app.status}
                </span>
            </td>
            <td class="p-4 text-center">
                ${(app.status === 'BOOKED' && app.tokenId) ? `
                    <button onclick="callPatient(${app.tokenId}, ${app.id})" 
                            class="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-blue-700 shadow-md transition flex items-center gap-2 mx-auto">
                        <i class="fas fa-bullhorn"></i> Call
                    </button>
                ` : `
                    <span class="text-gray-400 text-xs italic">Not Ready</span>
                `}
            </td>
        </tr>
    `).join('');
}



async function handleStatusUpdate(appointmentId, status) {
    const confirmMsg = status === 'BOOKED' ? "Confirm this appointment?" : "Reject this appointment? (Refund will be initiated)";

    if (!confirm(confirmMsg)) return;

    try {
        const response = await fetch(`http://localhost:8080/api/v1/doctor/appointments/${appointmentId}/status?status=${status}`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });

        if (response.ok) {
            alert(`Appointment ${status === 'BOOKED' ? 'confirmed' : 'rejected'} successfully!`);

            // লিস্ট থেকে রো-টি রিমুভ করা
            const row = document.getElementById(`request-row-${appointmentId}`);
            if (row) row.remove();

            // রিকোয়েস্ট কাউন্ট আপডেট করা
            fetchPendingRequests();
            // ডেইল লিস্ট আপডেট করা (যদি কনফার্ম হয়ে থাকে)
            fetchDailyAppointments();
        } else {
            const errorMsg = await response.text();
            alert("Error: " + errorMsg);
        }
    } catch (error) {
        console.error("Status update failed:", error);
        alert("Failed to update status. Check console for details.");
    }
}





async function saveScheduleByDoctor(e) {
    e.preventDefault();

    const scheduleData = [];
    const token = localStorage.getItem('accessToken');

    // প্রতিটি দিনের ডাটা লুপ করে সংগ্রহ করা
    document.querySelectorAll('#days-list .day-group').forEach(group => {
        const day = group.getAttribute('data-day');
        group.querySelectorAll('.session-row').forEach(row => {
            const isActiveCheckbox = row.querySelector('.is-active');

            // যদি সেশনটি একটিভ থাকে তবেই ডাটা পুশ করবে
            if (isActiveCheckbox && isActiveCheckbox.checked) {
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

    if (scheduleData.length === 0) {
        alert("Please add at least one active session.");
        return;
    }

    try {
        // এখানে URL আপনার ডাক্তারের জন্য নির্দিষ্ট এপিআই এন্ডপয়েন্ট হতে হবে
        // সাধারণত: /api/v1/doctor/schedule (আপনার ব্যাকেন্ড অনুযায়ী পরিবর্তন করুন)
        const response = await fetch(`http://localhost:8080/api/v1/doctor/appointments/schedule`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(scheduleData)
        });

        if (response.ok) {
            alert("✅ Weekly Schedule Updated Successfully!");
        } else {
            const errorText = await response.text();
            alert("❌ Failed to update schedule: " + errorText);
        }
    } catch (err) {
        console.error("Schedule Update Error:", err);
        alert("Server Error. Please check if the backend is running.");
    }
}



function showSection(sectionId) {
    // ১. সব সেকশন হাইড করা
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.add('hidden');
    });

    // ২. কাঙ্ক্ষিত সেকশন শো করা
    document.getElementById(sectionId).classList.remove('hidden');

    // ৩. সাইডবার মেনু হাইলাইট আপডেট করা
    const navLinks = document.querySelectorAll('aside nav a');
    navLinks.forEach(link => {
        link.classList.remove('bg-blue-600', 'text-white');
        link.classList.add('text-gray-600', 'hover:bg-blue-50');
    });

    // ক্লিক করা লিঙ্কে নীল রঙ দেওয়া
    const activeLink = (sectionId === 'appointment-section') ?
        document.getElementById('nav-appointments') :
        document.getElementById('nav-schedule');

    activeLink.classList.add('bg-blue-600', 'text-white');
    activeLink.classList.remove('text-gray-600', 'hover:bg-blue-50');
}


async function fetchDoctorInfo() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        // ১. ডাক্তারের প্রোফাইল থেকে নাম নিয়ে আসা
        const profileRes = await fetch('http://localhost:8080/api/v1/doctors/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const doctorData = await profileRes.json();

        // ড্যাশবোর্ডে নাম দেখানো
        document.getElementById('doc-name-display').innerText = doctorData.name;

        // ২. আজকের অ্যাপয়েন্টমেন্ট এবং কমপ্লিটেড ডাটা ফেচ করা
        const today = new Date().toISOString().split('T')[0];
        const appRes = await fetch(`http://localhost:8080/api/v1/doctor/appointments/daily?date=${today}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const appointments = await appRes.json();

        // ক্যালকুলেশন
        const totalToday = appointments.length;
        const completedToday = appointments.filter(app => app.status === 'COMPLETED').length;

        // কার্ডে ডাটা বসানো
        document.getElementById('today-app-count').innerText = String(totalToday).padStart(2, '0');
        document.getElementById('completed-app-count').innerText = String(completedToday).padStart(2, '0');

    } catch (error) {
        console.error("Error updating dashboard summary:", error);
    }
}



async function loadProfileData() {
    // আপনার স্টোরেজে 'accessToken' নামে সেভ করা আছে
    const token = localStorage.getItem('accessToken');
    if (!token) {
        console.error("No token found!");
        return;
    }

    showSection('profile-section');

    try {
        const response = await fetch('http://localhost:8080/api/v1/doctors/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Server Error Details:", errorText);
            throw new Error(`Status: ${response.status}`);
        }

        const doctor = await response.json();

        // ইনপুট ফিল্ডগুলোতে ডাটা বসানো (Null হলে ফাঁকা থাকবে)
        document.getElementById('update-docName').value = doctor.name || '';
        document.getElementById('update-docSpecialization').value = doctor.specialization || '';
        document.getElementById('update-docDegree').value = doctor.degree || '';
        document.getElementById('update-docDesignation').value = doctor.designation || '';
        document.getElementById('update-docPhone').value = doctor.phone || '';
        document.getElementById('update-docRoom').value = doctor.roomNo || '';
        document.getElementById('update-docFee').value = doctor.consultationFee || 0;
        document.getElementById('update-docExperience').value = doctor.experienceYears || 0;
        document.getElementById('update-docAbout').value = doctor.aboutDoctor || '';

        // ইমেজের অংশ হ্যান্ডেল করা
        const profileImgElement = document.getElementById('profile-img');
        if (profileImgElement) {
            if (doctor.profilePictureUrl && doctor.profilePictureUrl.trim() !== "") {
                profileImgElement.src = doctor.profilePictureUrl;
            } else {
                // যেহেতু আপনার কাছে লোকাল ইমেজ নেই, তাই এই অনলাইন ইমেজটি ডিফল্ট হিসেবে কাজ করবে
                profileImgElement.src = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(doctor.name || 'Doctor') + '&background=random&size=150';
            }
        }

    } catch (error) {
        console.error('Error loading profile:', error);
        alert('আপনার প্রোফাইল লোড করা সম্ভব হয়নি। সার্ভার বা ইন্টারনেট সংযোগ চেক করুন।');
    }
}

document.getElementById('updateProfileForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const token = localStorage.getItem('accessToken');
    if (!token) {
        alert("Session expired. Please login again.");
        return;
    }

    const formData = new FormData();
    const dto = {
        name: document.getElementById('update-docName').value,
        specialization: document.getElementById('update-docSpecialization').value,
        degree: document.getElementById('update-docDegree').value,
        designation: document.getElementById('update-docDesignation').value,
        phone: document.getElementById('update-docPhone').value,
        roomNo: document.getElementById('update-docRoom').value,
        consultationFee: document.getElementById('update-docFee').value,
        experienceYears: document.getElementById('update-docExperience').value,
        aboutDoctor: document.getElementById('update-docAbout').value
    };

    // DTO অংশটি JSON হিসেবে পাঠানো
    formData.append('dto', new Blob([JSON.stringify(dto)], { type: "application/json" }));

    // ইমেজ ফাইল থাকলে সেটা পাঠানো
    const fileInput = document.getElementById('update-docImage');
    if (fileInput && fileInput.files[0]) {
        formData.append('file', fileInput.files[0]);
    }

    try {
        const response = await fetch('http://localhost:8080/api/v1/doctors/profile-update', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
                // FormData ব্যবহার করলে Content-Type দিতে হয় না
            },
            body: formData
        });

        if (response.ok) {
            alert('Profile updated successfully!');
            location.reload();
        } else {
            const errorMsg = await response.text();
            alert('Update failed: ' + errorMsg);
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        alert('An error occurred during update.');
    }
});