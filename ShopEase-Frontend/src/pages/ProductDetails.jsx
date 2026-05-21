import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ShoppingCart, Heart, ArrowLeft, ShieldCheck, Truck, RefreshCw, AlertCircle } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { getProductImage } from '../utils/helpers';

export const ProductDetails = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToCart, addToWishlist, wishlistItems } = useCart();
  
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      try {
        const response = await api.get(`/product/api/v1/product/fetch/${productId}`);
        setProduct(response);
      } catch {
        console.warn('Failed to fetch product details from backend. Using fallback.');
        // Try searching in fallback products
        const fallback = [
          { productId: 'f1', name: 'ShopEase Premium Headphones', description: 'Wireless over-ear headphones with advanced active noise cancellation, high-fidelity audio, and up to 40 hours of playback.', price: 14999.00, categoryName: 'Electronics' },
          { productId: 'f2', name: 'Veloce Running Shoes', description: 'Ultra-lightweight mesh trainers for speed running and everyday comfort. Built with reactive rubber soles and breathable fabrics.', price: 7999.00, categoryName: 'Apparel' },
          { productId: 'f3', name: 'ShopEase Chrono Classic', description: 'Elegant water-resistant quartz watch with a genuine leather strap and mineral glass dial display. Perfect for corporate and casual styles.', price: 12499.00, categoryName: 'Accessories' },
          { productId: 'f4', name: 'Aero Slim Laptop Pro', description: '14-inch thin laptop with 16GB RAM, 512GB SSD, and powerful quad-core processor. Stunning IPS display and metal alloy chassis.', price: 64999.00, categoryName: 'Electronics' },
          { productId: 'f5', name: 'Ergonomic Mesh Chair', description: 'Comfortable lumbar support office chair with fully adjustable armrests, height adjustable cylinder, and heavy duty wheels.', price: 18999.00, categoryName: 'Home' },
          { productId: 'f6', name: 'Smart Fitness Band V4', description: 'Heart rate tracker, sleep monitor, calorie calculator, and 14-day battery backup smartwatch. Swim-proof design.', price: 3499.00, categoryName: 'Accessories' }
        ].find(p => p.productId === productId);
        
        if (fallback) {
          setProduct(fallback);
        } else {
          setError('Product not found.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [productId]);

  const handleAddToCart = async () => {
    if (!product) return;
    setActionLoading(true);
    setSuccessMsg('');
    setError(null);
    try {
      await addToCart(product, quantity);
      setSuccessMsg(`Added ${quantity} item(s) to your cart!`);
    } catch (err) {
      setError(err.message || 'Could not add item. Stock may be insufficient.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleWishlist = async () => {
    if (!product) return;
    try {
      await addToWishlist(product);
      setSuccessMsg('Added to wishlist!');
    } catch (err) {
      console.error(err);
    }
  };

  const handleBuyNow = async () => {
    if (!product) return;
    if (!user) {
      navigate('/auth');
      return;
    }
    
    setActionLoading(true);
    setError(null);
    try {
      // POST /product/api/v1/product/purchase/{productId}?quantity=N
      // Directly reserves stock and creates an order in one step
      const response = await api.post(
        `/product/api/v1/product/purchase/${product.productId}?quantity=${quantity}`
      );
      
      console.log('Buy Now (purchase) Response:', response);
      
      // Navigate to checkout with returned order details
      navigate('/checkout', {
        state: {
          orderId: response.orderId,
          paymentId: response.paymentId,
          grandTotal: response.grandTotal || product.price * quantity,
          items: [{ ...product, quantity }]
        }
      });
    } catch (err) {
      console.warn('purchase endpoint failed, falling back to create-order.', err);
      try {
        // Fallback: POST /order/api/v1/order/create-order
        const response = await api.post('/order/api/v1/order/create-order', {
          productId: product.productId,
          name: product.name,
          description: product.description || '',
          price: product.price,
          quantity: quantity
        });
        navigate('/checkout', {
          state: {
            orderId: response.orderId,
            paymentId: response.paymentId,
            grandTotal: product.price * quantity,
            items: [{ ...product, quantity }]
          }
        });
      } catch {
        // Final fallback: local mock checkout for development
        navigate('/checkout', {
          state: {
            orderId: crypto.randomUUID(),
            paymentId: crypto.randomUUID(),
            grandTotal: product.price * quantity,
            items: [{ ...product, quantity }],
            isMocked: true
          }
        });
      }
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', gap: '3rem', marginTop: '2rem' }} className="animate-fade-in">
        <div className="skeleton" style={{ flex: 1, height: '400px', borderRadius: 'var(--radius-lg)' }}></div>
        <div style={{ flex: 1.2, display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div className="skeleton" style={{ height: '36px', width: '80%' }}></div>
          <div className="skeleton" style={{ height: '24px', width: '30%' }}></div>
          <div className="skeleton" style={{ height: '80px', width: '100%' }}></div>
          <div className="skeleton" style={{ height: '50px', width: '50%' }}></div>
        </div>
      </div>
    );
  }

  if (error && !product) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 1rem' }}>
        <AlertCircle size={48} style={{ color: 'hsl(var(--error))', marginBottom: '1rem' }} />
        <h2>Product Not Found</h2>
        <p style={{ color: 'hsl(var(--text-secondary))', marginBottom: '1.5rem' }}>The product you are looking for does not exist or has been removed.</p>
        <Link to="/" className="btn btn-primary">Back to Catalog</Link>
      </div>
    );
  }

  const isWishlisted = wishlistItems.some(item => item.productId === product.productId);

  return (
    <div className="animate-fade-in" style={{ marginTop: '1rem' }}>
      {/* Back navigation */}
      <button onClick={() => navigate(-1)} className="btn btn-secondary" style={{ display: 'inline-flex', gap: '0.4rem', border: 'none', background: 'transparent', padding: '0.5rem 0', marginBottom: '1.5rem' }}>
        <ArrowLeft size={16} /> Back to Catalog
      </button>

      {/* Main product columns */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '3rem' }}>
        {/* Left Image column */}
        <div style={{ flex: '1 1 400px' }}>
          <div style={{
            width: '100%',
            height: '400px',
            borderRadius: 'var(--radius-lg)',
            overflow: 'hidden',
            border: '1px solid hsl(var(--border))',
            backgroundColor: 'hsl(var(--bg-secondary))'
          }}>
            <img
              src={getProductImage(product.name, product.categoryName)}
              alt={product.name}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </div>
        </div>

        {/* Right Details column */}
        <div style={{ flex: '1.2 1 450px', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div>
            <span className="badge badge-success" style={{ marginBottom: '0.5rem' }}>In Stock</span>
            <h1 style={{ fontSize: '2.25rem', marginBottom: '0.5rem', lineHeight: 1.2 }}>{product.name}</h1>
            <p style={{ fontSize: '1.75rem', fontWeight: 800, color: 'hsl(var(--text-primary))' }}>
              ₹{product.price.toLocaleString('en-IN')}
            </p>
          </div>

          <div style={{ borderTop: '1px solid hsl(var(--border))', borderBottom: '1px solid hsl(var(--border))', padding: '1rem 0' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', color: 'hsl(var(--text-secondary))' }}>Description</h3>
            <p style={{ color: 'hsl(var(--text-secondary))', lineHeight: 1.6, fontSize: '0.95rem' }}>
              {product.description || 'Experience ultimate build quality and premium styling with the ShopEase E-Commerce line. This product is designed to meet top industry standards.'}
            </p>
          </div>

          {/* Action triggers */}
          <div>
            {/* Quantity Selector */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
              <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Quantity:</span>
              <div style={{ display: 'inline-flex', border: '1px solid hsl(var(--border))', borderRadius: 'var(--radius-md)', overflow: 'hidden' }}>
                <button
                  onClick={() => setQuantity(q => Math.max(1, q - 1))}
                  style={{ width: '36px', height: '36px', background: 'transparent', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}
                >
                  -
                </button>
                <span style={{ width: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, borderLeft: '1px solid hsl(var(--border))', borderRight: '1px solid hsl(var(--border))' }}>
                  {quantity}
                </span>
                <button
                  onClick={() => setQuantity(q => q + 1)}
                  style={{ width: '36px', height: '36px', background: 'transparent', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}
                >
                  +
                </button>
              </div>
            </div>

            {/* Notification messages */}
            {successMsg && (
              <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--success) / 0.1)', border: '1px solid hsl(var(--success) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--success))', fontSize: '0.85rem', marginBottom: '1rem' }}>
                {successMsg}
              </div>
            )}
            {error && (
              <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--error))', fontSize: '0.85rem', marginBottom: '1rem' }}>
                {error}
              </div>
            )}

            {/* Buttons Row */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
              <button
                disabled={actionLoading}
                onClick={handleAddToCart}
                className="btn btn-secondary"
                style={{ flex: 1, minWidth: '150px' }}
              >
                <ShoppingCart size={18} /> Add to Cart
              </button>
              <button
                disabled={actionLoading}
                onClick={handleBuyNow}
                className="btn btn-primary"
                style={{ flex: 1.5, minWidth: '200px' }}
              >
                Buy Now
              </button>
              <button
                onClick={handleWishlist}
                className="btn btn-secondary"
                style={{ width: '48px', height: '48px', padding: 0 }}
                title="Add to Wishlist"
              >
                <Heart size={18} fill={isWishlisted ? 'currentColor' : 'none'} style={{ color: isWishlisted ? 'hsl(var(--error))' : 'inherit' }} />
              </button>
            </div>
          </div>

          {/* Delivery Policy info */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginTop: '1rem', borderTop: '1px solid hsl(var(--border))', paddingTop: '1.5rem' }}>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <Truck size={24} style={{ color: 'hsl(var(--primary))' }} />
              <div>
                <p style={{ fontSize: '0.85rem', fontWeight: 700 }}>Free Delivery</p>
                <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))' }}>For all local orders above ₹1,000</p>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <RefreshCw size={24} style={{ color: 'hsl(var(--primary))' }} />
              <div>
                <p style={{ fontSize: '0.85rem', fontWeight: 700 }}>Easy Returns</p>
                <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))' }}>30 days return window policy</p>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <ShieldCheck size={24} style={{ color: 'hsl(var(--primary))' }} />
              <div>
                <p style={{ fontSize: '0.85rem', fontWeight: 700 }}>Secure Checkout</p>
                <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))' }}>100% encrypted UPI transfers</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default ProductDetails;
