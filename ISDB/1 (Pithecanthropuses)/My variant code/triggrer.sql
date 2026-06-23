CREATE OR REPLACE TRIGGER trg_check_pithec_size
BEFORE INSERT OR UPDATE ON pithecanthropus
FOR EACH ROW
EXECUTE FUNCTION check_pithec_size();
