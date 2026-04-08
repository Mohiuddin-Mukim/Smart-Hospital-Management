const params = new URLSearchParams(window.location.search);
const doctorId = params.get('doctorId');
let selectedTime = null;

document.addEventListener('DOMContentLoaded', () => {
    if (!doctorId) {
        alert("Invalid Doctor Selection");
        window.location.href = 'doctors.html';
        return;
    }
    loadDoctorDetails();

    // তারিখ পরিবর্তন করলে স্লট লোড হবে
    document.getElementById('appointmentDate').addEventListener('change', (e) => {
        fetchAvailableSlots(e.target.value);
    });
});

// ডাক্তারের ডিটেইলস লোড (Summary Card এর জন্য)
async function loadDoctorDetails() {
    try {
        const res = await fetch(`http://localhost:8080/api/v1/doctors/${doctorId}`);
        const doc = await res.json();
        document.getElementById('doc-name').innerText = doc.name;
        document.getElementById('doc-spec').innerText = doc.specialization;
        document.getElementById('doc-fee').innerText = doc.consultationFee;
        if(doc.profilePictureUrl) {
            document.getElementById('doc-img').src = `http://localhost:8080${doc.profilePictureUrl}`;
        }
    } catch (e) { console.error("Error loading doctor"); }
}



// আপনার এন্ডপয়েন্ট: /api/v1/appointments/slots
// booking.js এর fetchAvailableSlots ফাংশনটি এভাবে আপডেট করুন
async function fetchAvailableSlots(date) {
    const container = document.getElementById('slots-container');
    container.innerHTML = `<p class="col-span-full text-blue-600 animate-pulse text-center">Checking sessions...</p>`;

    try {
        const token = localStorage.getItem('accessToken');
        const res = await fetch(`http://localhost:8080/api/v1/appointments/slots?doctorId=${doctorId}&date=${date}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const slots = await res.json();

        container.innerHTML = '';
        if (!slots || slots.length === 0) {
            container.innerHTML = `<p class="col-span-full text-red-500 text-sm text-center font-bold">No sessions available for this day.</p>`;
            return;
        }

        // আপনার DoctorAvailabilityDTO তে sessionName বা সময় অনুযায়ী গ্রুপ করা
        // আমরা এখানে সেশনগুলোকে ইউনিকভাবে দেখাবো
        const uniqueSessions = [...new Map(slots.map(item => [item.sessionName, item])).values()];

        uniqueSessions.forEach(session => {
            const availableCount = slots.filter(s => s.sessionName === session.sessionName && s.isAvailable).length;

            const div = document.createElement('div');
            div.className = `p-4 border-2 rounded-xl cursor-pointer transition flex justify-between items-center ${availableCount > 0 ? 'border-blue-100 hover:border-blue-500 bg-white' : 'bg-gray-100 opacity-60 cursor-not-allowed'}`;

            div.innerHTML = `
                <div>
                    <h5 class="font-bold text-gray-800">${session.sessionName || 'Consultation Session'}</h5>
                    <p class="text-xs text-gray-500">Available: <span class="text-blue-600 font-bold">${availableCount} slots left</span></p>
                </div>
                <i class="fas fa-chevron-right text-gray-300"></i>
            `;

            if (availableCount > 0) {
                div.onclick = () => {
                    document.querySelectorAll('#slots-container > div').forEach(d => d.classList.remove('border-blue-600', 'bg-blue-50'));
                    div.classList.add('border-blue-600', 'bg-blue-50');

                    // সেশনের প্রথম অ্যাভেইলেবল স্লটটি খুঁজে বের করা
                    const firstEmptySlot = slots.find(s => s.sessionName === session.sessionName && s.isAvailable);
                    selectedTime = firstEmptySlot.time;

                    document.getElementById('payBtn').disabled = false;
                    document.getElementById('payBtn').innerText = `Book Early Slot (${selectedTime.substring(0,5)}) & Pay`;
                };
            }
            container.appendChild(div);
        });
    } catch (e) {
        container.innerHTML = "Error loading sessions.";
    }
}




// বুকিং এবং পেমেন্ট সাবমিশন
document.getElementById('bookingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('payBtn');
    btn.innerText = "Processing...";
    btn.disabled = true;

    const bookingData = {
        doctorId: parseInt(doctorId),
        date: document.getElementById('appointmentDate').value,
        time: selectedTime,
        reason: document.getElementById('reason').value
    };

    try {
        const res = await fetch('http://localhost:8080/api/v1/appointments/book', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
            },
            body: JSON.stringify(bookingData)
        });

        const data = await res.json(); // AppointmentResponseDTO

        if (res.ok && data.paymentUrl) {
            // SSLCommerz গেটওয়েতে পাঠিয়ে দেওয়া
            window.location.href = data.paymentUrl;
        } else {
            alert(data.message || "Booking failed!");
            btn.disabled = false;
            btn.innerText = "Proceed to Payment";
        }
    } catch (err) {
        alert("Server error. Please try again.");
        btn.disabled = false;
    }
});