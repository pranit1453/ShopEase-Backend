import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Shield, Plus, Database, Users, CheckCircle2, ShieldAlert, Edit2, Trash2, X } from 'lucide-react';
import api from '../services/api';

export const AdminDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Redirect if not admin
  useEffect(() => {
    if (!user || !user.roles?.includes('ADMIN')) {
      navigate('/');
    }
  }, [user, navigate]);

  // Seeding Catalog Mock Data list
  const mockCategoriesToSeed = [
    { name: 'Electronics', description: 'Gadgets and top tier electronics gear.' },
    { name: 'Apparel', description: 'Designer clothing, wear and sneaker footwear.' },
    { name: 'Home', description: 'Modern home furniture, cushions and decorations.' },
    { name: 'Accessories', description: 'Premium quartz watches, leather wallets and leather bags.' }
  ];

  // Forms State
  const [catForm, setCatForm] = useState({ name: '', description: '' });
  const [prodForm, setProdForm] = useState({ name: '', description: '', price: '', quantity: '', categoryId: '' });

  // Edit modals state
  const [editCat, setEditCat] = useState(null);   // { categoryId, name, description }
  const [editProd, setEditProd] = useState(null);  // { productId, name, description, price, quantity }

  // Lists
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [users, setUsers] = useState([]);

  // Status states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Active tab
  const [activeTab, setActiveTab] = useState('categories'); // 'categories' | 'products' | 'users'

  // Fetch lists
  const refreshData = async () => {
    try {
      const cats = await api.get('/product/api/v1/category/fetch/all');
      if (cats && cats.content) setCategories(cats.content);
      else if (Array.isArray(cats)) setCategories(cats);

      const prods = await api.get('/product/api/v1/product/fetch/all?size=50');
      if (prods && prods.content) setProducts(prods.content);

      const usersList = await api.get('/auth/api/v1/user/all');
      if (usersList && usersList.content) {
        setUsers(usersList.content);
      }
    } catch (err) {
      console.warn("Failed to fetch lists (using local fallback mode).", err);
    }
  };

  useEffect(() => {
    if (user && user.roles?.includes('ADMIN')) {
      queueMicrotask(() => {
        refreshData();
      });
    }
  }, [user]);

  // ─── Create Category ────────────────────────────────────────────────────────
  const handleCatSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.post('/product/api/v1/category/create', catForm);
      setSuccess(`Category "${catForm.name}" created successfully!`);
      setCatForm({ name: '', description: '' });
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to create category.');
    } finally { setLoading(false); }
  };

  // ─── Update Category ────────────────────────────────────────────────────────
  const handleCatUpdate = async (e) => {
    e.preventDefault();
    if (!editCat) return;
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.put(`/product/api/v1/category/update/${editCat.categoryId}`, {
        name: editCat.name,
        description: editCat.description
      });
      setSuccess(`Category "${editCat.name}" updated!`);
      setEditCat(null);
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to update category.');
    } finally { setLoading(false); }
  };

  // ─── Delete Category ────────────────────────────────────────────────────────
  const handleCatDelete = async (categoryId, name) => {
    if (!window.confirm(`Delete category "${name}"? This may affect products in this category.`)) return;
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.delete(`/product/api/v1/category/delete/${categoryId}`);
      setSuccess(`Category "${name}" deleted.`);
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to delete category.');
    } finally { setLoading(false); }
  };

  // ─── Create Product ─────────────────────────────────────────────────────────
  const handleProdSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null); setSuccess(null);
    try {
      const payload = {
        ...prodForm,
        price: parseFloat(prodForm.price),
        quantity: parseInt(prodForm.quantity)
      };
      await api.post('/product/api/v1/product/create', payload);
      setSuccess(`Product "${prodForm.name}" created successfully!`);
      setProdForm({ name: '', description: '', price: '', quantity: '', categoryId: '' });
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to create product. Make sure Category ID is correct.');
    } finally { setLoading(false); }
  };

  // ─── Update Product ─────────────────────────────────────────────────────────
  const handleProdUpdate = async (e) => {
    e.preventDefault();
    if (!editProd) return;
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.put(`/product/api/v1/product/update/${editProd.productId}`, {
        name: editProd.name,
        description: editProd.description,
        price: parseFloat(editProd.price),
        quantity: parseInt(editProd.quantity)
      });
      setSuccess(`Product "${editProd.name}" updated!`);
      setEditProd(null);
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to update product.');
    } finally { setLoading(false); }
  };

  // ─── Delete Product ─────────────────────────────────────────────────────────
  const handleProdDelete = async (productId, name) => {
    if (!window.confirm(`Delete product "${name}"? This cannot be undone.`)) return;
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.delete(`/product/api/v1/product/remove/${productId}`);
      setSuccess(`Product "${name}" deleted.`);
      refreshData();
    } catch (err) {
      setError(err.message || 'Failed to delete product.');
    } finally { setLoading(false); }
  };

  // ─── Change User Role ───────────────────────────────────────────────────────
  const handleUpdateRole = async (userId, targetRoleId) => {
    setLoading(true); setError(null); setSuccess(null);
    try {
      await api.put(`/auth/api/v1/user/${userId}/role/${targetRoleId}`);
      setSuccess("User role updated successfully!");
      refreshData();
    } catch (err) {
      setError(err.message || "Failed to update user role.");
    } finally { setLoading(false); }
  };

  // ─── Live Catalog Seeder ────────────────────────────────────────────────────
  const handleSeedCatalog = async () => {
    setLoading(true); setError(null); setSuccess(null);
    try {
      const catRes = await api.post('/product/api/v1/category/list/create', mockCategoriesToSeed);
      console.log('Seeded Categories:', catRes);
      const categoryMap = {};
      catRes.forEach(c => { categoryMap[c.name] = c.categoryId; });

      const mockProductsToSeed = [
        { name: 'ShopEase Premium Headphones', description: 'Wireless over-ear headphones with advanced active noise cancellation.', price: 14999.00, quantity: 50, categoryId: categoryMap['Electronics'] },
        { name: 'Veloce Running Shoes', description: 'Ultra-lightweight mesh trainers for speed running and everyday comfort.', price: 7999.00, quantity: 120, categoryId: categoryMap['Apparel'] },
        { name: 'ShopEase Chrono Classic', description: 'Elegant water-resistant quartz watch with a genuine leather strap.', price: 12499.00, quantity: 30, categoryId: categoryMap['Accessories'] },
        { name: 'Aero Slim Laptop Pro', description: '14-inch thin laptop with 16GB RAM, 512GB SSD, and powerful quad-core processor.', price: 64999.00, quantity: 15, categoryId: categoryMap['Electronics'] },
        { name: 'Ergonomic Mesh Chair', description: 'Comfortable lumbar support office chair with fully adjustable armrests.', price: 18999.00, quantity: 25, categoryId: categoryMap['Home'] },
        { name: 'Smart Fitness Band V4', description: 'Heart rate tracker, sleep monitor, and 14-day battery backup smartwatch.', price: 3499.00, quantity: 200, categoryId: categoryMap['Accessories'] }
      ].filter(p => p.categoryId !== undefined);

      if (mockProductsToSeed.length > 0) {
        await api.post('/product/api/v1/product/list/create', mockProductsToSeed);
      }
      setSuccess("Database catalog seeded successfully with standard categories and products!");
      refreshData();
    } catch (err) {
      console.error(err);
      setError(err.message || "Seeding failed. Ensure backend services are running and your token is valid.");
    } finally { setLoading(false); }
  };

  if (!user || !user.roles?.includes('ADMIN')) return null;

  const tabBtnStyle = (active) => ({
    padding: '0.5rem 1.25rem',
    borderRadius: 'var(--radius-full)',
    border: active ? 'none' : '1px solid hsl(var(--border))',
    background: active ? 'hsl(var(--primary))' : 'transparent',
    color: active ? '#fff' : 'hsl(var(--text-secondary))',
    fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', transition: 'all 0.2s'
  });

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Title */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid hsl(var(--border))', paddingBottom: '1rem' }}>
        <h1 style={{ fontSize: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Shield style={{ color: 'hsl(var(--warning))' }} /> Admin Management Dashboard
        </h1>
        <button disabled={loading} onClick={handleSeedCatalog} className="btn btn-secondary"
          style={{ border: '1px solid hsl(var(--warning))', color: 'hsl(var(--warning))', display: 'flex', gap: '0.4rem', borderRadius: 'var(--radius-md)' }}>
          <Database size={16} /> Seed Catalog Data
        </button>
      </div>

      {/* Alerts */}
      {error && (
        <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--error))', fontSize: '0.85rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <ShieldAlert size={16} /> {error}
        </div>
      )}
      {success && (
        <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--success) / 0.1)', border: '1px solid hsl(var(--success) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--success))', fontSize: '0.85rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <CheckCircle2 size={16} /> {success}
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.75rem', borderBottom: '1px solid hsl(var(--border))', paddingBottom: '1rem' }}>
        {['categories', 'products', 'users'].map(tab => (
          <button key={tab} style={tabBtnStyle(activeTab === tab)} onClick={() => setActiveTab(tab)}>
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {/* ── CATEGORIES TAB ─────────────────────────────────────────────────────── */}
      {activeTab === 'categories' && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem' }}>
          {/* Create Category Form */}
          <div className="card glass-panel" style={{ flex: '1 1 320px' }}>
            <h2 style={{ fontSize: '1.2rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Plus size={18} style={{ color: 'hsl(var(--primary))' }} /> Create Category
            </h2>
            <form onSubmit={handleCatSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label htmlFor="cat-name">Category Name</label>
                <input id="cat-name" required type="text" placeholder="Electronics" value={catForm.name}
                  onChange={(e) => setCatForm({ ...catForm, name: e.target.value })} className="input-field" />
              </div>
              <div className="form-group">
                <label htmlFor="cat-desc">Description</label>
                <textarea id="cat-desc" required rows={3} placeholder="Devices, laptops, and smart items"
                  value={catForm.description} onChange={(e) => setCatForm({ ...catForm, description: e.target.value })}
                  className="input-field" style={{ resize: 'none', fontFamily: 'inherit' }} />
              </div>
              <button type="submit" disabled={loading} className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
                Create Category
              </button>
            </form>
          </div>

          {/* Categories List */}
          <div className="card glass-panel" style={{ flex: '2 1 450px' }}>
            <h2 style={{ fontSize: '1.2rem', marginBottom: '1.25rem' }}>All Categories ({categories.length})</h2>
            {categories.length === 0 ? (
              <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.9rem' }}>No categories yet. Create one or seed the catalog.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '420px', overflowY: 'auto', paddingRight: '0.5rem' }}>
                {categories.map(cat => (
                  <div key={cat.categoryId} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.75rem', borderRadius: 'var(--radius-md)', border: '1px solid hsl(var(--border))', backgroundColor: 'hsl(var(--bg-secondary))' }}>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ fontWeight: 700, fontSize: '0.95rem' }}>{cat.name}</p>
                      <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{cat.description}</p>
                      <p style={{ fontSize: '0.65rem', fontFamily: 'monospace', color: 'hsl(var(--text-muted))', marginTop: '0.2rem' }}>{cat.categoryId}</p>
                    </div>
                    <button onClick={() => setEditCat({ ...cat })} style={{ padding: '0.35rem 0.6rem', background: 'hsl(var(--primary) / 0.15)', border: '1px solid hsl(var(--primary) / 0.4)', borderRadius: 'var(--radius-sm)', color: 'hsl(var(--primary))', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem' }}>
                      <Edit2 size={13} /> Edit
                    </button>
                    <button onClick={() => handleCatDelete(cat.categoryId, cat.name)} style={{ padding: '0.35rem 0.6rem', background: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-sm)', color: 'hsl(var(--error))', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem' }}>
                      <Trash2 size={13} /> Delete
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── PRODUCTS TAB ───────────────────────────────────────────────────────── */}
      {activeTab === 'products' && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem' }}>
          {/* Create Product Form */}
          <div className="card glass-panel" style={{ flex: '1 1 360px' }}>
            <h2 style={{ fontSize: '1.2rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Plus size={18} style={{ color: 'hsl(var(--primary))' }} /> Add New Product
            </h2>
            <form onSubmit={handleProdSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label htmlFor="prod-name">Product Name</label>
                <input id="prod-name" required type="text" placeholder="ShopEase Wireless Pro" value={prodForm.name}
                  onChange={(e) => setProdForm({ ...prodForm, name: e.target.value })} className="input-field" />
              </div>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label htmlFor="prod-price">Price (INR)</label>
                  <input id="prod-price" required type="number" placeholder="2499.00" value={prodForm.price}
                    onChange={(e) => setProdForm({ ...prodForm, price: e.target.value })} className="input-field" />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label htmlFor="prod-qty">Stock Qty</label>
                  <input id="prod-qty" required type="number" placeholder="50" value={prodForm.quantity}
                    onChange={(e) => setProdForm({ ...prodForm, quantity: e.target.value })} className="input-field" />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="prod-cat">Select Category</label>
                <select id="prod-cat" required value={prodForm.categoryId}
                  onChange={(e) => setProdForm({ ...prodForm, categoryId: e.target.value })} className="input-field">
                  <option value="">-- Choose Category --</option>
                  {categories.map((c) => (
                    <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="prod-desc">Description</label>
                <textarea id="prod-desc" required rows={2} placeholder="Brief summary..." value={prodForm.description}
                  onChange={(e) => setProdForm({ ...prodForm, description: e.target.value })}
                  className="input-field" style={{ resize: 'none', fontFamily: 'inherit' }} />
              </div>
              <button type="submit" disabled={loading} className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
                Create Product
              </button>
            </form>
          </div>

          {/* Products List */}
          <div className="card glass-panel" style={{ flex: '2 1 450px' }}>
            <h2 style={{ fontSize: '1.2rem', marginBottom: '1.25rem' }}>All Products ({products.length})</h2>
            {products.length === 0 ? (
              <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.9rem' }}>No products yet. Create one or seed the catalog.</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid hsl(var(--border))', color: 'hsl(var(--text-muted))' }}>
                      <th style={{ padding: '0.6rem', textAlign: 'left' }}>Name</th>
                      <th style={{ padding: '0.6rem', textAlign: 'right' }}>Price</th>
                      <th style={{ padding: '0.6rem', textAlign: 'right' }}>Qty</th>
                      <th style={{ padding: '0.6rem', textAlign: 'center' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map(p => (
                      <tr key={p.productId} style={{ borderBottom: '1px solid hsl(var(--border) / 0.5)' }}>
                        <td style={{ padding: '0.6rem', fontWeight: 600, maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</td>
                        <td style={{ padding: '0.6rem', textAlign: 'right' }}>₹{p.price?.toLocaleString('en-IN')}</td>
                        <td style={{ padding: '0.6rem', textAlign: 'right' }}>{p.quantity ?? '—'}</td>
                        <td style={{ padding: '0.6rem', textAlign: 'center' }}>
                          <div style={{ display: 'flex', gap: '0.4rem', justifyContent: 'center' }}>
                            <button onClick={() => setEditProd({ ...p })} style={{ padding: '0.3rem 0.55rem', background: 'hsl(var(--primary) / 0.15)', border: '1px solid hsl(var(--primary) / 0.4)', borderRadius: 'var(--radius-sm)', color: 'hsl(var(--primary))', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem' }}>
                              <Edit2 size={12} /> Edit
                            </button>
                            <button onClick={() => handleProdDelete(p.productId, p.name)} style={{ padding: '0.3rem 0.55rem', background: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-sm)', color: 'hsl(var(--error))', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem' }}>
                              <Trash2 size={12} /> Del
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── USERS TAB ──────────────────────────────────────────────────────────── */}
      {activeTab === 'users' && (
        <div className="card glass-panel">
          <h2 style={{ fontSize: '1.2rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Users size={18} style={{ color: 'hsl(var(--primary))' }} /> User Accounts & Roles
          </h2>
          {users.length === 0 ? (
            <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.9rem' }}>No users registered yet, or server connection pending.</p>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid hsl(var(--border))', color: 'hsl(var(--text-muted))' }}>
                    <th style={{ padding: '0.75rem' }}>Name</th>
                    <th style={{ padding: '0.75rem' }}>Username</th>
                    <th style={{ padding: '0.75rem' }}>User UUID</th>
                    <th style={{ padding: '0.75rem' }}>Gender</th>
                    <th style={{ padding: '0.75rem' }}>Roles</th>
                    <th style={{ padding: '0.75rem' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.userId} style={{ borderBottom: '1px solid hsl(var(--border) / 0.5)' }}>
                      <td style={{ padding: '0.75rem', fontWeight: 600 }}>{u.name}</td>
                      <td style={{ padding: '0.75rem' }}>@{u.username}</td>
                      <td style={{ padding: '0.75rem', fontSize: '0.75rem', fontFamily: 'monospace' }}>{u.userId}</td>
                      <td style={{ padding: '0.75rem' }}>{u.gender}</td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>
                          {u.roleWithPermission?.map(r => r.roleName).join(', ') || 'USER'}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button onClick={() => handleUpdateRole(u.userId, '1')} className="btn btn-secondary"
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', borderRadius: 'var(--radius-sm)' }} title="Promote to Admin">
                            Make Admin
                          </button>
                          <button onClick={() => handleUpdateRole(u.userId, '2')} className="btn btn-secondary"
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', borderRadius: 'var(--radius-sm)' }} title="Demote to User">
                            Make User
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ── EDIT CATEGORY MODAL ────────────────────────────────────────────────── */}
      {editCat && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '440px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}><Edit2 size={18} style={{ marginRight: '0.5rem', color: 'hsl(var(--primary))' }} />Edit Category</h3>
              <button onClick={() => setEditCat(null)} style={{ background: 'transparent', border: 'none', color: 'hsl(var(--text-muted))', cursor: 'pointer' }}><X size={20} /></button>
            </div>
            <form onSubmit={handleCatUpdate} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Category Name</label>
                <input required type="text" value={editCat.name} onChange={(e) => setEditCat({ ...editCat, name: e.target.value })} className="input-field" />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea required rows={3} value={editCat.description} onChange={(e) => setEditCat({ ...editCat, description: e.target.value })}
                  className="input-field" style={{ resize: 'none', fontFamily: 'inherit' }} />
              </div>
              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                <button type="button" onClick={() => setEditCat(null)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={loading} className="btn btn-primary">Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── EDIT PRODUCT MODAL ─────────────────────────────────────────────────── */}
      {editProd && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}><Edit2 size={18} style={{ marginRight: '0.5rem', color: 'hsl(var(--primary))' }} />Edit Product</h3>
              <button onClick={() => setEditProd(null)} style={{ background: 'transparent', border: 'none', color: 'hsl(var(--text-muted))', cursor: 'pointer' }}><X size={20} /></button>
            </div>
            <form onSubmit={handleProdUpdate} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Product Name</label>
                <input required type="text" value={editProd.name} onChange={(e) => setEditProd({ ...editProd, name: e.target.value })} className="input-field" />
              </div>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Price (INR)</label>
                  <input required type="number" value={editProd.price} onChange={(e) => setEditProd({ ...editProd, price: e.target.value })} className="input-field" />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Stock Quantity</label>
                  <input required type="number" value={editProd.quantity} onChange={(e) => setEditProd({ ...editProd, quantity: e.target.value })} className="input-field" />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea required rows={3} value={editProd.description} onChange={(e) => setEditProd({ ...editProd, description: e.target.value })}
                  className="input-field" style={{ resize: 'none', fontFamily: 'inherit' }} />
              </div>
              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                <button type="button" onClick={() => setEditProd(null)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={loading} className="btn btn-primary">Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default AdminDashboard;
