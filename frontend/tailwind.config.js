/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",  // Scan all HTML and TypeScript files
  ],
  theme: {
    extend: {
      colors: {
        // Al Baraka brand colors
        primary: '#0f3460',
        secondary: '#e94560',
        accent: '#533483',
      }
    },
  },
  plugins: [],
}
