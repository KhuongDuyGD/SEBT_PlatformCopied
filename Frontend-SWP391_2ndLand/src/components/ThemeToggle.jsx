import { useTheme, THEMES } from '../contexts/ThemeContext';
import { useState } from 'react';

// Simple toggle; can be replaced later with Ant Design Dropdown
export default function ThemeToggle({ className = '' }) {
  const { mode, toggle, setMode } = useTheme();
  const [open, setOpen] = useState(false);

  return (
    <div className={`relative inline-block text-left ${className}`}>
      <button
        type="button"
        onClick={() => setOpen(o => !o)}
        className="px-3 py-2 rounded-md bg-surface/60 dark:bg-surface-dark/60 backdrop-blur border border-border/50 dark:border-border-dark/40 shadow-sm flex items-center gap-2 text-sm hover:shadow-md transition"
        aria-haspopup="true"
        aria-expanded={open}
      >
        <span className="inline-flex items-center gap-1">
          {mode === THEMES.DARK ? (
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4"><path d="M21.64 13a1 1 0 0 0-1.05-.14 8.05 8.05 0 0 1-3.37.73 8.15 8.15 0 0 1-8.14-8.1 8 8 0 0 1 .25-2A1 1 0 0 0 8.4 2.36 10.14 10.14 0 1 0 22 14.6a1 1 0 0 0-.36-1.6Z"/></svg>
          ) : mode === THEMES.LIGHT ? (
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-4 h-4"><path strokeLinecap="round" strokeLinejoin="round" d="M12 3v2.25M18.364 5.636l-1.591 1.591M21 12h-2.25M18.364 18.364l-1.591-1.591M12 18.75V21M7.227 16.773l-1.591 1.591M5.25 12H3m3.636-6.364L7.227 7.227M15.75 12a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0Z"/></svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4"><path d="M12 2a1 1 0 0 1 .993.883L13 3v2a1 1 0 0 1-1.993.117L11 5V3a1 1 0 0 1 1-1Zm5.657 2.343a1 1 0 0 1 1.32-.083l.094.083 1.414 1.414a1 1 0 0 1-1.32 1.497l-.094-.083-1.414-1.414a1 1 0 0 1 0-1.414ZM12 17a5 5 0 0 1 .217 9.995L12 27a5 5 0 0 1-.217-9.995L12 17Zm8-6a1 1 0 0 1 .117 1.993L20 13h-2a1 1 0 0 1-.117-1.993L18 11h2ZM6 11a1 1 0 0 1 .117 1.993L6 13H4a1 1 0 0 1-.117-1.993L4 11h2Zm.636-6.95.094.083 1.414 1.414a1 1 0 0 1-1.32 1.497l-.094-.083L5.316 6.05a1 1 0 0 1 1.32-1.497ZM12 7a5 5 0 0 1 .217 9.995L12 17a5 5 0 0 1-.217-9.995L12 7Z"/></svg>
          )}
          <span className="capitalize hidden sm:inline">{mode}</span>
        </span>
        <svg className="w-3 h-3 opacity-60" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.23 7.21a.75.75 0 0 1 1.06.02L10 11.168l3.71-3.938a.75.75 0 1 1 1.08 1.04l-4.24 4.5a.75.75 0 0 1-1.08 0l-4.24-4.5a.75.75 0 0 1 .02-1.06Z" clipRule="evenodd"/></svg>
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-40 origin-top-right rounded-md border border-border/50 dark:border-border-dark/40 bg-surface dark:bg-surface-dark shadow-lg ring-1 ring-black/5 focus:outline-none z-50">
          <div className="py-1 text-sm">
            {[THEMES.LIGHT, THEMES.DARK, THEMES.SYSTEM].map(opt => (
              <button
                key={opt}
                onClick={() => { setMode(opt); setOpen(false); }}
                className={`w-full text-left px-3 py-2 hover:bg-primary/10 dark:hover:bg-primary-dark/20 transition flex items-center gap-2 ${mode===opt?'text-primary dark:text-primary-dark font-medium':''}`}
              >
                {opt === THEMES.LIGHT && '🌞'}
                {opt === THEMES.DARK && '🌜'}
                {opt === THEMES.SYSTEM && '🖥️'}
                <span className="capitalize">{opt}</span>
              </button>
            ))}
          </div>
          <div className="px-2 pb-2">
            <button onClick={toggle} className="text-xs text-muted dark:text-muted-dark underline hover:text-primary dark:hover:text-primary-dark">
              Nhanh: chuyển {mode === THEMES.DARK ? 'Light' : 'Dark'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
