CREATE OR REPLACE FUNCTION check_pithec_size()
RETURNS TRIGGER AS $$
BEGIN
	IF NEW.height < 100 THEN
		RAISE NOTICE 'Предупреждение: у питекантропа % слишком маленький рост (%). Поднимаем до 100.', NEW.name, NEW.height;	 	
		NEW.height := 100;
	END IF;

	IF NEW.weight < 20.5 THEN
		RAISE NOTICE 'Предупреждение: у % недовес (%). Ставим 20.5 кг.', NEW.name, NEW.weight;
		NEW.weight := 20.5;
	ELSIF NEW.weight > 100.0 THEN
		RAISE NOTICE 'Предупреждение: % слишком жирный (%). Срезаем до 100 кг.', NEW.name, NEW.weight;
		NEW.weight := 100;
	END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;
