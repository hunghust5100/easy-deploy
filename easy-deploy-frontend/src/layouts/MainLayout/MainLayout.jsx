import Header from '../Header/Header';
import Sidebar from '../Sidebar/Sidebar';
import Footer from '../Footer/Footer';
import './MainLayout.css';

function MainLayout({ children }) {
  return (
    <div className="app-shell">
      <Header />

      <div className="app-shell__body">
        <Sidebar />

        <div className="app-shell__main">
          <main className="app-shell__content">
            {children}
          </main>
          <Footer />
        </div>
      </div>
    </div>
  );
}

export default MainLayout;
