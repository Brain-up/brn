ALTER TABLE public.user_account DROP COLUMN doctor_id;

CREATE TABLE IF NOT EXISTS doctor_patient_mpg (
	doctor_id int8 NOT NULL,
	patient_id int8 NOT NULL,
	CONSTRAINT doctor_patient_mpg_pkey PRIMARY KEY (doctor_id, patient_id)
);
ALTER TABLE doctor_patient_mpg ADD CONSTRAINT doctor_patient_mpg_doctor_id FOREIGN KEY (doctor_id) REFERENCES user_account(id);
ALTER TABLE doctor_patient_mpg ADD CONSTRAINT doctor_patient_mpg_patient_id FOREIGN KEY (patient_id) REFERENCES user_account(id);
