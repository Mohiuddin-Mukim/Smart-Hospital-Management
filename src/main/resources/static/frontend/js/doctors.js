const API_URL = "http://localhost:8080/api/v1/doctors";
let allDoctors = []; // সব ডাক্তারদের ডেটা সেভ করে রাখার জন্য

document.addEventListener('DOMContentLoaded', () => {
    fetchDoctors();
});

// ডাক্তারদের ডেটা ফেচ করা
async function fetchDoctors() {
    const container = document.getElementById('doctorList');
    try {
        const response = await fetch(API_URL);
        allDoctors = await response.json();

        if (response.ok) {
            renderDoctors(allDoctors);
        } else {
            container.innerHTML = `<p class="col-span-full text-center text-red-500">তথ্য লোড করতে সমস্যা হয়েছে।</p>`;
        }
    } catch (error) {
        console.error("Error:", error);
        container.innerHTML = `<p class="col-span-full text-center text-red-500">সার্ভার কানেকশন এরর!</p>`;
    }
}

// কার্ড রেন্ডার করা
// ... আগের fetchDoctors ফাংশন একই থাকবে ...

function renderDoctors(doctors) {
    const container = document.getElementById('doctorList');
    container.innerHTML = '';

    doctors.forEach(doc => {
        // ১. ডাটাবেস থেকে আসা অরিজিনাল পাথটি আগে ভেরিয়েবলে নিন
        let rawPath = doc.profilePictureUrl;

        // ২. ডিবাগিং: কনসোলে দেখুন ডাটাবেস থেকে আসলে কী আসছে
        console.log("Doctor Name:", doc.name, "| Raw Path:", rawPath);

        // ৩. পাথ চেক এবং ইউআরএল তৈরি
        let imageUrl;
        if (rawPath) {
            // যদি পাথে অলরেডি http থাকে তবে সেটা ধরবে, না থাকলে লোকাল হোস্ট যোগ করবে
            imageUrl = rawPath.startsWith('http') ? rawPath : `http://localhost:8080${rawPath}`;
        } else {
            imageUrl = 'https://cdn-icons-png.flaticon.com/512/3774/3774299.png';
        }

        // ৪. স্পেস বা স্পেশাল ক্যারেক্টার হ্যান্ডেল করার জন্য encodeURI (ঐচ্ছিক কিন্তু নিরাপদ)
        const finalImageUrl = encodeURI(imageUrl);
        console.log("Final Computed URL:", finalImageUrl);

        const card = `
            <div onclick="openDoctorDetails(${doc.id})" class="cursor-pointer bg-white rounded-2xl shadow-md hover:shadow-2xl transition duration-300 overflow-hidden border border-gray-100 group">
                <div class="relative h-64 overflow-hidden bg-gray-200">
                    <img src="${finalImageUrl}" 
                         alt="${doc.name}" 
                         onerror="this.src='https://cdn-icons-png.flaticon.com/512/3774/3774299.png'"
                         class="w-full h-full object-cover group-hover:scale-110 transition duration-500">
                    <div class="absolute top-4 right-4 bg-blue-600 text-white px-3 py-1 rounded-full text-xs font-bold shadow-lg">
                        ${doc.experienceYears || 0} Years Exp.
                    </div>
                </div>
                <div class="p-6">
                    <h3 class="text-xl font-bold text-gray-900">${doc.name}</h3>
                    <p class="text-blue-600 font-medium mt-1">${doc.specialization}</p>
                    <div class="mt-6 pt-4 border-t border-gray-100 flex justify-between items-center">
                        <div>
                            <span class="text-sm text-gray-500 block">Consultation Fee</span>
                            <span class="text-lg font-bold text-gray-900">৳ ${doc.consultationFee}</span>
                        </div>
                        <button onclick="event.stopPropagation(); bookAppointment(${doc.id})" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition font-semibold">
                            Book Now
                        </button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += card;
    });
}

// মোডাল ওপেন করার ফাংশন
async function openDoctorDetails(id) {
    const modal = document.getElementById('doctorModal');
    const content = document.getElementById('modalContent');
    if(!modal) return;

    modal.classList.remove('hidden');
    content.innerHTML = `<div class="text-center py-10"><i class="fas fa-spinner animate-spin text-3xl text-blue-600"></i></div>`;

    try {
        const res = await fetch(`${API_URL}/${id}`);
        const doc = await res.json();

        // 🔥 ইমেজ পাথ ফিক্স (এখানেও)
        const img = doc.profilePicture ? `http://localhost:8080${doc.profilePicture}` : 'https://cdn-icons-png.flaticon.com/512/3774/3774299.png';

        content.innerHTML = `
            <div class="flex flex-col md:flex-row gap-8">
                <img src="${img}" class="w-48 h-48 rounded-2xl object-cover shadow-md border-4 border-white">
                <div class="flex-1">
                    <h2 class="text-3xl font-bold text-gray-900">${doc.name}</h2>
                    <p class="text-blue-600 font-semibold text-lg">${doc.specialization}</p>
                    <div class="mt-4 space-y-2 text-gray-600">
                        <p><strong>Designation:</strong> ${doc.designation || 'Specialist'}</p>
                        <p><strong>Degree:</strong> ${doc.degree}</p>
                        <p><strong>Experience:</strong> ${doc.experienceYears} Years</p>
                    </div>
                </div>
            </div>
            <div class="mt-8">
                <h4 class="font-bold text-gray-800 border-b pb-2 mb-4">About Doctor</h4>
                <p class="text-gray-600 leading-relaxed">${doc.aboutDoctor || 'No details provided yet.'}</p>
            </div>
            <div class="mt-8 flex justify-between items-center bg-blue-50 p-6 rounded-2xl">
                <div>
                    <p class="text-sm text-gray-500">Fees</p>
                    <p class="text-2xl font-bold text-blue-900">৳ ${doc.consultationFee}</p>
                </div>
                <button onclick="bookAppointment(${doc.id})" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-blue-700 transition shadow-lg">
                    Book Appointment
                </button>
            </div>
        `;
    } catch (e) {
        content.innerHTML = "<p class='text-red-500'>Error loading doctor details.</p>";
    }
}

// মোডাল বন্ধ করা
function closeModal() {
    document.getElementById('doctorModal').classList.add('hidden');
}

// সার্চ ফিল্টার লজিক
document.getElementById('doctorSearch')?.addEventListener('input', (e) => {
    const term = e.target.value.toLowerCase();
    const filtered = allDoctors.filter(doc =>
        doc.name.toLowerCase().includes(term) ||
        doc.specialization.toLowerCase().includes(term)
    );
    renderDoctors(filtered);
});

// বুকিং রিডাইরেক্ট
function bookAppointment(doctorId) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        alert('অ্যাপয়েন্টমেন্ট বুক করতে আগে লগইন করুন।');
        window.location.href = 'login.html';
        return;
    }
    window.location.href = `booking.html?doctorId=${doctorId}`;
}