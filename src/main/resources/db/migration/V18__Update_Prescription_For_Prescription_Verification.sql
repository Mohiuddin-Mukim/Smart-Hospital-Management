-- ১. কলামটি যোগ করা
ALTER TABLE prescriptions ADD COLUMN verification_token VARCHAR(255);

-- ২. পুরনো ডাটাগুলোর জন্য টোকেন জেনারেট করা (আপনার যদি অনেক ডাটা থাকে)
UPDATE prescriptions SET verification_token = UUID() WHERE verification_token IS NULL;

-- ৩. কলামটিকে ইউনিক এবং নট নাল করা
ALTER TABLE prescriptions MODIFY verification_token VARCHAR(255) NOT NULL;
ALTER TABLE prescriptions ADD CONSTRAINT uc_verification_token UNIQUE (verification_token);