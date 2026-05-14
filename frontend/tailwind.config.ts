import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Zinc dark theme base
        background: 'hsl(240 10% 3.9%)',
        foreground: 'hsl(0 0% 98%)',
        card: {
          DEFAULT: 'hsl(240 10% 5.9%)',
          foreground: 'hsl(0 0% 98%)',
        },
        border: 'hsl(240 3.7% 15.9%)',
        input: 'hsl(240 3.7% 15.9%)',
        muted: {
          DEFAULT: 'hsl(240 3.7% 15.9%)',
          foreground: 'hsl(240 5% 64.9%)',
        },
        // Amber accent
        primary: {
          DEFAULT: 'hsl(38 92% 50%)',
          foreground: 'hsl(240 10% 3.9%)',
        },
        // Blue secondary
        secondary: {
          DEFAULT: 'hsl(217.2 91.2% 59.8%)',
          foreground: 'hsl(0 0% 98%)',
        },
        // Status colors
        success: 'hsl(142.1 70.6% 45.3%)',
        warning: 'hsl(38 92% 50%)',
        destructive: {
          DEFAULT: 'hsl(0 72.2% 50.6%)',
          foreground: 'hsl(0 0% 98%)',
        },
        accent: {
          DEFAULT: 'hsl(240 3.7% 15.9%)',
          foreground: 'hsl(0 0% 98%)',
        },
        popover: {
          DEFAULT: 'hsl(240 10% 5.9%)',
          foreground: 'hsl(0 0% 98%)',
        },
      },
      borderRadius: {
        lg: '0.625rem',
        md: 'calc(0.625rem - 2px)',
        sm: 'calc(0.625rem - 4px)',
      },
      fontFamily: {
        sans: ['Outfit', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
} satisfies Config
