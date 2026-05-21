import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { Trash2, ShoppingCart, Heart } from 'lucide-react';
import { getProductImage } from '../utils/helpers';

export const Wishlist = () => {
  const { wishlistItems, removeFromWishlist, addToCart } = useCart();

  const handleMoveToCart = async (product) => {
    try {
      await addToCart(product, 1);
      removeFromWishlist(product.productId);
    } catch (err) {
      console.error('Failed to move item to cart', err);
    }
  };

  if (wishlistItems.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 1rem' }} className="animate-fade-in">
        <Heart size={64} style={{ color: 'hsl(var(--text-muted))', marginBottom: '1.5rem' }} />
        <h2 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Your Wishlist is Empty</h2>
        <p style={{ color: 'hsl(var(--text-secondary))', marginBottom: '2rem' }}>
          Save products you like here to purchase them later. Keep exploring!
        </p>
        <Link to="/" className="btn btn-primary" style={{ borderRadius: 'var(--radius-full)' }}>
          Discover Products
        </Link>
      </div>
    );
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <Heart style={{ color: 'hsl(var(--error))' }} fill="currentColor" /> My Wishlist
      </h1>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
        gap: '2rem'
      }}>
        {wishlistItems.map((item) => (
          <div key={item.productId} className="card glass-panel" style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem',
            position: 'relative'
          }}>
            {/* Delete/Remove button absolute */}
            <button
              onClick={() => removeFromWishlist(item.productId)}
              style={{
                position: 'absolute',
                top: '10px',
                right: '10px',
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: 'rgba(9, 9, 11, 0.6)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                color: 'hsl(var(--error))',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                zIndex: 10
              }}
              title="Remove from Wishlist"
            >
              <Trash2 size={16} />
            </button>

            {/* Product Image */}
            <div style={{
              width: '100%',
              height: '180px',
              borderRadius: 'var(--radius-md)',
              overflow: 'hidden',
              backgroundColor: 'hsl(var(--bg-surface))'
            }}>
              <img
                src={getProductImage(item.name, item.categoryName)}
                alt={item.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>

            {/* Details */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
              <Link to={`/product/${item.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {item.name}
                </h3>
              </Link>
              <p style={{
                color: 'hsl(var(--text-secondary))',
                fontSize: '0.8rem',
                lineHeight: '1.4',
                height: '38px',
                overflow: 'hidden',
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical'
              }}>
                {item.description || 'No description available.'}
              </p>
            </div>

            {/* Price & Cart Add Button */}
            <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{ fontSize: '1.15rem', fontWeight: 800 }}>
                ₹{item.price.toLocaleString('en-IN')}
              </span>
              <button
                onClick={() => handleMoveToCart(item)}
                className="btn btn-primary"
                style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem', borderRadius: 'var(--radius-md)', display: 'flex', gap: '0.35rem' }}
              >
                <ShoppingCart size={14} /> Add to Cart
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
export default Wishlist;
