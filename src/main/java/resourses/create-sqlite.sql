CREATE TABLE "shortForm"
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    value TEXT NOT NULL,
    type INTEGER NULL
)
/

CREATE UNIQUE INDEX shortForms_id_uindex ON "shortForm" (id)
/

CREATE TABLE "longForm"
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    shortFormId INTEGER,
    definition TEXT,
    description TEXT,
    CONSTRAINT longForms_shortForms_id_fk FOREIGN KEY (shortFormId) REFERENCES shortForms (id)
)
/

CREATE UNIQUE INDEX table_name_id_uindex ON "longForm" (id)
/