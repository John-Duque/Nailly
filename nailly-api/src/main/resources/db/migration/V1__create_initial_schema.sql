-- =====================================================================
-- Nailly: schema inicial
-- Datas/horas de agendamento em UTC. working_hours usa horário local.
-- =====================================================================

-- ---------- Acesso ----------
CREATE TABLE users (
                       id            BIGINT       NOT NULL AUTO_INCREMENT,
                       email         VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(20)  NOT NULL,
                       active        BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at    DATETIME(6)  NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                       PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'PROFESSIONAL', 'ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------- Clientes ----------
-- user_id pode ser nulo: cliente cadastrada pela profissional, sem conta no app.
CREATE TABLE customers (
                           id         BIGINT       NOT NULL AUTO_INCREMENT,
                           user_id    BIGINT       NULL,
                           name       VARCHAR(120) NOT NULL,
                           phone      VARCHAR(20)  NOT NULL,
                           notes      VARCHAR(500) NULL,
                           created_at DATETIME(6)  NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                           PRIMARY KEY (id),
                           CONSTRAINT uk_customers_user UNIQUE (user_id),
                           CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_customers_phone ON customers (phone);

-- Endereços da cliente (usados no atendimento a domicílio).
CREATE TABLE customer_addresses (
                                    id           BIGINT       NOT NULL AUTO_INCREMENT,
                                    customer_id  BIGINT       NOT NULL,
                                    label        VARCHAR(60)  NOT NULL,
                                    street       VARCHAR(150) NOT NULL,
                                    number       VARCHAR(20)  NOT NULL,
                                    complement   VARCHAR(80)  NULL,
                                    neighborhood VARCHAR(80)  NOT NULL,
                                    city         VARCHAR(80)  NOT NULL,
                                    state        CHAR(2)      NOT NULL,
                                    zip_code     VARCHAR(9)   NOT NULL,
                                    latitude     DECIMAL(9,6) NULL,
                                    longitude    DECIMAL(9,6) NULL,
                                    active       BOOLEAN      NOT NULL DEFAULT TRUE,
                                    PRIMARY KEY (id),
                                    CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_customer_addresses_customer ON customer_addresses (customer_id);

-- ---------- Profissionais ----------
-- booking_approval: aprovação dos agendamentos no local dela.
--   AUTOMATIC = já nasce CONFIRMED; MANUAL = nasce PENDING e ela aprova um a um.
-- home_visit_approval: o mesmo, para atendimento a domicílio.
-- request_expiration_minutes: prazo para ela responder um PENDING. NULL = não expira.
-- offers_home_visit: se TRUE, precisa informar o ponto de saída (coordenadas).
CREATE TABLE professionals (
                               id                          BIGINT       NOT NULL AUTO_INCREMENT,
                               user_id                     BIGINT       NOT NULL,
                               name                        VARCHAR(120) NOT NULL,
                               phone                       VARCHAR(20)  NULL,
                               bio                         VARCHAR(500) NULL,
                               booking_approval            VARCHAR(10)  NOT NULL DEFAULT 'AUTOMATIC',
                               home_visit_approval         VARCHAR(10)  NOT NULL DEFAULT 'MANUAL',
                               request_expiration_minutes  INT          NULL,
                               offers_home_visit           BOOLEAN      NOT NULL DEFAULT FALSE,
                               home_visit_origin_latitude  DECIMAL(9,6) NULL,
                               home_visit_origin_longitude DECIMAL(9,6) NULL,
                               active                      BOOLEAN      NOT NULL DEFAULT TRUE,
                               created_at                  DATETIME(6)  NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                               PRIMARY KEY (id),
                               CONSTRAINT uk_professionals_user UNIQUE (user_id),
                               CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users (id),
                               CONSTRAINT ck_professionals_approval CHECK (booking_approval IN ('AUTOMATIC', 'MANUAL')),
                               CONSTRAINT ck_professionals_home_approval CHECK (home_visit_approval IN ('AUTOMATIC', 'MANUAL')),
                               CONSTRAINT ck_professionals_expiration CHECK (
                                   request_expiration_minutes IS NULL OR request_expiration_minutes > 0
                                   ),
                               CONSTRAINT ck_professionals_home_origin CHECK (
                                   offers_home_visit = FALSE
                                       OR (home_visit_origin_latitude IS NOT NULL AND home_visit_origin_longitude IS NOT NULL)
                                   )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Locais de atendimento da profissional (estabelecimento, estúdio, etc.).
CREATE TABLE professional_locations (
                                        id              BIGINT       NOT NULL AUTO_INCREMENT,
                                        professional_id BIGINT       NOT NULL,
                                        name            VARCHAR(80)  NOT NULL,
                                        street          VARCHAR(150) NOT NULL,
                                        number          VARCHAR(20)  NOT NULL,
                                        complement      VARCHAR(80)  NULL,
                                        neighborhood    VARCHAR(80)  NOT NULL,
                                        city            VARCHAR(80)  NOT NULL,
                                        state           CHAR(2)      NOT NULL,
                                        zip_code        VARCHAR(9)   NOT NULL,
                                        latitude        DECIMAL(9,6) NULL,
                                        longitude       DECIMAL(9,6) NULL,
                                        active          BOOLEAN      NOT NULL DEFAULT TRUE,
                                        PRIMARY KEY (id),
                                        CONSTRAINT fk_professional_locations_professional FOREIGN KEY (professional_id) REFERENCES professionals (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_professional_locations_professional ON professional_locations (professional_id, active);

-- Taxa de deslocamento por faixa de distância.
-- Ex.: (5.0 km, R$ 10), (10.0 km, R$ 20). Vale a menor faixa que comporta a distância.
CREATE TABLE home_visit_fees (
                                 id              BIGINT        NOT NULL AUTO_INCREMENT,
                                 professional_id BIGINT        NOT NULL,
                                 max_distance_km DECIMAL(5,1)  NOT NULL,
                                 fee             DECIMAL(10,2) NOT NULL,
                                 PRIMARY KEY (id),
                                 CONSTRAINT uk_home_visit_fees_distance UNIQUE (professional_id, max_distance_km),
                                 CONSTRAINT fk_home_visit_fees_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
                                 CONSTRAINT ck_home_visit_fees_distance CHECK (max_distance_km > 0),
                                 CONSTRAINT ck_home_visit_fees_fee CHECK (fee >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------- Técnicas (tipo de procedimento) ----------
CREATE TABLE procedure_types (
                                 id     BIGINT      NOT NULL AUTO_INCREMENT,
                                 name   VARCHAR(80) NOT NULL,
                                 active BOOLEAN     NOT NULL DEFAULT TRUE,
                                 PRIMARY KEY (id),
                                 CONSTRAINT uk_procedure_types_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Dados iniciais (ponto de partida; pode editar).
INSERT INTO procedure_types (name) VALUES
                                       ('Esmaltação comum'),
                                       ('Esmaltação em gel'),
                                       ('Alongamento em gel'),
                                       ('Blindagem'),
                                       ('Manutenção'),
                                       ('Remoção');

-- ---------- Serviços (o trabalho que cada profissional oferece) ----------
-- body_area: HANDS (mãos), FEET (pés), BOTH (mãos e pés).
-- available_at_home: FALSE = serviço que ela não faz na casa da cliente.
CREATE TABLE salon_services (
                                id                BIGINT        NOT NULL AUTO_INCREMENT,
                                professional_id   BIGINT        NOT NULL,
                                procedure_type_id BIGINT        NOT NULL,
                                title             VARCHAR(120)  NOT NULL,
                                description       VARCHAR(1000) NULL,
                                body_area         VARCHAR(10)   NOT NULL,
                                duration_minutes  SMALLINT      NOT NULL,
                                price             DECIMAL(10,2) NOT NULL,
                                available_at_home BOOLEAN       NOT NULL DEFAULT TRUE,
                                active            BOOLEAN       NOT NULL DEFAULT TRUE,
                                created_at        DATETIME(6)   NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                                PRIMARY KEY (id),
                                CONSTRAINT fk_salon_services_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
                                CONSTRAINT fk_salon_services_procedure_type FOREIGN KEY (procedure_type_id) REFERENCES procedure_types (id),
                                CONSTRAINT ck_salon_services_body_area CHECK (body_area IN ('HANDS', 'FEET', 'BOTH')),
                                CONSTRAINT ck_salon_services_duration CHECK (duration_minutes > 0),
                                CONSTRAINT ck_salon_services_price CHECK (price >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_salon_services_professional ON salon_services (professional_id, active);

-- Fotos do trabalho (só a URL; o armazenamento da imagem fica para depois).
CREATE TABLE service_photos (
                                id         BIGINT       NOT NULL AUTO_INCREMENT,
                                service_id BIGINT       NOT NULL,
                                url        VARCHAR(500) NOT NULL,
                                position   SMALLINT     NOT NULL DEFAULT 0,
                                PRIMARY KEY (id),
                                CONSTRAINT fk_service_photos_service FOREIGN KEY (service_id) REFERENCES salon_services (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_service_photos_service ON service_photos (service_id, position);

-- ---------- Horário de funcionamento ----------
-- day_of_week segue java.time.DayOfWeek: 1 = segunda ... 7 = domingo.
-- Várias linhas no mesmo dia permitem intervalo (ex.: 09-12 e 13-18).
-- location_id preenchido = horário naquele local.
-- location_id NULL        = horário de atendimento a domicílio.
CREATE TABLE working_hours (
                               id              BIGINT  NOT NULL AUTO_INCREMENT,
                               professional_id BIGINT  NOT NULL,
                               location_id     BIGINT  NULL,
                               day_of_week     TINYINT NOT NULL,
                               start_time      TIME    NOT NULL,
                               end_time        TIME    NOT NULL,
                               PRIMARY KEY (id),
                               CONSTRAINT fk_working_hours_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
                               CONSTRAINT fk_working_hours_location FOREIGN KEY (location_id) REFERENCES professional_locations (id),
                               CONSTRAINT ck_working_hours_day CHECK (day_of_week BETWEEN 1 AND 7),
                               CONSTRAINT ck_working_hours_range CHECK (end_time > start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_working_hours_professional_day ON working_hours (professional_id, day_of_week);

-- ---------- Agendamentos ----------
-- status: PENDING (aguarda a profissional), CONFIRMED, REJECTED (recusado por ela),
--         EXPIRED (ela não respondeu a tempo), CANCELED, COMPLETED, NO_SHOW.
-- expires_at: hora limite para ela responder (só em pedidos PENDING; NULL = não expira).
-- service_mode: AT_LOCATION (no local dela) ou HOME_VISIT (na casa da cliente).
-- service_address: cópia do endereço onde o atendimento acontece (histórico).
-- end_at = start_at + soma das durações dos itens (calculado no caso de uso).
-- total_price = items_total + home_visit_fee.
CREATE TABLE appointments (
                              id                  BIGINT        NOT NULL AUTO_INCREMENT,
                              customer_id         BIGINT        NOT NULL,
                              professional_id     BIGINT        NOT NULL,
                              start_at            DATETIME(6)   NOT NULL,
                              end_at              DATETIME(6)   NOT NULL,
                              status              VARCHAR(20)   NOT NULL,
                              expires_at          DATETIME(6)   NULL,
                              service_mode        VARCHAR(15)   NOT NULL,
                              location_id         BIGINT        NULL,
                              customer_address_id BIGINT        NULL,
                              service_address     VARCHAR(400)  NOT NULL,
                              distance_km         DECIMAL(5,1)  NULL,
                              home_visit_fee      DECIMAL(10,2) NOT NULL DEFAULT 0,
                              items_total         DECIMAL(10,2) NOT NULL,
                              total_price         DECIMAL(10,2) NOT NULL,
                              notes               VARCHAR(500)  NULL,
                              status_reason       VARCHAR(255)  NULL,
                              decided_at          DATETIME(6)   NULL,
                              canceled_at         DATETIME(6)   NULL,
                              created_at          DATETIME(6)   NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                              updated_at          DATETIME(6)   NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
                              PRIMARY KEY (id),
                              CONSTRAINT fk_appointments_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
                              CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
                              CONSTRAINT fk_appointments_location FOREIGN KEY (location_id) REFERENCES professional_locations (id),
                              CONSTRAINT fk_appointments_customer_address FOREIGN KEY (customer_address_id) REFERENCES customer_addresses (id),
                              CONSTRAINT ck_appointments_range CHECK (end_at > start_at),
                              CONSTRAINT ck_appointments_status CHECK (
                                  status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'EXPIRED', 'CANCELED', 'COMPLETED', 'NO_SHOW')
                                  ),
                              CONSTRAINT ck_appointments_mode CHECK (
                                  (service_mode = 'AT_LOCATION' AND location_id IS NOT NULL)
                                      OR (service_mode = 'HOME_VISIT' AND customer_address_id IS NOT NULL)
                                  ),
                              CONSTRAINT ck_appointments_amounts CHECK (items_total >= 0 AND home_visit_fee >= 0 AND total_price >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_appointments_professional_start ON appointments (professional_id, start_at);
CREATE INDEX idx_appointments_customer_start ON appointments (customer_id, start_at);
CREATE INDEX idx_appointments_status_expires ON appointments (status, expires_at);

-- Cópia do serviço no momento do agendamento: o histórico não muda
-- se a profissional alterar título, tempo ou preço depois.
CREATE TABLE appointment_items (
                                   id                  BIGINT        NOT NULL AUTO_INCREMENT,
                                   appointment_id      BIGINT        NOT NULL,
                                   service_id          BIGINT        NOT NULL,
                                   service_title       VARCHAR(120)  NOT NULL,
                                   procedure_type_name VARCHAR(80)   NOT NULL,
                                   body_area           VARCHAR(10)   NOT NULL,
                                   duration_minutes    SMALLINT      NOT NULL,
                                   price               DECIMAL(10,2) NOT NULL,
                                   PRIMARY KEY (id),
                                   CONSTRAINT fk_appointment_items_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
                                   CONSTRAINT fk_appointment_items_service FOREIGN KEY (service_id) REFERENCES salon_services (id),
                                   CONSTRAINT ck_appointment_items_body_area CHECK (body_area IN ('HANDS', 'FEET', 'BOTH')),
                                   CONSTRAINT ck_appointment_items_duration CHECK (duration_minutes > 0),
                                   CONSTRAINT ck_appointment_items_price CHECK (price >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_appointment_items_appointment ON appointment_items (appointment_id);