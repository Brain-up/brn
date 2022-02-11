ALTER TABLE public.user_account DROP COLUMN doctor_id;

CREATE TABLE IF NOT EXISTS patients_doctors (
	doctor_id int8 NOT NULL,
	patient_id int8 NOT NULL,
	CONSTRAINT patients_doctors_pkey PRIMARY KEY (doctor_id, patient_id)
);
ALTER TABLE patients_doctors ADD CONSTRAINT patients_doctors_doctor_id FOREIGN KEY (doctor_id) REFERENCES user_account(id);
ALTER TABLE patients_doctors ADD CONSTRAINT patients_doctors_patient_id FOREIGN KEY (patient_id) REFERENCES user_account(id);
