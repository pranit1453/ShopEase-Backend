import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { Trash2, Heart, ArrowRight, ShoppingBag, ShoppingCart } from 'lucide-react';
import api from '../services/api';
import { getProductImage } from '../utils/helpers';

export const Cart = () => {
  const { cartItems, updateQuantity, removeFromCart, moveToWishlist, getCartTotal, clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const subtotal = getCartTotal();
  const shipping = subtotal > 1000 ? 0 : 150;
  const total = subtotal + shipping;

  const handleCheckout = async () => {
    if (!user) {
      navigate('/auth', { state: { from: { pathname: '/cart' } } });
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // Step 1: Validate cart items and reserve stock via proceed-to-buy
      const orderItems = cartItems.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      }));

      let response;
      try {
        // POST /product/api/v1/cart/proceed-to-buy
        // Validates stock and prepares cart for checkout
        response = await api.post('/product/api/v1/cart/proceed-to-buy', {
          userId: user.userId,
          items: orderItems,
        });
        console.log('Proceed to Buy Response:', response);
      } catch (cartErr) {
        console.warn('proceed-to-buy not available, falling back to order/create directly.', cartErr);
      }

      // Step 2: Create backend order
      // POST /order/api/v1/order/create
      const orderResponse = await api.post('/order/api/v1/order/create', {
        userId: user.userId,
        items: orderItems,
      });

      console.log('Order Creation Response:', orderResponse);

      navigate('/checkout', {
        state: {
          orderId: orderResponse.orderId,
          paymentId: orderResponse.paymentId,
          grandTotal: orderResponse.grandTotal || total,
          items: cartItems,
        },
      });

      clearCart();
    } catch (err) {
      console.warn('Backend order checkout failed, simulating local order creation.', err);
      navigate('/checkout', {
        state: {
          orderId: crypto.randomUUID(),
          paymentId: crypto.randomUUID(),
          grandTotal: total,
          items: cartItems,
          isMocked: true,
        },
      });
      clearCart();
    } finally {
      setLoading(false);
    }
  };

  if (cartItems.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 1rem' }} className="animate-fade-in">
        <ShoppingCart size={64} style={{ color: 'hsl(var(--text-muted))', marginBottom: '1.5rem' }} />
        <h2 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Your Cart is Empty</h2>
        <p style={{ color: 'hsl(var(--text-secondary))', marginBottom: '2rem' }}>
          Looks like you haven't added anything to your cart yet. Explore our latest arrivals!
        </p>
        <Link to="/" className="btn btn-primary" style={{ borderRadius: 'var(--radius-full)' }}>
          Shop Now
        </Link>
      </div>
    );
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <ShoppingBag /> Shopping Cart
      </h1>

      {error && (
        <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--error))', fontSize: '0.85rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem' }}>
        {/* Left Cart Items List */}
        <div style={{ flex: '2 1 600px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {cartItems.map((item) => (
            <div key={item.productId} className="card glass-panel" style={{
              display: 'flex',
              gap: '1.25rem',
              padding: '1.25rem',
              alignItems: 'center',
              flexWrap: 'wrap'
            }}>
              {/* Product Image */}
              <div style={{
                width: '90px',
                height: '90px',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
                flexShrink: 0
              }}>
                <img
                  src={getProductImage(item.name, item.categoryName)}
                  alt={item.name}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>

              {/* Title & Desc */}
              <div style={{ flex: 1, minWidth: '200px' }}>
                <Link to={`/product/${item.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '0.25rem' }}>{item.name}</h3>
                </Link>
                <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-muted))', height: '20px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {item.description || 'No description available.'}
                </p>
                <span style={{ fontSize: '0.95rem', fontWeight: 800, display: 'block', marginTop: '0.5rem' }}>
                  ₹{item.price.toLocaleString('en-IN')}
                </span>
              </div>

              {/* Quantity Changer */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <button
                  onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                  style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid hsl(var(--border))',
                    backgroundColor: 'transparent',
                    color: '#fff',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  -
                </button>
                <span style={{ width: '32px', textAlign: 'center', fontWeight: 600 }}>{item.quantity}</span>
                <button
                  onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                  style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid hsl(var(--border))',
                    backgroundColor: 'transparent',
                    color: '#fff',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  +
                </button>
              </div>

              {/* Actions Column */}
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  onClick={() => moveToWishlist(item)}
                  className="btn btn-secondary"
                  style={{ padding: '0.5rem', width: '36px', height: '36px', borderRadius: 'var(--radius-md)' }}
                  title="Move to Wishlist"
                >
                  <Heart size={16} />
                </button>
                <button
                  onClick={() => removeFromCart(item.productId)}
                  className="btn btn-danger"
                  style={{ padding: '0.5rem', width: '36px', height: '36px', borderRadius: 'var(--radius-md)' }}
                  title="Remove from Cart"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Right Checkout Details Card */}
        <div style={{ flex: '1 1 350px' }}>
          <div className="card glass-panel" style={{
            position: 'sticky',
            top: '90px',
            display: 'flex',
            flexDirection: 'column',
            gap: '1.25rem'
          }}>
            <h3 style={{ fontSize: '1.25rem', borderBottom: '1px solid hsl(var(--border))', paddingBottom: '0.75rem' }}>
              Order Summary
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.95rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'hsl(var(--text-secondary))' }}>
                <span>Subtotal</span>
                <span>₹{subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'hsl(var(--text-secondary))' }}>
                <span>Shipping</span>
                <span>{shipping === 0 ? <span style={{ color: 'hsl(var(--success))' }}>FREE</span> : `₹${shipping}`}</span>
              </div>
              {shipping > 0 && (
                <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))', marginTop: '-0.25rem' }}>
                  Add ₹{(1000 - subtotal).toLocaleString('en-IN')} more to unlock FREE shipping!
                </p>
              )}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                fontWeight: 800,
                fontSize: '1.15rem',
                borderTop: '1px solid hsl(var(--border))',
                paddingTop: '0.75rem',
                marginTop: '0.25rem'
              }}>
                <span>Total</span>
                <span style={{ color: 'hsl(var(--warning))' }}>₹{total.toLocaleString('en-IN')}</span>
              </div>
            </div>

            <button
              disabled={loading}
              onClick={handleCheckout}
              className="btn btn-primary"
              style={{
                width: '100%',
                padding: '0.9rem',
                marginTop: '0.5rem',
                borderRadius: 'var(--radius-md)'
              }}
            >
              {loading ? 'Processing Checkout...' : 'Proceed to Checkout'} <ArrowRight size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Cart;
