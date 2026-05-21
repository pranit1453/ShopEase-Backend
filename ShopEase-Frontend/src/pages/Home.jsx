import { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { ShoppingCart, Heart, ArrowUpDown, ChevronLeft, ChevronRight, Sparkles, Filter } from 'lucide-react';
import api from '../services/api';
import { getProductImage } from '../utils/helpers';

// Fallback local products if database is empty/unreachable
const FALLBACK_PRODUCTS = [
  { productId: 'f1', name: 'ShopEase Premium Headphones', description: 'Wireless over-ear headphones with advanced active noise cancellation.', price: 14999.00, categoryName: 'Electronics' },
  { productId: 'f2', name: 'Veloce Running Shoes', description: 'Ultra-lightweight mesh trainers for speed running and everyday comfort.', price: 7999.00, categoryName: 'Apparel' },
  { productId: 'f3', name: 'ShopEase Chrono Classic', description: 'Elegant water-resistant quartz watch with a genuine leather strap.', price: 12499.00, categoryName: 'Accessories' },
  { productId: 'f4', name: 'Aero Slim Laptop Pro', description: '14-inch thin laptop with 16GB RAM, 512GB SSD, and powerful quad-core processor.', price: 64999.00, categoryName: 'Electronics' },
  { productId: 'f5', name: 'Ergonomic Mesh Chair', description: 'Comfortable lumbar support office chair with fully adjustable armrests.', price: 18999.00, categoryName: 'Home' },
  { productId: 'f6', name: 'Smart Fitness Band V4', description: 'Heart rate tracker, sleep monitor, and 14-day battery backup smartwatch.', price: 3499.00, categoryName: 'Accessories' }
];

export const Home = ({ searchVal }) => {
  const { user } = useAuth();
  const { addToCart, addToWishlist, wishlistItems } = useCart();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Pagination & Filters State
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [sortBy, setSortBy] = useState('PRODUCT_ID');
  const [sortDir, setSortDir] = useState('ASC');
  const [usingFallback, setUsingFallback] = useState(false);

  // Fetch Categories
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await api.get('/product/api/v1/category/fetch/all');
        if (Array.isArray(response)) {
          setCategories(response);
        }
      } catch (err) {
        console.warn('Failed to fetch categories from backend, using fallbacks.', err);
        setCategories([
          { categoryId: 'c1', name: 'Electronics', description: 'Gadgets and gear' },
          { categoryId: 'c2', name: 'Apparel', description: 'Clothing and footwear' },
          { categoryId: 'c3', name: 'Home', description: 'Furniture and decor' },
          { categoryId: 'c4', name: 'Accessories', description: 'Watches, bags and more' },
        ]);
      }
    };
    fetchCategories();
  }, []);

  // Fetch Products
  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        let response;
        if (selectedCategory) {
          // Fetch products filtered by category
          response = await api.get(
            `/product/api/v1/category/fetch/category-with-products?page=${page}&size=6&categoryId=${selectedCategory}&keyword=${searchVal || ''}`
          );
          // category-with-products returns PageResponse of CategoryWithProductResponse
          // let's extract the products
          const items = response?.content?.[0]?.products || [];
          setProducts(items);
          setTotalPages(response?.totalPages || 1);
        } else {
          // Fetch all products
          response = await api.get(
            `/product/api/v1/product/fetch/all?page=${page}&size=6&sortBy=${sortBy}&sortDirection=${sortDir}&keyword=${searchVal || ''}`
          );
          if (response && response.content) {
            setProducts(response.content);
            setTotalPages(response.totalPages || 1);
            setUsingFallback(false);
          } else {
            throw new Error('Invalid format');
          }
        }
      } catch (err) {
        console.warn('Failed to fetch products from backend, showing fallback data.', err);
        
        // Apply client search filter to fallback products
        let items = FALLBACK_PRODUCTS;
        if (selectedCategory) {
          const cat = categories.find(c => c.categoryId === selectedCategory);
          if (cat) {
            items = items.filter(p => p.categoryName.toLowerCase() === cat.name.toLowerCase());
          }
        }
        if (searchVal) {
          items = items.filter(p => p.name.toLowerCase().includes(searchVal.toLowerCase()));
        }
        
        // Sorting fallbacks
        items = [...items].sort((a, b) => {
          if (sortBy === 'PRICE') {
            return sortDir === 'ASC' ? a.price - b.price : b.price - a.price;
          }
          return 0; // Maintain original
        });

        setProducts(items);
        setTotalPages(1);
        setUsingFallback(true);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, [page, selectedCategory, sortBy, sortDir, searchVal, categories]);

  const toggleSortDir = () => {
    setSortDir(prev => prev === 'ASC' ? 'DESC' : 'ASC');
    setPage(0);
  };

  const handleSortChange = (e) => {
    setSortBy(e.target.value);
    setPage(0);
  };

  const isWishlisted = (id) => {
    return wishlistItems.some(item => item.productId === id);
  };

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <div style={{
        background: 'linear-gradient(135deg, hsl(var(--primary) / 0.2) 0%, hsl(var(--warning) / 0.1) 100%)',
        border: '1px solid hsl(var(--border))',
        borderRadius: 'var(--radius-lg)',
        padding: '3.5rem 2rem',
        marginBottom: '2.5rem',
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{
          position: 'absolute',
          top: '-50px',
          right: '-50px',
          width: '200px',
          height: '200px',
          borderRadius: '50%',
          background: 'hsl(var(--primary))',
          filter: 'blur(100px)',
          opacity: 0.15
        }}></div>
        <div style={{
          position: 'absolute',
          bottom: '-50px',
          left: '-50px',
          width: '200px',
          height: '200px',
          borderRadius: '50%',
          background: 'hsl(var(--warning))',
          filter: 'blur(100px)',
          opacity: 0.15
        }}></div>

        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.4rem 1rem', background: 'hsl(var(--primary) / 0.1)', border: '1px solid hsl(var(--primary) / 0.3)', borderRadius: 'var(--radius-full)', marginBottom: '1.25rem' }}>
          <Sparkles size={14} style={{ color: 'hsl(var(--warning))' }} />
          <span style={{ fontSize: '0.75rem', fontWeight: 800, color: 'hsl(var(--text-secondary))', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Elevating E-Commerce Experience</span>
        </div>

        <h1 style={{ fontSize: '3rem', fontWeight: 800, marginBottom: '1rem', lineHeight: 1.1 }}>
          Your Ultimate Shopping Destination
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 1.75rem' }}>
          Explore our premium curated collection of handpicked electronics, accessories, and designer apparel. Seamless experience from click to delivery.
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
          <a href="#catalog" className="btn btn-primary" style={{ borderRadius: 'var(--radius-full)' }}>Explore Catalog</a>
          {!user && <Link to="/auth" className="btn btn-secondary" style={{ borderRadius: 'var(--radius-full)' }}>Join ShopEase</Link>}
        </div>
      </div>

      {/* Catalog Filters Bar */}
      <div id="catalog" style={{
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap: '1rem',
        marginBottom: '2rem',
        borderBottom: '1px solid hsl(var(--border))',
        paddingBottom: '1rem'
      }}>
        {/* Categories Chips */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          <button
            onClick={() => { setSelectedCategory(null); setPage(0); }}
            className={`btn ${selectedCategory === null ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.4rem 1rem', fontSize: '0.85rem', borderRadius: 'var(--radius-full)' }}
          >
            All Products
          </button>
          {categories.map((cat) => (
            <button
              key={cat.categoryId}
              onClick={() => { setSelectedCategory(cat.categoryId); setPage(0); }}
              className={`btn ${selectedCategory === cat.categoryId ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '0.4rem 1rem', fontSize: '0.85rem', borderRadius: 'var(--radius-full)' }}
            >
              {cat.name}
            </button>
          ))}
        </div>

        {/* Sorting Dropdown */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={16} style={{ color: 'hsl(var(--text-muted))' }} />
          <select
            value={sortBy}
            onChange={handleSortChange}
            className="input-field"
            style={{ width: '150px', height: '36px', padding: '0 0.5rem', fontSize: '0.85rem', borderRadius: 'var(--radius-md)' }}
          >
            <option value="PRODUCT_ID">Default Sort</option>
            <option value="PRICE">Price</option>
          </select>
          <button
            onClick={toggleSortDir}
            className="btn btn-secondary"
            style={{ width: '36px', height: '36px', padding: 0, borderRadius: 'var(--radius-md)' }}
            title={sortDir === 'ASC' ? 'Sort Ascending' : 'Sort Descending'}
          >
            <ArrowUpDown size={16} />
          </button>
        </div>
      </div>

      {/* Database Empty Seeding Prompt */}
      {usingFallback && (
        <div style={{
          padding: '0.75rem 1.5rem',
          backgroundColor: 'hsl(var(--warning) / 0.1)',
          border: '1px solid hsl(var(--warning) / 0.3)',
          borderRadius: 'var(--radius-md)',
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          fontSize: '0.85rem'
        }}>
          <p style={{ color: 'hsl(var(--warning))' }}>
            <strong>Local Sandbox Mode:</strong> The frontend is running in visual-only mode. Connect your backend API on port 8080 to query the live services.
          </p>
          {user?.roles?.includes('ADMIN') && (
            <Link to="/admin" className="btn btn-primary" style={{ padding: '0.3rem 0.75rem', fontSize: '0.75rem', borderRadius: 'var(--radius-sm)' }}>
              Seed Live Database
            </Link>
          )}
        </div>
      )}

      {/* Catalog Display */}
      {loading ? (
        <div className="product-grid">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card" style={{ height: '380px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="skeleton" style={{ height: '180px', width: '100%', borderRadius: 'var(--radius-md)' }}></div>
              <div className="skeleton" style={{ height: '24px', width: '60%' }}></div>
              <div className="skeleton" style={{ height: '16px', width: '100%' }}></div>
              <div className="skeleton" style={{ height: '16px', width: '80%' }}></div>
              <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'space-between' }}>
                <div className="skeleton" style={{ height: '24px', width: '25%' }}></div>
                <div className="skeleton" style={{ height: '36px', width: '40%' }}></div>
              </div>
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem 1rem', border: '1px dashed hsl(var(--border))', borderRadius: 'var(--radius-lg)' }}>
          <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '1.2rem', marginBottom: '1rem' }}>
            No products found matching your search.
          </p>
          <button onClick={() => { setSelectedCategory(null); setSelectedCategory(null); }} className="btn btn-primary">
            Clear Filters
          </button>
        </div>
      ) : (
        <>
          <div className="product-grid">
            {products.map((product) => (
              <div key={product.productId} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                {/* Product Image */}
                <div style={{
                  position: 'relative',
                  width: '100%',
                  height: '180px',
                  borderRadius: 'var(--radius-md)',
                  overflow: 'hidden',
                  marginBottom: '1rem',
                  backgroundColor: 'hsl(var(--bg-surface))'
                }}>
                  <img
                    src={getProductImage(product.name, selectedCategory ? categories.find(c => c.categoryId === selectedCategory)?.name : '')}
                    alt={product.name}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                  />
                  {/* Wishlist Button */}
                  <button
                    onClick={() => isWishlisted(product.productId) ? null : addToWishlist(product)}
                    style={{
                      position: 'absolute',
                      top: '10px',
                      right: '10px',
                      width: '36px',
                      height: '36px',
                      borderRadius: 'var(--radius-full)',
                      background: 'rgba(9, 9, 11, 0.6)',
                      border: '1px solid rgba(255, 255, 255, 0.1)',
                      color: isWishlisted(product.productId) ? 'hsl(var(--error))' : '#fff',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer',
                      backdropFilter: 'blur(4px)',
                      transition: 'var(--transition-fast)'
                    }}
                  >
                    <Heart size={18} fill={isWishlisted(product.productId) ? 'currentColor' : 'none'} />
                  </button>
                </div>

                {/* Product Info */}
                <Link to={`/product/${product.productId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                  <h3 style={{
                    fontSize: '1.15rem',
                    fontWeight: 700,
                    marginBottom: '0.5rem',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                  }}>
                    {product.name}
                  </h3>
                </Link>
                <p style={{
                  color: 'hsl(var(--text-secondary))',
                  fontSize: '0.85rem',
                  lineHeight: '1.4',
                  marginBottom: '1.25rem',
                  height: '40px',
                  overflow: 'hidden',
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical'
                }}>
                  {product.description || 'No description available for this item.'}
                </p>

                {/* Action Row */}
                <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', pt: '0.5rem' }}>
                  <span style={{ fontSize: '1.25rem', fontWeight: 800 }}>
                    ₹{product.price.toLocaleString('en-IN')}
                  </span>
                  <button
                    onClick={() => addToCart(product, 1)}
                    className="btn btn-primary"
                    style={{
                      padding: '0.4rem 1rem',
                      fontSize: '0.85rem',
                      borderRadius: 'var(--radius-md)'
                    }}
                  >
                    <ShoppingCart size={14} /> Add to Cart
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '3rem' }}>
              <button
                disabled={page === 0}
                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                className="btn btn-secondary"
                style={{ padding: '0.5rem 1rem', borderRadius: 'var(--radius-md)', opacity: page === 0 ? 0.5 : 1 }}
              >
                <ChevronLeft size={16} /> Prev
              </button>
              <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>
                Page {page + 1} of {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(prev => Math.min(totalPages - 1, prev + 1))}
                className="btn btn-secondary"
                style={{ padding: '0.5rem 1rem', borderRadius: 'var(--radius-md)', opacity: page >= totalPages - 1 ? 0.5 : 1 }}
              >
                Next <ChevronRight size={16} />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};
export default Home;
