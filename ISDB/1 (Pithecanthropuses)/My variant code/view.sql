CREATE OR REPLACE VIEW v_band_statistics AS
SELECT 
	b.name AS band_name,
	COUNT(p.id) AS member_count,
	ROUND(AVG(p.weight)::numeric, 2) AS avg_weight,
	ROUND(AVG(p.height)::numeric, 2) AS avg_height
FROM band AS b
LEFT join pithecanthropus AS p ON p.band_id = b.id
GROUP BY b.name;
