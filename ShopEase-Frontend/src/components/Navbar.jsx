import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Heart, User, LogOut, Shield, Settings, Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar = ({ searchVal, setSearchVal }) => {
  const { user, logout, isAdmin } = useAuth();
  const { getCartCount, wishlistItems } = useCart();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const navigate = useNavigate();

  const handleSearchChange = (e) => {
    const val = e.target.value;
    if (setSearchVal) {
      setSearchVal(val);
    }
    navigate('/');
  };

  const handleLogoutClick = () => {
    logout();
    setDropdownOpen(false);
    navigate('/auth');
  };

  return (
    <nav className="glass-panel" style={{
      position: 'sticky',
      top: 0,
      zIndex: 100,
      padding: '0.75rem 5%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      borderBottom: '1px solid hsl(var(--border))',
      backgroundColor: 'rgba(9, 9, 11, 0.8)'
    }}>
      {/* Brand Logo */}
      <Link to="/" style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        textDecoration: 'none',
        color: 'inherit'
      }}>
        <div style={{
          background: 'linear-gradient(135deg, hsl(var(--primary)), hsl(var(--warning)))',
          width: '38px',
          height: '38px',
          borderRadius: 'var(--radius-md)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 800,
          color: '#fff',
          fontSize: '1.25rem',
          boxShadow: 'var(--shadow-glow)'
        }}>
          S
        </div>
        <span style={{
          fontFamily: 'var(--font-heading)',
          fontSize: '1.4rem',
          fontWeight: 800,
          background: 'linear-gradient(to right, #fff, hsl(var(--text-secondary)))',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent'
        }}>
          ShopEase
        </span>
      </Link>

      {/* Search Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        position: 'relative',
        maxWidth: '450px',
        width: '100%',
        margin: '0 1.5rem'
      }}>
        <Search size={18} style={{
          position: 'absolute',
          left: '12px',
          color: 'hsl(var(--text-muted))'
        }} />
        <input
          type="text"
          placeholder="Search products, brands, categories..."
          value={searchVal || ''}
          onChange={handleSearchChange}
          className="input-field"
          style={{
            paddingLeft: '2.5rem',
            borderRadius: 'var(--radius-full)',
            height: '40px',
            backgroundColor: 'hsl(var(--bg-secondary))'
          }}
        />
      </div>

      {/* Nav Actions */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '1.5rem'
      }}>
        <Link to="/" style={{ color: 'hsl(var(--text-secondary))', textDecoration: 'none', fontWeight: 600 }}>Home</Link>

        {/* Wishlist Link */}
        <Link to="/wishlist" style={{
          position: 'relative',
          color: 'hsl(var(--text-secondary))',
          display: 'flex',
          alignItems: 'center'
        }}>
          <Heart size={22} />
          {wishlistItems.length > 0 && (
            <span style={{
              position: 'absolute',
              top: '-8px',
              right: '-8px',
              background: 'hsl(var(--error))',
              color: '#fff',
              fontSize: '0.65rem',
              fontWeight: 800,
              borderRadius: 'var(--radius-full)',
              padding: '2px 5px',
              minWidth: '16px',
              textAlign: 'center',
              border: '2px solid hsl(var(--bg-primary))'
            }}>
              {wishlistItems.length}
            </span>
          )}
        </Link>

        {/* Cart Link */}
        <Link to="/cart" style={{
          position: 'relative',
          color: 'hsl(var(--text-secondary))',
          display: 'flex',
          alignItems: 'center'
        }}>
          <ShoppingCart size={22} />
          {getCartCount() > 0 && (
            <span style={{
              position: 'absolute',
              top: '-8px',
              right: '-8px',
              background: 'hsl(var(--primary))',
              color: '#fff',
              fontSize: '0.65rem',
              fontWeight: 800,
              borderRadius: 'var(--radius-full)',
              padding: '2px 5px',
              minWidth: '16px',
              textAlign: 'center',
              border: '2px solid hsl(var(--bg-primary))'
            }}>
              {getCartCount()}
            </span>
          )}
        </Link>

        {/* User / Profile Dropdown */}
        {user ? (
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                background: 'transparent',
                border: 'none',
                color: 'hsl(var(--text-primary))',
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: '0.95rem'
              }}
            >
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: 'hsl(var(--primary) / 0.2)',
                border: '1px solid hsl(var(--primary) / 0.5)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'hsl(var(--primary))'
              }}>
                <User size={18} />
              </div>
              <span style={{ maxWidth: '100px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {user.name.split(' ')[0]}
              </span>
            </button>

            {dropdownOpen && (
              <div className="glass-panel" style={{
                position: 'absolute',
                right: 0,
                top: '42px',
                width: '220px',
                borderRadius: 'var(--radius-md)',
                boxShadow: 'var(--shadow-lg)',
                padding: '0.5rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.25rem',
                backgroundColor: 'hsl(var(--bg-popover))'
              }}>
                <div style={{
                  padding: '0.5rem',
                  borderBottom: '1px solid hsl(var(--border))',
                  marginBottom: '0.25rem',
                  fontSize: '0.85rem'
                }}>
                  <p style={{ fontWeight: 700, color: 'hsl(var(--text-primary))' }}>{user.name}</p>
                  <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.75rem' }}>@{user.username}</p>
                </div>

                {isAdmin() && (
                  <Link
                    to="/admin"
                    onClick={() => setDropdownOpen(false)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      padding: '0.5rem',
                      borderRadius: 'var(--radius-sm)',
                      color: 'hsl(var(--warning))',
                      textDecoration: 'none',
                      fontSize: '0.9rem',
                      fontWeight: 600
                    }}
                  >
                    <Shield size={16} /> Admin Panel
                  </Link>
                )}

                <Link
                  to="/profile"
                  onClick={() => setDropdownOpen(false)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    color: 'hsl(var(--text-secondary))',
                    textDecoration: 'none',
                    fontSize: '0.9rem'
                  }}
                >
                  <Settings size={16} /> Update Profile
                </Link>

                <button
                  onClick={handleLogoutClick}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    color: 'hsl(var(--error))',
                    background: 'transparent',
                    border: 'none',
                    textAlign: 'left',
                    cursor: 'pointer',
                    width: '100%',
                    fontSize: '0.9rem'
                  }}
                >
                  <LogOut size={16} /> Logout
                </button>
              </div>
            )}
          </div>
        ) : (
          <Link to="/auth" className="btn btn-primary" style={{
            padding: '0.5rem 1.25rem',
            fontSize: '0.9rem',
            borderRadius: 'var(--radius-full)'
          }}>
            Sign In
          </Link>
        )}
      </div>
    </nav>
  );
};
export default Navbar;
