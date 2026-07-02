-- Supabase Schema for Aura Learning App

-- Create Users table (extends auth.users)
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT,
    email TEXT,
    "photoUrl" TEXT,
    provider TEXT,
    "createdAt" BIGINT,
    role TEXT DEFAULT 'user',
    "savedBooks" TEXT[] DEFAULT '{}',
    "savedVideos" TEXT[] DEFAULT '{}'
);

-- Create Books table
CREATE TABLE IF NOT EXISTS public.books (
    id UUID PRIMARY KEY,
    "bookName" TEXT,
    "className" TEXT,
    subject TEXT,
    "coverImage" TEXT,
    "pdfUrl" TEXT,
    "createdAt" BIGINT
);

-- Create Videos table
CREATE TABLE IF NOT EXISTS public.videos (
    id UUID PRIMARY KEY,
    title TEXT,
    description TEXT,
    "className" TEXT,
    subject TEXT,
    thumbnail TEXT,
    "videoUrl" TEXT,
    "youtubeVideoId" TEXT,
    chapter TEXT,
    "partNumber" INT,
    teacher TEXT,
    duration TEXT,
    "order" INT,
    "relatedBooks" TEXT[],
    "createdAt" BIGINT
);

-- Create Banners table
CREATE TABLE IF NOT EXISTS public.banners (
    id TEXT PRIMARY KEY,
    title TEXT,
    "imageUrl" TEXT,
    link TEXT,
    "createdAt" BIGINT
);

-- Create Notes table
CREATE TABLE IF NOT EXISTS public.notes (
    id TEXT PRIMARY KEY,
    "userId" TEXT,
    title TEXT,
    content TEXT,
    "associatedId" TEXT,
    "createdAt" BIGINT
);

-- Create Flashcard Decks table
CREATE TABLE IF NOT EXISTS public.flashcard_decks (
    id TEXT PRIMARY KEY,
    "userId" TEXT,
    title TEXT,
    subject TEXT,
    "className" TEXT,
    "createdAt" BIGINT
);

-- Create Flashcards table
CREATE TABLE IF NOT EXISTS public.flashcards (
    id TEXT PRIMARY KEY,
    "deckId" TEXT,
    "frontText" TEXT,
    "backText" TEXT,
    "createdAt" BIGINT
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.videos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.banners ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.flashcard_decks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.flashcards ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if any
DROP POLICY IF EXISTS "Users can view and manage their own profile" ON public.users;
DROP POLICY IF EXISTS "Admin can manage all users" ON public.users;
DROP POLICY IF EXISTS "Public read access for books" ON public.books;
DROP POLICY IF EXISTS "Admin write access for books" ON public.books;
DROP POLICY IF EXISTS "Public read access for videos" ON public.videos;
DROP POLICY IF EXISTS "Admin write access for videos" ON public.videos;
DROP POLICY IF EXISTS "Public read access for banners" ON public.banners;
DROP POLICY IF EXISTS "Admin write access for banners" ON public.banners;
DROP POLICY IF EXISTS "Users can manage own notes" ON public.notes;
DROP POLICY IF EXISTS "Users can manage own flashcard decks" ON public.flashcard_decks;
DROP POLICY IF EXISTS "Users can manage own flashcards" ON public.flashcards;

-- Users policies
CREATE POLICY "Users can view and manage their own profile" ON public.users FOR ALL USING (auth.uid() = id);
-- Simplified admin policy (assuming admin sets their role manually in DB to 'admin')
CREATE POLICY "Admin can manage all users" ON public.users FOR ALL USING ( (SELECT role FROM public.users WHERE id = auth.uid()) = 'admin' );

-- Books policies
CREATE POLICY "Authenticated read access for books" ON public.books FOR SELECT TO authenticated USING (true);
CREATE POLICY "Admin write access for books" ON public.books FOR ALL USING ( (SELECT role FROM public.users WHERE id = auth.uid()) = 'admin' );

-- Videos policies
CREATE POLICY "Authenticated read access for videos" ON public.videos FOR SELECT TO authenticated USING (true);
CREATE POLICY "Admin write access for videos" ON public.videos FOR ALL USING ( (SELECT role FROM public.users WHERE id = auth.uid()) = 'admin' );

-- Banners policies
CREATE POLICY "Public read access for banners" ON public.banners FOR SELECT USING (true);
CREATE POLICY "Admin write access for banners" ON public.banners FOR ALL USING ( (SELECT role FROM public.users WHERE id = auth.uid()) = 'admin' );

-- Notes policies
CREATE POLICY "Users can manage own notes" ON public.notes FOR ALL USING (auth.uid()::text = "userId");

-- Flashcard Decks policies
CREATE POLICY "Users can manage own flashcard decks" ON public.flashcard_decks FOR ALL USING (auth.uid()::text = "userId");

-- Flashcards policies
CREATE POLICY "Users can manage own flashcards" ON public.flashcards FOR ALL USING (
    EXISTS (
        SELECT 1 FROM public.flashcard_decks 
        WHERE public.flashcard_decks.id = public.flashcards."deckId" 
        AND public.flashcard_decks."userId" = auth.uid()::text
    )
);

-- Insert Storage Buckets
INSERT INTO storage.buckets (id, name, public) VALUES ('covers', 'covers', true) ON CONFLICT DO NOTHING;
INSERT INTO storage.buckets (id, name, public) VALUES ('pdfs', 'pdfs', true) ON CONFLICT DO NOTHING;
INSERT INTO storage.buckets (id, name, public) VALUES ('thumbnails', 'thumbnails', true) ON CONFLICT DO NOTHING;
