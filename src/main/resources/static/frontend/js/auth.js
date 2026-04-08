const BASE_URL = "http://localhost:8080/api/v1/auth";

// --- ১. পাসওয়ার্ড দেখা বা লুকানোর লজিক (লগইন ফর্মের বাইরে থাকবে) ---
const togglePassword = document.querySelector('#togglePassword');
const passwordField = document.querySelector('#password');
const eyeIcon = document.querySelector('#eyeIcon');

if (togglePassword) {
    togglePassword.addEventListener('click', function () {
        const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordField.setAttribute('type', type);
        eyeIcon.classList.toggle('fa-eye');
        eyeIcon.classList.toggle('fa-eye-slash');
    });
}

// --- ২. লগইন সাবমিট লজিক ---
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const loginBtn = document.getElementById('loginBtn');

    loginBtn.innerText = "Processing...";
    loginBtn.disabled = true;

    try {
        const response = await fetch(`${BASE_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('userRole', data.role);
            localStorage.setItem('userId', data.userId);

            // ডিবাগিং এর জন্য কনসোলে রোলটি দেখে নিন
            console.log("Logged in user role:", data.role);

            // স্মার্ট রোল চেক (case-insensitive এবং ROLE_ প্রিফিক্স হ্যান্ডেল করা)
            const role = data.role.toUpperCase();

            if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
                window.location.href = 'admin-dashboard.html';
            } else if (role === 'ROLE_DOCTOR' || role === 'DOCTOR') {
                window.location.href = 'doctor-dashboard.html';
            } else {
                // পেশেন্টদের জন্য আলাদা ড্যাশবোর্ড থাকলে সেটা দেবেন, নাহলে index.html
                //window.location.href = 'index.html';
                window.location.href = 'patient-dashboard.html'
            }
        } else {
            alert(data.message || 'Login failed! Please check credentials.');
        }
    } catch (error) {
        alert('Server connection failed!');
    } finally {
        loginBtn.innerText = "Login";
        loginBtn.disabled = false;
    }
});


// --- ৩. সাইনআপ সাবমিট লজিক ---
document.getElementById('signupForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const signupBtn = document.getElementById('signupBtn');
    const formData = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        password: document.getElementById('password').value,
        age: parseInt(document.getElementById('age').value),
        gender: document.getElementById('gender').value,
        address: document.getElementById('address').value
    };

    signupBtn.innerText = "Creating Account...";
    signupBtn.disabled = true;

    try {
        const response = await fetch(`${BASE_URL}/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Registration Successful! Please login.');
            window.location.href = 'login.html';
        } else {
            const errorText = await response.text();
            alert(errorText || 'Signup failed! Check your data.');
        }
    } catch (error) {
        alert('Server error occurred.');
    } finally {
        signupBtn.innerText = "Register Now";
        signupBtn.disabled = false;
    }
});