CREATE DATABASE IF NOT EXISTS le_juste_accord;
USE le_juste_accord;

-- Crée la table si Hibernate ne l'a pas encore fait
CREATE TABLE IF NOT EXISTS accords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle_fr VARCHAR(255) NOT NULL,
    libelle_en VARCHAR(255) NOT NULL,
    couleur VARCHAR(255) NOT NULL,
    valeur INT NOT NULL
);

-- Insère les 12 notes de la gamme chromatique
INSERT INTO accords (libelle_fr, libelle_en, couleur, valeur) VALUES
('Do', 'C', '#EF4444', 1),
('Do#', 'C#', '#F43F5E', 2),
('Ré', 'D', '#F97316', 3),
('Ré#', 'D#', '#FBBF24', 4),
('Mi', 'E', '#F59E0B', 5),
('Fa', 'F', '#10B981', 6),
('Fa#', 'F#', '#06B6D4', 7),
('Sol', 'G', '#3B82F6', 8),
('Sol#', 'G#', '#6366F1', 9),
('La', 'A', '#8B5CF6', 10),
('La#', 'A#', '#D946EF', 11),
('Si', 'B', '#EC4899', 12);