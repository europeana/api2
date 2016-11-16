-- CREATE USER europeana WITH PASSWORD 'culture';
-- CREATE DATABASE europeana;
GRANT ALL PRIVILEGES ON DATABASE europeana TO europeana;

--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.2
-- Dumped by pg_dump version 9.5.5

-- Started on 2016-11-09 13:56:18 CET

-- TOC entry 209 (class 1259 OID 4580530)
-- Name: apikey; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE apikey (
  apikey character varying(30) NOT NULL,
  privatekey character varying(30) NOT NULL,
  usagelimit bigint,
  appname character varying(255),
  registrationdate date DEFAULT '1980-01-01'::date NOT NULL,
  activationdate date,
  email character varying(100) DEFAULT 'unknown'::character varying NOT NULL,
  level character varying(8) DEFAULT 'CLIENT'::character varying NOT NULL,
  firstname character varying(50),
  lastname character varying(50),
  company character varying(100),
  website character varying(100),
  description character varying(255)
);


ALTER TABLE apikey OWNER TO europeana;

--
-- TOC entry 210 (class 1259 OID 4580539)
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: europeana
--

CREATE SEQUENCE hibernate_sequence
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;


ALTER TABLE hibernate_sequence OWNER TO europeana;

--
-- TOC entry 211 (class 1259 OID 4580541)
-- Name: saveditem; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE saveditem (
  id bigint NOT NULL,
  author character varying(80),
  datesaved timestamp without time zone,
  doctype character varying(10),
  europeanaobject character varying(256),
  title character varying(120),
  userid bigint,
  europeanauri character varying(256),
  locale character varying(5),
  language character varying(3),
  europeanaid bigint,
  carouselitemid bigint
);


ALTER TABLE saveditem OWNER TO europeana;

--
-- TOC entry 212 (class 1259 OID 4580547)
-- Name: savedsearch; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE savedsearch (
  id bigint NOT NULL,
  datesaved timestamp without time zone,
  query character varying(200),
  userid bigint,
  querystring character varying(200),
  searchtermid bigint,
  editorpickid bigint,
  locale character varying(5),
  language character varying(3)
);


ALTER TABLE savedsearch OWNER TO europeana;

--
-- TOC entry 213 (class 1259 OID 4580550)
-- Name: socialtag; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE socialtag (
  id bigint NOT NULL,
  datesaved timestamp without time zone,
  doctype character varying(10),
  europeanaobject character varying(256),
  europeanauri character varying(256),
  tag character varying(60),
  title character varying(120),
  userid bigint,
  locale character varying(5),
  language character varying(3),
  europeanaid bigint
);


ALTER TABLE socialtag OWNER TO europeana;

--
-- TOC entry 214 (class 1259 OID 4580556)
-- Name: token; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE token (
  token character varying(64) NOT NULL,
  created bigint NOT NULL,
  email character varying(64) NOT NULL,
  redirect character varying(256) DEFAULT 'http://europeana.eu'::character varying NOT NULL
);


ALTER TABLE token OWNER TO europeana;

--
-- TOC entry 215 (class 1259 OID 4580559)
-- Name: users; Type: TABLE; Schema: public; Owner: europeana
--

CREATE TABLE users (
  id bigint NOT NULL,
  email character varying(100) NOT NULL,
  enabled boolean,
  firstname character varying(100),
  lastlogin timestamp without time zone,
  lastname character varying(100),
  newsletter boolean,
  password character varying(64),
  registrationdate date,
  role character varying(25),
  username character varying(60),
  languages character varying(30),
  projectid character varying(30),
  providerid character varying(30),
  address character varying(250),
  company character varying(100),
  country character varying(30),
  phone character varying(15),
  website character varying(100),
  fieldofwork character varying(50),
  languageitem character varying(20),
  languageportal character varying(20),
  languagesearch character varying(20),
  languagesearchapplied boolean DEFAULT true,
  activationdate date
);


ALTER TABLE users OWNER TO europeana;

--
-- TOC entry 3333 (class 2606 OID 4580567)
-- Name: apikey_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY apikey
  ADD CONSTRAINT apikey_pkey PRIMARY KEY (apikey);


--
-- TOC entry 3335 (class 2606 OID 4580569)
-- Name: saveditem_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY saveditem
  ADD CONSTRAINT saveditem_pkey PRIMARY KEY (id);


--
-- TOC entry 3338 (class 2606 OID 4580571)
-- Name: savedsearch_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY savedsearch
  ADD CONSTRAINT savedsearch_pkey PRIMARY KEY (id);


--
-- TOC entry 3341 (class 2606 OID 4580573)
-- Name: socialtag_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY socialtag
  ADD CONSTRAINT socialtag_pkey PRIMARY KEY (id);


--
-- TOC entry 3344 (class 2606 OID 4580575)
-- Name: token_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY token
  ADD CONSTRAINT token_pkey PRIMARY KEY (token);


--
-- TOC entry 3349 (class 2606 OID 4580577)
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY users
  ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3331 (class 1259 OID 4580578)
-- Name: apikey_email_index; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX apikey_email_index ON apikey USING btree (email);


--
-- TOC entry 3345 (class 1259 OID 4580579)
-- Name: email_index; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX email_index ON users USING btree (email);


--
-- TOC entry 3346 (class 1259 OID 4580580)
-- Name: id_idx; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX id_idx ON users USING btree (id);


--
-- TOC entry 3336 (class 1259 OID 4580581)
-- Name: saveditem_userid_idx; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX saveditem_userid_idx ON saveditem USING btree (userid);


--
-- TOC entry 3339 (class 1259 OID 4580582)
-- Name: savedsearch_userid_idx; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX savedsearch_userid_idx ON savedsearch USING btree (userid);


--
-- TOC entry 3342 (class 1259 OID 4580583)
-- Name: socialtag_userid_idx; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX socialtag_userid_idx ON socialtag USING btree (userid);


--
-- TOC entry 3347 (class 1259 OID 4580584)
-- Name: username_index; Type: INDEX; Schema: public; Owner: europeana
--

CREATE INDEX username_index ON users USING btree (username);


--
-- TOC entry 3354 (class 2606 OID 4580585)
-- Name: fk_socialtag_users; Type: FK CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY socialtag
  ADD CONSTRAINT fk_socialtag_users FOREIGN KEY (userid) REFERENCES users(id);


--
-- TOC entry 3352 (class 2606 OID 4580590)
-- Name: fk_savedsearch_users; Type: FK CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY savedsearch
  ADD CONSTRAINT fk_savedsearch_users FOREIGN KEY (userid) REFERENCES users(id);


--
-- TOC entry 3350 (class 2606 OID 4580595)
-- Name: fk_saveditem_users; Type: FK CONSTRAINT; Schema: public; Owner: europeana
--

ALTER TABLE ONLY saveditem
  ADD CONSTRAINT fk_saveditem_users FOREIGN KEY (userid) REFERENCES users(id);


--
-- TOC entry 3477 (class 0 OID 0)
-- Dependencies: 30
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--


-- Completed on 2016-11-09 13:56:33 CET

--
-- PostgreSQL database dump complete
--
--TestUser(user:pw)= test@test.com:test
INSERT INTO users (id, activationdate, address, company, country, email, fieldofwork, firstname, languageitem, languageportal, languagesearch, languagesearchapplied, lastlogin, lastname, password, phone, registrationdate, role, username, website)
VALUES (1, '1980-01-01 00:00:00.000000', null, null, null, 'test@test.com', null, 'test', null, null, null, true, null, 'test', 'a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', null, '2016-01-01', 'ROLE_GOD', 'test', null);
INSERT INTO apikey (apikey, activationdate, appname, company, description, email, firstname, lastname, level, privatekey, registrationdate, usagelimit, website)
VALUES ('api2demo', '2016-01-01', 'test', 'test', null, 'test@test.com', 'test', 'test', 'ADMIN', 'verysecret', '2016-01-01', 0, null);
