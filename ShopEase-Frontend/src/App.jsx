import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';

// Pages
import Home from './pages/Home';
import ProductDetails from './pages/ProductDetails';
import Auth from './pages/Auth';
import Cart from './pages/Cart';
import Wishlist from './pages/Wishlist';
import Checkout from './pages/Checkout';
import AdminDashboard from './pages/AdminDashboard';
import Profile from './pages/Profile';

export function App() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <div className="app-container">
            {/* Global Sticky Navigation Bar */}
            <Navbar searchVal={searchQuery} setSearchVal={setSearchQuery} />

            {/* Main Content Page Container */}
            <main className="content-area">
              <Routes>
                <Route path="/" element={<Home searchVal={searchQuery} />} />
                <Route path="/product/:productId" element={<ProductDetails />} />
                <Route path="/auth" element={<Auth />} />
                <Route path="/cart" element={<Cart />} />
                <Route path="/wishlist" element={<Wishlist />} />
                <Route path="/checkout" element={<Checkout />} />
                <Route path="/admin" element={<AdminDashboard />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>
          </div>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
