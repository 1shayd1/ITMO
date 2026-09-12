CREATE OR REPLACE FUNCTION remove_mismatched_gender_value_function()
RETURNS TRIGGER AS $$
DECLARE
	pithec_sex varchar(6);
	suggested_sex varchar(6);
BEGIN
	SELECT sex INTO pithec_sex
	FROM Pithecanthropus
	WHERE id = NEW.pithec_id;

	SELECT suggested_sex_of_user INTO suggested_sex
	FROM Invention i
	JOIN Invention_type it ON
	it.invention_id = i.id
	WHERE i.id = NEW.invention_id
	LIMIT 1;

	IF pithec_sex != suggested_sex AND pithec_sex != 'all' AND suggested_sex != 'all' THEN 
		INSERT INTO ipaLog(
			deleted_action,
			deleted_pithec,
			deleted_invention,
			deleted_pithec_sex,
			suggested_sex,
			reason
		)
		VALUES (
			NEW.action_id,
			NEW.pithec_id,
			NEW.invention_id,
			pithec_sex,
			suggested_sex,
			'Sex of pithecanthropus does not match the recommended one.'
		);

		RAISE NOTICE 'This "insert" operation does not match the rule: Pithecanthropus.sex = Invention_id.suggested_sex_of_user.';
		RETURN NULL;
	END IF;
	RETURN NEW;
END;

$$ LANGUAGE plpgsql;
