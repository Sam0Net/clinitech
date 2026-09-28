export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#006591',
        'primary-container': '#0ea5e9',
        'on-primary': '#ffffff',
        'on-primary-container': '#003751',
        secondary: '#006398',
        'on-secondary': '#ffffff',
        'on-secondary-container': '#00476e',
        tertiary: '#006c49',
        'tertiary-container': '#00b17b',
        'on-tertiary': '#ffffff',
        error: '#ba1a1a',
        'on-error': '#ffffff',
        'error-container': '#ffdad6',
        surface: '#f6faff',
        'surface-container': '#eaeef4',
        'surface-container-high': '#e4e8ee',
        'surface-container-highest': '#dee3e9',
        'surface-container-low': '#f0f4fa',
        'surface-container-lowest': '#ffffff',
        'surface-dim': '#d6dae0',
        'surface-bright': '#f6faff',
        'on-surface': '#171c20',
        'on-surface-variant': '#3e4850',
        background: '#f6faff',
        'on-background': '#171c20',
        outline: '#6e7881',
        'outline-variant': '#bec8d2',
      },
      borderColor: {
        DEFAULT: '#bec8d2',  // Color por defecto de todos los bordes
        outline: '#6e7881',
        'outline-variant': '#bec8d2',
      },
      spacing: {
        'sidebar-expanded': '260px',
        'sidebar-collapsed': '72px',
        'container-max': '1440px',
        gutter: '24px',
      },
      fontFamily: {
        inter: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};