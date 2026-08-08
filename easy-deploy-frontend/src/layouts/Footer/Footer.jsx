import { Heart } from 'lucide-react'
import './Footer.css'

function Footer() {
  return (
    <footer className="footer">
      <p className="footer__text">
        Built with <Heart size={12} className="footer__heart" /> by{' '}
        <a
          href="https://github.com/hunghust5100"
          target="_blank"
          rel="noopener noreferrer"
        >
          Nguyễn Khánh Hưng
        </a>{' '}
        — HUST Project 2, 2026
      </p>
    </footer>
  )
}

export default Footer
