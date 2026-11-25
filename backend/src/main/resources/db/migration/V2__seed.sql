INSERT INTO resource (name, type, unit, unit_price) VALUES
    ('Ciment',            'MATERIAL',  'sac',   28000.00),
    ('Sable',             'MATERIAL',  'm3',    45000.00),
    ('Gravier',           'MATERIAL',  'm3',    60000.00),
    ('Maçon',             'LABOUR',    'heure',  5000.00),
    ('Manœuvre',          'LABOUR',    'heure',  2500.00),
    ('Bétonnière',        'EQUIPMENT', 'heure', 12000.00),
    ('Pelle hydraulique', 'EQUIPMENT', 'heure', 90000.00);

INSERT INTO work_item (name, unit) VALUES
    ('Béton de fondation',      'm3'),
    ('Enduit de façade',        'm2'),
    ('Terrassement en fouille', 'm3'),
    ('Dallage béton 10 cm',     'm2');

INSERT INTO work_item_resource (work_item_id, resource_id, quantity)
SELECT w.id, r.id, v.quantity
FROM (VALUES
    ('Béton de fondation',      'Ciment',            7.000),
    ('Béton de fondation',      'Sable',             0.500),
    ('Béton de fondation',      'Gravier',           0.800),
    ('Béton de fondation',      'Maçon',             3.000),
    ('Béton de fondation',      'Bétonnière',        1.500),

    ('Enduit de façade',        'Ciment',            0.250),
    ('Enduit de façade',        'Sable',             0.030),
    ('Enduit de façade',        'Maçon',             0.600),
    ('Enduit de façade',        'Manœuvre',          0.400),

    ('Terrassement en fouille', 'Pelle hydraulique', 0.150),
    ('Terrassement en fouille', 'Manœuvre',          0.500),

    ('Dallage béton 10 cm',     'Ciment',            0.700),
    ('Dallage béton 10 cm',     'Sable',             0.050),
    ('Dallage béton 10 cm',     'Gravier',           0.080),
    ('Dallage béton 10 cm',     'Maçon',             0.400),
    ('Dallage béton 10 cm',     'Manœuvre',          0.400),
    ('Dallage béton 10 cm',     'Bétonnière',        0.150)
) AS v (work_item_name, resource_name, quantity)
JOIN work_item w ON w.name = v.work_item_name
JOIN resource  r ON r.name = v.resource_name;
