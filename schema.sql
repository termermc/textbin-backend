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


--
-- Name: deletecomments(integer); Type: FUNCTION; Schema: public; Owner: termer
--

CREATE FUNCTION public.deletecomments(category integer) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	posts CURSOR FOR
		SELECT post_id
		FROM posts
		WHERE post_category = category;
	post posts%rowtype;
BEGIN
	FOR post IN posts LOOP
		DELETE FROM comments WHERE comments.post_id = post.post_id;
	END LOOP;
	RETURN true;
END;
$$;


ALTER FUNCTION public.deletecomments(category integer) OWNER TO termer;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account_logins; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.account_logins (
    account_id integer NOT NULL,
    ip text NOT NULL,
    "timestamp" timestamp without time zone NOT NULL
);


ALTER TABLE public.account_logins OWNER TO termer;

--
-- Name: accounts; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.accounts (
    id integer NOT NULL,
    account_username character varying(30) NOT NULL,
    account_hash character varying(80) NOT NULL,
    account_rank integer NOT NULL,
    account_record_logins smallint DEFAULT 0
);


ALTER TABLE public.accounts OWNER TO termer;

--
-- Name: accounts_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.accounts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accounts_id_seq OWNER TO termer;

--
-- Name: accounts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.accounts_id_seq OWNED BY public.accounts.id;


--
-- Name: bans; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.bans (
    id integer NOT NULL,
    ban_ip text NOT NULL,
    ban_reason text NOT NULL,
    ban_account integer NOT NULL,
    ban_timestamp timestamp without time zone NOT NULL
);


ALTER TABLE public.bans OWNER TO termer;

--
-- Name: bans_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.bans_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bans_id_seq OWNER TO termer;

--
-- Name: bans_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.bans_id_seq OWNED BY public.bans.id;


--
-- Name: bulletins; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.bulletins (
    id integer NOT NULL,
    bulletin_poster integer NOT NULL,
    bulletin_content text NOT NULL,
    bulletin_date text NOT NULL,
    bulletin_time text NOT NULL
);


ALTER TABLE public.bulletins OWNER TO termer;

--
-- Name: bulletins_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.bulletins_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bulletins_id_seq OWNER TO termer;

--
-- Name: bulletins_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.bulletins_id_seq OWNED BY public.bulletins.id;


--
-- Name: categories; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.categories (
    id integer NOT NULL,
    category_name text NOT NULL,
    category_description text,
    category_code character varying(4),
    category_rank_required integer DEFAULT 0
);


ALTER TABLE public.categories OWNER TO termer;

--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: termer
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.categories_id_seq OWNER TO termer;

--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: termer
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: comments; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.comments (
    id integer NOT NULL,
    comment_name text DEFAULT 'Anonymous'::text NOT NULL,
    comment_date text NOT NULL,
    comment_time text NOT NULL,
    comment_text text NOT NULL,
    post_id text NOT NULL,
    comment_ip text NOT NULL,
    comment_poster_rank integer DEFAULT 0,
    comment_email text,
    comment_trip character varying(10),
    comment_poster_ban text
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
    post_name text NOT NULL,
    post_date text NOT NULL,
    post_time text NOT NULL,
    post_type text DEFAULT 'text'::text NOT NULL,
    post_ip text NOT NULL,
    post_text text NOT NULL,
    post_expire_date text,
    post_expire_time text,
    post_category smallint,
    post_bump timestamp with time zone,
    post_sticky smallint DEFAULT 0
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
-- Name: ranks; Type: TABLE; Schema: public; Owner: termer
--

CREATE TABLE public.ranks (
    id integer NOT NULL,
    rank_name text NOT NULL,
    rank_flare text
);


ALTER TABLE public.ranks OWNER TO termer;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.accounts ALTER COLUMN id SET DEFAULT nextval('public.accounts_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.bans ALTER COLUMN id SET DEFAULT nextval('public.bans_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.bulletins ALTER COLUMN id SET DEFAULT nextval('public.bulletins_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.comments ALTER COLUMN id SET DEFAULT nextval('public.comments_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.posts ALTER COLUMN id SET DEFAULT nextval('public.posts_id_seq'::regclass);


--
-- Name: accounts_pkey1; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey1 PRIMARY KEY (id);


--
-- Name: bans_pkey; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.bans
    ADD CONSTRAINT bans_pkey PRIMARY KEY (id);


--
-- Name: bulletins_pkey1; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.bulletins
    ADD CONSTRAINT bulletins_pkey1 PRIMARY KEY (id);


--
-- Name: categories_pkey1; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey1 PRIMARY KEY (id);


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
-- Name: ranks_pkey; Type: CONSTRAINT; Schema: public; Owner: termer
--

ALTER TABLE ONLY public.ranks
    ADD CONSTRAINT ranks_pkey PRIMARY KEY (id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

INSERT INTO public.ranks
(
	id,
	rank_name,
	rank_flare
) VALUES
(0, 'Poster', null),
(1, 'Moderator', '## Moderator ##'),
(2, 'Administrator', '## Administrator'),
(3, 'Super Admin', '** Super Admin **');

INSERT INTO public.categories
(
	id,
	category_name,
	category_description
) VALUES (
	-1,
	'Private',
	'Post will not be listed and will only be viewable via a link'
);

--
-- PostgreSQL database dump complete
--

