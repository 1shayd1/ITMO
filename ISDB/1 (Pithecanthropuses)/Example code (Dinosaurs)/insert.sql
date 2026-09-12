insert into dino_class (id, class) values
    (default, 'birds'),
    (default, 'reptiles');

insert into dino (id, name, weight, height, class_id) values
    (default, 'pterodactyl', 6.70, 1.88, 1),
    (default, 'microrapror', 0.68, 0.70, 1),
    (default, 'archaeopteryx', 0.55, 0.44, 1),
    (default, 'pteranodon', 23.80, 5.21, 1),
    (default, 'quetzalcoatl', 230.21, 10.50, 1),
    (default, 'pteranodon', 22.10, 6.15, 1),
    (default, 'archaeopteryx', 0.73, 0.70, 1),

    (default, 'stegosaurus', 4050.63, 4.67, 2),
    (default, 'triceratops', 6786.14, 3.15, 2),
    (default, 'diplodocus', 12039.85, 4.86, 2),
    (default, 'tyrannosaurus rex', 8923.57, 3.69, 2),
    (default, 'stegosaurus', 4732.42, 4.13, 2),
    (default, 'triceratops', 7946.13, 2.90, 2),
    (default, 'diplodocus', 10041.85, 5.31, 2),
    (default, 'tyrannosaurus rex', 7182.53, 4.42, 2);

insert into location (id, name, obstacles) values
    (default, 'hell creek formation', 'badlands'),
    (default, 'yixian formation', 'volcanos'),
    (default, 'patagonia', 'strong wind');

insert into fossils (id, name, location_id, dino_id) values
    (default, 'tooth', 1,15),
    (default, 'sculp', 2,4),
    (default, 'wing', 3,3),
    (default, 'spine', 1,5),
    (default, 'leg', 1,12),
    (default, 'wing', 3,2),
    (default, 'wing', 1,7),
    (default, 'tooth', 2,14),
    (default, 'leg', 1,13),
    (default, 'arm', 3,11),
    (default, 'spine', 1,10),
    (default, 'spine', 2,9),
    (default, 'wing', 3,6),
    (default, 'leg', 2,8),
    (default, 'arm', 2,8),
    (default, 'spine', 1,5),
    (default, 'spine', 2,4),
    (default, 'tooth', 3,3),
    (default, 'tooth', 3,2),
    (default, 'arm', 1,12);

insert into paleo (id, name, type) values
    (default, 'Greg', 'ornithologist'),
    (default, 'Jimmy', 'reptelologist'),
    (default, 'Steph', 'documentarian'),
    (default, 'Kevin', 'reptelologist'),
    (default, 'Dray', 'teamlead');

insert into paleo_actions (id, name, start, "end", paleo_id) values
    (default, 'dig', '2024-08-15 08:00:00', '2024-08-15 09:30:00', 1),
    (default, 'clean', '2024-08-15 09:00:00', '2024-08-15 09:15:00', 2),
    (default, 'dig', '2024-08-15 09:00:00', '2024-08-15 10:45:00', 4),
    (default, 'cheetmeal', '2024-08-15 11:00:00', '2024-08-15 12:00:00', 5),
    (default, 'dive on bottom', '2024-08-15 12:25:00', '2024-08-15 16:52:00', 4),
    (default, 'documetation', '2024-08-15 18:00:00', '2024-08-15 20:00:00', 3),

    (default, 'dig', '2024-08-16 07:00:00', '2024-08-16 09:26:00', 1),
    (default, 'clean', '2024-08-16 08:36:00', '2024-08-16 09:15:00', 4),
    (default, 'dive on bottom', '2024-08-16 09:00:00', '2024-08-16 10:45:00', 4),
    (default, 'cheetmeal', '2024-08-16 12:00:00', '2024-08-16 13:00:00', 5),
    (default, 'dive on bottom', '2024-08-16 14:56:00', '2024-08-16 17:13:00', 2),
    (default, 'documetation', '2024-08-16 19:00:00', '2024-08-16 21:30:00', 3);

insert into discovery (id, actions_id, fossil_id, dollars_gained) values
    (default, 1, 1, 15),
    (default, 2, 2, 150),
    (default, 3, 3, 20),
    (default, 5, 4, 0),
    (default, 6, 5, 1100),
    (default, 7, 6, 325),
    (default, 8, 7, 400),
    (default, 9, 8, 800),
    (default, 11, 9, 934),
    (default, 12, 10, 1000),
    (default, 1, 11, 10),
    (default, 2, 12, 265),
    (default, 3, 13, 775),
    (default, 5, 14, 374),
    (default, 6, 15, 985),
    (default, 7, 16, 123),
    (default, 8, 17, 15),
    (default, 9, 18, 13),
    (default, 11, 19, 1),
    (default, 12, 20, 111);