-- ১. জেনেরিক নামের লেংথ বাড়ানো
ALTER TABLE medicines
    MODIFY COLUMN generic_name VARCHAR(500) NOT NULL;

-- ২. ব্র্যান্ড নামের লেংথ বাড়ানো
ALTER TABLE medicine_brands
    MODIFY COLUMN brand_name VARCHAR(500) NOT NULL;

-- ৩. ডেসক্রিপশন এবং অন্যান্য টেক্সট ফিল্ডের জন্য সেফটি
ALTER TABLE medicines
    MODIFY COLUMN description TEXT;