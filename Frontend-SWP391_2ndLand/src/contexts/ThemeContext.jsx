import { createContext, useContext, useEffect, useState, useCallback } from 'react';

// Theme tokens
export const THEMES = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system'
};

const STORAGE_KEY = 'ui.theme';

function getSystemPreference() {
  if (typeof window === 'undefined') return THEMES.LIGHT;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? THEMES.DARK : THEMES.LIGHT;
}

function resolveTheme(mode) {
  if (mode === THEMES.SYSTEM) return getSystemPreference();
  return mode;
}

export const ThemeContext = createContext({
  theme: THEMES.LIGHT,
  mode: THEMES.LIGHT,
  setMode: () => {},
  toggle: () => {}
});

export function ThemeProvider({ children }) {
  const [mode, setMode] = useState(() => {
    if (typeof window === 'undefined') return THEMES.LIGHT;
    return localStorage.getItem(STORAGE_KEY) || THEMES.LIGHT;
  });
  const [theme, setTheme] = useState(resolveTheme(mode));

  // Apply theme class to html element
  useEffect(() => {
    const effective = resolveTheme(mode);
    setTheme(effective);
    const root = document.documentElement;
    if (effective === THEMES.DARK) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [mode]);

  // Watch system changes when in system mode
  useEffect(() => {
    if (mode !== THEMES.SYSTEM) return;
    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => setTheme(resolveTheme(THEMES.SYSTEM));
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, [mode]);

  const setModePersist = useCallback((next) => {
    setMode(next);
    if (typeof window !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, next);
    }
  }, []);

  const toggle = useCallback(() => {
    setModePersist(prev => prev === THEMES.DARK ? THEMES.LIGHT : THEMES.DARK);
  }, [setModePersist]);

  return (
    <ThemeContext.Provider value={{ theme, mode, setMode: setModePersist, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
