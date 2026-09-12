insert into Band (name, area) values
('Fierce Fangs', 'Eastern Valley'),
('Wise Rocks', 'Mountain Range'),
('Swift Feet', 'Western Plains'),
('Fiery Hearts', 'Northern Caves');

insert into Invention (name, weight) values
('club', 3.2),
('saw', 1.5),
('horn dagger', 0.6),
('bone scraper', 0.4),
('stone axe', 2.5),
('spear', 1.8),
('bow', 0.9),
('stone knife', 0.4);

insert into Action (name, start, finish) values
('mammoth hunt', '4712-03-15 06:00:00', '4712-03-15 14:00:00'),
('weapon crafting', '4712-03-15 09:00:00', '4712-03-15 12:00:00'),
('berry gathering', '4712-03-15 08:00:00', '4712-03-15 11:00:00'),
('fire making', '4712-03-15 07:00:00', '4712-03-15 08:00:00'),
('shelter building', '4711-03-16 06:00:00', '4711-03-16 18:00:00'),
('fishing', '4711-03-16 05:00:00', '4711-03-16 09:00:00'),
('youth training', '4711-03-16 10:00:00', '4711-03-16 14:00:00'),
('territory defense', '4710-03-17 00:00:00', '4710-03-17 06:00:00'),
('food preparation', '4710-03-17 12:00:00', '4710-03-17 14:00:00'),
('clothing making', '4710-03-17 09:00:00', '4710-03-17 15:00:00'),
('ritual dance', '4710-03-17 20:00:00', '4710-03-17 23:00:00'),
('healing wounded', '4709-03-18 08:00:00', '4709-03-18 12:00:00');

insert into Pithecanthropus (name, band_id, weight, height, sex) values
('Grisha', 1, 85.5, 175, 'male'),
('Misha', 1, 92.0, 182, 'male'),
('Leha', 1, 78.3, 168, 'male'),
('Xenia', 1, 76.8, 170, 'female'),
('Jack', 1, 80.5, 188, 'male'),
('Yarik', 2, 88.7, 179, 'male'),
('Oleg', 2, 81.2, 172, 'male'),
('Sahsa', 2, 95.1, 185, 'male'),
('Harmony', 2, 91.7, 175, 'female'),
('Shelly', 2, 74.2, 169, 'female'),
('Vlad', 3, 72.4, 165, 'male'),
('Amanda', 3, 77.6, 171, 'female'),
('Clara', 3, 70.1, 166, 'female'),
('Steph', 3, 83.4, 176, 'female'),
('Kevin', 3, 79.8, 183, 'male'),
('Cooper', 4, 86.2, 198, 'male'),
('Sidney', 4, 94.5, 174, 'male'),
('Laura', 4, 90.3, 170, 'female'),
('Jimmy', 4, 89.3, 177, 'male'),
('Robert', 4, 68.9, 174, 'male');

insert into Invention_type (invention_id, applicability, material, suggested_sex_of_user) values
(1, 'heavy combat', 'oak wood', 'male'),
(1, 'light training', 'pine wood', 'male'),
(1, 'ceremonial display', 'carved wood', 'male'),
(2, 'tree felling', 'stone, oak', 'male'),
(2, 'precision carving', 'flint, pine', 'male'),
(2, 'ritual cutting', 'obsidian', 'male'),
(3, 'close combat', 'mammoth tusk', 'male'),
(3, 'hunting tool', 'deer antler', 'male'),
(3, 'status symbol', 'polished bone', 'male'),
(4, 'hide processing', 'whale bone', 'female'),
(4, 'delicate scraping', 'fish bone', 'female'),
(4, 'ritual cleaning', 'polished bone', 'female'),
(5, 'heavy chopping', 'granite, oak', 'male'),
(5, 'wood carving', 'basalt, pine', 'male'),
(5, 'sacrificial tool', 'limestone', 'male'),
(6, 'big game hunt', 'ash wood, flint', 'male'),
(6, 'river fishing', 'bamboo, bone', 'male'),
(6, 'territory marking', 'decorated shaft', 'male'),
(7, 'small game hunt', 'yew wood', 'male'),
(7, 'target practice', 'willow wood', 'male'),
(7, 'festival decoration', 'painted wood', 'male'),
(8, 'butchering meat', 'flint', 'all'),
(8, 'crafting materials', 'obsidian', 'all'),
(8, 'ritual offering', 'ceremonial stone', 'all');

insert into Symboled_band_invention (type, band_id, invention_id) values
('strength', 1, 1), ('war', 1, 1),
('hunt', 1, 6), ('blood', 1, 6),
('fire', 1, 2), ('light', 1, 2),
('wisdom', 2, 4), ('patience', 2, 4),
('craft', 2, 5), ('endurance', 2, 5),
('protection', 2, 3), ('bravery', 2, 3),
('speed', 3, 7), ('wind', 3, 7),
('agility', 3, 4), ('grace', 3, 4),
('survival', 3, 8), ('life', 3, 8),
('fire', 4, 2), ('warmth', 4, 2),
('unity', 4, 1), ('clan', 4, 1),
('hunt', 4, 6), ('food', 4, 6);

insert into Invention_pithec_action (pithec_id, invention_id, action_id) values
(1, 1, 1), (2, 6, 1), (3, 5, 2), (5, 7, 3), (6, 3, 4),
(7, 8, 5), (8, 2, 6), (11, 1, 7), (15, 6, 8), (16, 5, 9),

(4, 4, 10), (9, 8, 9), (10, 4, 11), (12, 8, 12), (13, 4, 10),
(14, 8, 11), (18, 4, 12), (4, 8, 1), (9, 4, 2), (10, 8, 3),

(4, 1, 1),
(1, 4, 2),  
(9, 5, 3),   
(16, 4, 4),  
(12, 6, 5);  
