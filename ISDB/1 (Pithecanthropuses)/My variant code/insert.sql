insert into Band (name, area) values
('Fierce Fangs', 'Eastern Valley'),
('Wise Rocks', 'Mountain Range'),
('Swift Feet', 'Western Plains'),
('Fiery Hearts', 'Northern Caves'),
('Silent Hunters', 'Southern Forest'),
('Steel Teeth', 'Riverside Zone'),
('Ancient Stones', 'Central Plateau');

insert into Invention (name, weight) values
('club', 3.2),
('saw', 1.5),
('horn dagger', 0.6),
('bone scraper', 0.4),
('stone axe', 2.5),
('spear', 1.8),
('bow', 0.9),
('stone knife', 0.4);

insert into Pithecanthropus (name, band_id, weight, height) values
('Grisha', 1, 85.5, 175),
('Misha', 1, 92.0, 182),
('Leha', 1, 78.3, 168),
('Yarik', 2, 88.7, 179),
('Oleg', 2, 81.2, 172),
('Sahsa', 2, 95.1, 185),
('Vlad', 3, 72.4, 165),
('Xenia', 3, 76.8, 170),
('Jack', 3, 80.5, 188),
('Jimmy', 4, 89.3, 177),
('Robert', 4, 68.9, 174),
('Harmony', 4, 91.7, 175),
('Shelly', 5, 74.2, 169),
('Amanda', 5, 77.6, 171),
('Clara', 5, 70.1, 166),
('Steph', 6, 83.4, 176),
('Kevin', 6, 79.8, 183),
('Cooper', 6, 86.2, 198),
('Sidney', 7, 94.5, 174),
('Laura', 7, 90.3, 170);

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

insert into Pithec_action (pithec_id, action_id) values
(1, 1), (2, 1), (3, 1),
(4, 2), (5, 2),
(6, 3), (7, 3), (8, 3),
(9, 4), (10, 4),
(11, 5), (12, 5), (13, 5), (14, 5),
(15, 6), (16, 6),
(17, 7), (18, 7),
(19, 8), (20, 8), (1, 8),
(2, 9), (3, 9), (4, 9),
(5, 10), (6, 10),
(7, 11), (8, 11), (9, 11),
(10, 12), (11, 12), (12, 12);

insert into Invention_in_action (action_id, invention_id) values
(1, 5), (1, 6), (1, 1),
(2, 5), (2, 8), (2, 4),
(3, 6), (3, 8),
(4, 1), (4, 7),
(5, 5), (5, 4), (5, 7),
(6, 3), (6, 8),
(7, 6), (7, 1),
(8, 6), (8, 7),
(9, 8), (9, 6),
(10, 3), (10, 7),
(11, 1), (11, 7),
(12, 8), (12, 3);

insert into Invention_type (invention_id, applicability, material, suggested_sex_of_user) values
(1, 'close combat, blunt weapon', 'wood', 'male'),
(2, 'cutting wood, processing materials', 'stone, wood', 'male'),
(3, 'close combat, piercing weapon', 'bone', 'male'),
(4, 'scraping hides, cleaning bones', 'bone', 'female'),
(5, 'chopping trees, butchering', 'stone, wood', 'male'),
(6, 'hunting large game', 'wood, stone', 'male'),
(7, 'hunting small game at distance', 'wood, fibers', 'male'),
(8, 'butchering meat, cutting materials', 'stone', 'all');

insert into Symboled_band_invention (type, band_id, invention_id) values
('strength', 1, 5),      
('strength', 7, 1),      
('hunt', 1, 6),          
('hunt', 4, 6),          
('wisdom', 2, 4),        
('wisdom', 5, 8),        
('speed', 3, 7),         
('speed', 6, 3),         
('fire', 4, 2),          
('fire', 1, 1),          
('protection', 2, 1),    
('protection', 7, 7);    
