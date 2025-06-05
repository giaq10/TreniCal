DROP TABLE IF EXISTS biglietti;
DROP TABLE IF EXISTS viaggi;
DROP TABLE IF EXISTS promozioni;
DROP TABLE IF EXISTS clienti;

-- TABELLA CLIENTI
CREATE TABLE IF NOT EXISTS clienti (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    abbonamento_fedelta BOOLEAN DEFAULT 0,
    data_registrazione DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- TABELLA VIAGGI
CREATE TABLE IF NOT EXISTS viaggi (
    id VARCHAR(255) PRIMARY KEY,
    codice_treno VARCHAR(50) NOT NULL,
    tipo_treno VARCHAR(20) NOT NULL CHECK (tipo_treno IN ('ECONOMY', 'STANDARD', 'BUSINESS')),
    stazione_partenza VARCHAR(100) NOT NULL,
    stazione_arrivo VARCHAR(100) NOT NULL,
    data_viaggio TEXT NOT NULL,
    orario_partenza TEXT NOT NULL,
    orario_arrivo TEXT NOT NULL,
    data_arrivo TEXT NOT NULL,
    prezzo DECIMAL(10,2) NOT NULL,
    durata_minuti INTEGER NOT NULL,
    posti_totali INTEGER NOT NULL,
    posti_disponibili INTEGER NOT NULL,
    stato VARCHAR(20) DEFAULT 'PROGRAMMATO' CHECK (stato IN ('PROGRAMMATO', 'CONFERMATO', 'IN_VIAGGIO', 'RITARDO', 'ARRIVATO', 'CANCELLATO')),
    binario_partenza VARCHAR(20),
    ritardo_minuti INTEGER DEFAULT 0,
    motivo_cancellazione TEXT,
    distanza_km INTEGER NOT NULL
);

-- TABELLA PROMOZIONI
CREATE TABLE IF NOT EXISTS promozioni (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('Standard', 'Fedelta')),
    percentuale_sconto DECIMAL(5,2) NOT NULL CHECK (percentuale_sconto > 0 AND percentuale_sconto <= 100),
    data_creazione DATETIME DEFAULT CURRENT_TIMESTAMP,
    attiva BOOLEAN DEFAULT 1
);

-- TABELLA BIGLIETTI
CREATE TABLE IF NOT EXISTS biglietti (
    id VARCHAR(255) PRIMARY KEY,
    cliente_email VARCHAR(255) NOT NULL,
    viaggio_id VARCHAR(255) NOT NULL,
    nominativo VARCHAR(255) NOT NULL,
    data_acquisto DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_email) REFERENCES clienti(email) ON DELETE CASCADE,
    FOREIGN KEY (viaggio_id) REFERENCES viaggi(id) ON DELETE CASCADE
);

-- Clienti di test
INSERT OR IGNORE INTO clienti (email, password, nome, abbonamento_fedelta) VALUES
('mario.rossi@email.com', '123456', 'Mario Rossi', 1),
('anna.verdi@email.com', 'abcdef', 'Anna Verdi', 0),
('luca.bianchi@email.com', 'federicogay', 'Luca Bianchi', 1);

-- Promozioni di test
INSERT OR IGNORE INTO promozioni (id, nome, tipo, percentuale_sconto) VALUES 
('PROMO_12345678', 'Sconto Estate', 'Standard', 20.0),
('PROMO_87654321', 'Sconto Fedeltà VIP', 'Fedelta', 15.0),
('PROMO_11111111', 'Black Friday', 'Standard', 30.0);

-- Viaggi di test (CON DATE COME TEXT)
INSERT OR IGNORE INTO viaggi (
    id, codice_treno, tipo_treno, stazione_partenza, stazione_arrivo,
    data_viaggio, orario_partenza, orario_arrivo, data_arrivo,
    prezzo, durata_minuti, posti_totali, posti_disponibili,
    stato, binario_partenza, distanza_km
) VALUES 
('V12345678', 'FR001', 'BUSINESS', 'Roma', 'Milano', 
 '2025-06-15', '08:00', '12:30', '2025-06-15',
 150.00, 270, 250, 248, 'PROGRAMMATO', 'Binario 1', 573),
 
('V23456789', 'ST002', 'STANDARD', 'Napoli', 'Torino',
 '2025-06-16', '10:00', '16:45', '2025-06-16', 
 89.50, 405, 350, 350, 'PROGRAMMATO', 'Binario 2', 769),
 
('V34567890', 'EC003', 'ECONOMY', 'Firenze', 'Bologna',
 '2025-06-17', '14:30', '16:15', '2025-06-17',
 25.00, 105, 450, 450, 'PROGRAMMATO', 'Binario 3', 105);


CREATE INDEX IF NOT EXISTS idx_viaggi_data ON viaggi(data_viaggio);
CREATE INDEX IF NOT EXISTS idx_viaggi_stazioni ON viaggi(stazione_partenza, stazione_arrivo);
CREATE INDEX IF NOT EXISTS idx_biglietti_cliente ON biglietti(cliente_email);
CREATE INDEX IF NOT EXISTS idx_biglietti_viaggio ON biglietti(viaggio_id);


SELECT 'RESET COMPLETATO!' as messaggio;
SELECT COUNT(*) as clienti_inseriti FROM clienti;
SELECT COUNT(*) as viaggi_inseriti FROM viaggi;
SELECT COUNT(*) as promozioni_inserite FROM promozioni;