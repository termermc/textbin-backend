--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.16
-- Dumped by pg_dump version 9.5.16

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.comments (
    id integer NOT NULL,
    name text DEFAULT 'Anonymous'::text NOT NULL,
    date text NOT NULL,
    "time" text NOT NULL,
    text text NOT NULL,
    post_id text NOT NULL,
    ip text NOT NULL
);


ALTER TABLE public.comments OWNER TO termer;

--
-- Name: comments_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.comments_id_seq OWNER TO termer;

--
-- Name: comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.comments_id_seq OWNED BY public.comments.id;


--
-- Name: posts; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.posts (
    id integer NOT NULL,
    post_id text NOT NULL,
    name text NOT NULL,
    date text NOT NULL,
    "time" text NOT NULL,
    type text DEFAULT 'text'::text NOT NULL,
    ip text NOT NULL,
    public smallint DEFAULT '1'::smallint NOT NULL,
    text text NOT NULL
);


ALTER TABLE public.posts OWNER TO termer;

--
-- Name: posts_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.posts_id_seq OWNER TO termer;

--
-- Name: posts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.posts_id_seq OWNED BY public.posts.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.comments ALTER COLUMN id SET DEFAULT nextval('public.comments_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.posts ALTER COLUMN id SET DEFAULT nextval('public.posts_id_seq'::regclass);


--
-- Name: comments_pkey; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: posts_pkey; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

