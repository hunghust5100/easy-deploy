import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

/* Design System imports — order matters */
import './styles/tokens.css'
import './styles/reset.css'
import './styles/typography.css'
import './styles/globals.css'

import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
