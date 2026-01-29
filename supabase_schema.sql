-- =============================================================================
-- UniTask Supabase Database Schema
-- =============================================================================
-- Ejecuta este script en el SQL Editor de tu proyecto Supabase Dashboard
-- Dashboard > SQL Editor > New Query
-- =============================================================================

-- Tabla de perfiles de usuario
-- Se crea automáticamente cuando un usuario se registra
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    avatar_url TEXT,
    total_xp INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabla de estadísticas de usuario
CREATE TABLE IF NOT EXISTS public.user_stats (
    user_id UUID REFERENCES public.profiles(id) PRIMARY KEY,
    total_tasks_completed INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_completed_date DATE
);

-- Tabla de materias/asignaturas
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id),
    name TEXT NOT NULL,
    color_hex TEXT NOT NULL,
    teacher TEXT,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabla de tareas
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    subject_id UUID REFERENCES public.subjects(id) NOT NULL,
    title TEXT NOT NULL,
    due_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabla de templates de alarmas
CREATE TABLE IF NOT EXISTS public.alarm_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    name TEXT NOT NULL,
    minutes_before INTEGER NOT NULL,
    sound_enabled BOOLEAN DEFAULT TRUE,
    vibration_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =============================================================================
-- Row Level Security (RLS) Policies
-- =============================================================================

-- Habilitar RLS en todas las tablas
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_stats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alarm_templates ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- Eliminar políticas existentes (para poder recrearlas)
-- =============================================================================

-- Políticas de profiles
DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON public.profiles;
DROP POLICY IF EXISTS "Anyone can view profiles for leaderboard" ON public.profiles;

-- Políticas de user_stats
DROP POLICY IF EXISTS "Users can view own stats" ON public.user_stats;
DROP POLICY IF EXISTS "Users can update own stats" ON public.user_stats;
DROP POLICY IF EXISTS "Users can insert own stats" ON public.user_stats;

-- Políticas de subjects
DROP POLICY IF EXISTS "Users can view own subjects" ON public.subjects;
DROP POLICY IF EXISTS "Users can create subjects" ON public.subjects;
DROP POLICY IF EXISTS "Users can update own subjects" ON public.subjects;
DROP POLICY IF EXISTS "Users can delete own subjects" ON public.subjects;

-- Políticas de tasks
DROP POLICY IF EXISTS "Users can view own tasks" ON public.tasks;
DROP POLICY IF EXISTS "Users can create tasks" ON public.tasks;
DROP POLICY IF EXISTS "Users can update own tasks" ON public.tasks;
DROP POLICY IF EXISTS "Users can delete own tasks" ON public.tasks;

-- Políticas de alarm_templates
DROP POLICY IF EXISTS "Users can view own alarm templates" ON public.alarm_templates;
DROP POLICY IF EXISTS "Users can create alarm templates" ON public.alarm_templates;
DROP POLICY IF EXISTS "Users can update own alarm templates" ON public.alarm_templates;
DROP POLICY IF EXISTS "Users can delete own alarm templates" ON public.alarm_templates;

-- =============================================================================
-- Crear políticas nuevas
-- =============================================================================

-- Políticas para profiles
CREATE POLICY "Users can view own profile" 
    ON public.profiles FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile" 
    ON public.profiles FOR INSERT 
    WITH CHECK (auth.uid() = id);

-- Política para ver todos los perfiles (para leaderboard)
CREATE POLICY "Anyone can view profiles for leaderboard" 
    ON public.profiles FOR SELECT 
    USING (TRUE);

-- Políticas para user_stats
CREATE POLICY "Users can view own stats" 
    ON public.user_stats FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can update own stats" 
    ON public.user_stats FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own stats" 
    ON public.user_stats FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

-- Políticas para subjects
CREATE POLICY "Users can view own subjects" 
    ON public.subjects FOR SELECT 
    USING (auth.uid() = user_id OR is_shared = TRUE);

CREATE POLICY "Users can create subjects" 
    ON public.subjects FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own subjects" 
    ON public.subjects FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own subjects" 
    ON public.subjects FOR DELETE 
    USING (auth.uid() = user_id);

-- Políticas para tasks
CREATE POLICY "Users can view own tasks" 
    ON public.tasks FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create tasks" 
    ON public.tasks FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own tasks" 
    ON public.tasks FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own tasks" 
    ON public.tasks FOR DELETE 
    USING (auth.uid() = user_id);

-- Políticas para alarm_templates
CREATE POLICY "Users can view own alarm templates" 
    ON public.alarm_templates FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create alarm templates" 
    ON public.alarm_templates FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own alarm templates" 
    ON public.alarm_templates FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own alarm templates" 
    ON public.alarm_templates FOR DELETE 
    USING (auth.uid() = user_id);

-- =============================================================================
-- Storage Bucket for Avatars
-- =============================================================================
-- Ejecutar esto en Storage > Create new bucket
-- Nombre: avatars
-- Public: true

-- Política de storage (ejecutar en SQL Editor después de crear el bucket)
-- INSERT INTO storage.buckets (id, name, public) VALUES ('avatars', 'avatars', true);

-- =============================================================================
-- Trigger para crear perfil automáticamente al registrarse
-- =============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, email)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', NEW.email),
        NEW.email
    );
    
    INSERT INTO public.user_stats (user_id)
    VALUES (NEW.id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Crear trigger
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- =============================================================================
-- Índices para mejorar performance
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON public.tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON public.tasks(due_date_time);
CREATE INDEX IF NOT EXISTS idx_subjects_user_id ON public.subjects(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);
