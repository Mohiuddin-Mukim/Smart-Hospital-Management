ALTER TABLE doctors ADD COLUMN is_profile_verified TINYINT(1) DEFAULT 1;
-- ১ মানে ভেরিফাইড (এডমিন যখন বানাবে তখন অটো ভেরিফাইড থাকবে)
-- ০ মানে পেন্ডিং (যখন ডাক্তার নিজে এডিট করবে তখন ০ হয়ে যাবে)