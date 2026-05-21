import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { CreditCard, CheckCircle2, ChevronRight, AlertTriangle, AlertCircle, Sparkles, Building, Phone, MapPin } from 'lucide-react';
import api from '../services/api';

export const Checkout = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Retrieve state passed from Cart/Buy Now
  const checkoutData = location.state || null;
  const isMocked = checkoutData?.isMocked || false;

  const [shippingAddress, setShippingAddress] = useState({
    street: '',
    city: '',
    zip: '',
    phone: ''
  });
  
  // Payment States
  const [paymentStep, setPaymentStep] = useState('shipping'); // 'shipping', 'payment', 'success', 'failed'
  const [upiId, setUpiId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Received backend gateway details
  const [gatewayOrderId, setGatewayOrderId] = useState('');
  const [simulatingGateway, setSimulatingGateway] = useState(false);

  useEffect(() => {
    // If no checkout details are present, redirect back to cart
    if (!checkoutData) {
      navigate('/cart');
    }
  }, [checkoutData, navigate]);

  if (!checkoutData) return null;

  const { orderId, paymentId, grandTotal, items } = checkoutData;

  const handleShippingSubmit = (e) => {
    e.preventDefault();
    setPaymentStep('payment');
  };

  const handlePayNow = async (e) => {
    e.preventDefault();
    if (!upiId) {
      setError("Please enter a valid UPI ID");
      return;
    }

    setLoading(true);
    setError(null);

    if (isMocked) {
      // Simulate payment delay
      setTimeout(() => {
        setGatewayOrderId('MOCK_ORDER_' + Math.random().toString(36).substring(7).toUpperCase());
        setSimulatingGateway(true);
        setLoading(false);
      }, 1000);
      return;
    }

    try {
      // 1. Call Payment Service Checkout
      // POST /payment/api/v1/payment/checkout
      const checkoutRes = await api.post('/payment/api/v1/payment/checkout', {
        paymentId: paymentId,
        gateway: 'dummy-gateway',
        service: 'UPI',
        detail: {
          type: 'UPI',
          upiId: upiId
        }
      });

      // Returns map containing gatewayOrderId
      console.log('Payment Checkout Response:', checkoutRes);
      if (checkoutRes && checkoutRes.gatewayOrderId) {
        setGatewayOrderId(checkoutRes.gatewayOrderId);
        setSimulatingGateway(true);
      } else {
        throw new Error("Failed to initialize gateway order.");
      }
    } catch (err) {
      console.error(err);
      setError(err.message || 'Payment initiation failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleCompletePaymentSimulated = async () => {
    setLoading(true);
    setError(null);

    if (isMocked) {
      setTimeout(() => {
        setSimulatingGateway(false);
        setPaymentStep('success');
        setLoading(false);
      }, 1500);
      return;
    }

    try {
      // 2. Call the Dummy Gateway API
      // POST /payment/api/payment/gateway/dummy-gateway/pay?gatewayOrderId=...
      const gatewayRes = await api.post(`/payment/api/payment/gateway/dummy-gateway/pay?gatewayOrderId=${gatewayOrderId}`);
      console.log('Dummy Gateway Complete Response:', gatewayRes);
      
      // 3. Verify payment on backend
      // POST /payment/api/v1/payment/verify
      await api.post('/payment/api/v1/payment/verify', {
        paymentId: paymentId,
        gatewayOrderId: gatewayRes.gatewayOrderId,
        gatewayPaymentId: gatewayRes.gatewayPaymentId,
        gatewaySignature: gatewayRes.gatewaySignature
      });

      // 4. Update order status to PAID on backend order-service
      // PUT /order/api/v1/order/change-status
      try {
        await api.put('/order/api/v1/order/change-status', {
          orderId: orderId,
          status: 'PAID'
        });
      } catch (errStatus) {
        console.warn('Failed to update order status (already updated by event broker?), ignoring.', errStatus);
      }

      setSimulatingGateway(false);
      setPaymentStep('success');
    } catch (err) {
      console.error(err);
      setError(err.message || 'Payment verification failed.');
      setSimulatingGateway(false);
      setPaymentStep('failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ maxWidth: '900px', margin: '1rem auto' }}>
      {/* Step Progress */}
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            backgroundColor: paymentStep === 'shipping' ? 'hsl(var(--primary))' : 'hsl(var(--success))',
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '0.85rem',
            fontWeight: 700
          }}>
            {paymentStep !== 'shipping' ? '✓' : '1'}
          </div>
          <span style={{ fontWeight: 600, fontSize: '0.9rem', color: paymentStep === 'shipping' ? 'hsl(var(--text-primary))' : 'hsl(var(--text-secondary))' }}>
            Shipping
          </span>
        </div>
        <ChevronRight size={16} style={{ color: 'hsl(var(--text-muted))' }} />
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            backgroundColor: paymentStep === 'payment' ? 'hsl(var(--primary))' : paymentStep === 'success' ? 'hsl(var(--success))' : 'hsl(var(--border))',
            color: paymentStep === 'payment' || paymentStep === 'success' ? '#fff' : 'hsl(var(--text-muted))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '0.85rem',
            fontWeight: 700,
            border: paymentStep === 'shipping' ? '1px solid hsl(var(--border))' : 'none'
          }}>
            {paymentStep === 'success' ? '✓' : '2'}
          </div>
          <span style={{ fontWeight: 600, fontSize: '0.9rem', color: paymentStep === 'payment' ? 'hsl(var(--text-primary))' : 'hsl(var(--text-muted))' }}>
            Payment
          </span>
        </div>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: 'hsl(var(--error) / 0.1)',
          border: '1px solid hsl(var(--error) / 0.3)',
          borderRadius: 'var(--radius-md)',
          color: 'hsl(var(--error))',
          fontSize: '0.9rem',
          marginBottom: '2rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Main Checkout Split */}
      {paymentStep === 'shipping' && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2.5rem' }}>
          {/* Shipping Form */}
          <div className="card glass-panel" style={{ flex: '1.5 1 450px', padding: '2rem' }}>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <MapPin size={22} style={{ color: 'hsl(var(--primary))' }} /> Shipping Address
            </h2>
            <form onSubmit={handleShippingSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="form-group">
                <label htmlFor="street">Street Address</label>
                <input
                  id="street"
                  required
                  type="text"
                  placeholder="123 Luxury Avenue, Penthouse B"
                  value={shippingAddress.street}
                  onChange={(e) => setShippingAddress({ ...shippingAddress, street: e.target.value })}
                  className="input-field"
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1.5 }}>
                  <label htmlFor="city">City</label>
                  <input
                    id="city"
                    required
                    type="text"
                    placeholder="Mumbai"
                    value={shippingAddress.city}
                    onChange={(e) => setShippingAddress({ ...shippingAddress, city: e.target.value })}
                    className="input-field"
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label htmlFor="zip">ZIP / Postal Code</label>
                  <input
                    id="zip"
                    required
                    type="text"
                    maxLength={6}
                    placeholder="400001"
                    value={shippingAddress.zip}
                    onChange={(e) => setShippingAddress({ ...shippingAddress, zip: e.target.value })}
                    className="input-field"
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="phone">Phone Number</label>
                <div style={{ position: 'relative' }}>
                  <Phone size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'hsl(var(--text-muted))' }} />
                  <input
                    id="phone"
                    required
                    type="tel"
                    placeholder="9876543210"
                    value={shippingAddress.phone}
                    onChange={(e) => setShippingAddress({ ...shippingAddress, phone: e.target.value })}
                    className="input-field"
                    style={{ paddingLeft: '2.5rem' }}
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem', alignSelf: 'flex-start' }}>
                Continue to Payment <ChevronRight size={16} />
              </button>
            </form>
          </div>

          {/* Cart Sidebar Review */}
          <div style={{ flex: '1 1 300px' }}>
            <div className="card glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <h3 style={{ fontSize: '1.1rem', borderBottom: '1px solid hsl(var(--border))', paddingBottom: '0.75rem' }}>
                Review Order Items
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '250px', overflowY: 'auto' }}>
                {items.map(item => (
                  <div key={item.productId} style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                    <div style={{ width: '42px', height: '42px', borderRadius: 'var(--radius-sm)', background: 'hsl(var(--bg-surface))', overflow: 'hidden', flexShrink: 0 }}>
                      <img src={item.image || 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=150'} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ fontSize: '0.85rem', fontWeight: 700, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.name}</p>
                      <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))' }}>Qty: {item.quantity}</p>
                    </div>
                    <span style={{ fontSize: '0.85rem', fontWeight: 800 }}>₹{(item.price * item.quantity).toLocaleString('en-IN')}</span>
                  </div>
                ))}
              </div>
              <div style={{ borderTop: '1px solid hsl(var(--border))', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', fontWeight: 800 }}>
                <span>Order Total</span>
                <span style={{ color: 'hsl(var(--warning))' }}>₹{grandTotal.toLocaleString('en-IN')}</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Payment Selection Form */}
      {paymentStep === 'payment' && (
        <div style={{ maxWidth: '500px', margin: '0 auto' }}>
          <div className="card glass-panel" style={{ padding: '2rem' }}>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <CreditCard size={22} style={{ color: 'hsl(var(--primary))' }} /> Select Payment Method
            </h2>

            {isMocked && (
              <div style={{ padding: '0.5rem 1rem', backgroundColor: 'hsl(var(--warning) / 0.1)', border: '1px solid hsl(var(--warning) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--warning))', fontSize: '0.75rem', marginBottom: '1.5rem' }}>
                <strong>Local Mode:</strong> Checkout is mocked. Simulated payment will run.
              </div>
            )}

            <form onSubmit={handlePayNow} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{
                border: '1px solid hsl(var(--primary) / 0.5)',
                backgroundColor: 'hsl(var(--primary) / 0.05)',
                borderRadius: 'var(--radius-md)',
                padding: '1rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem'
              }}>
                <Building size={20} style={{ color: 'hsl(var(--primary))' }} />
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 700, fontSize: '0.9rem' }}>Instant UPI Checkout</p>
                  <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))' }}>Secure transfer via GPay, PhonePe or Paytm</p>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="upi">Enter UPI Virtual Address (VPA)</label>
                <input
                  id="upi"
                  required
                  type="text"
                  placeholder="username@upi"
                  value={upiId}
                  onChange={(e) => setUpiId(e.target.value)}
                  className="input-field"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid hsl(var(--border))', paddingTop: '1rem', marginTop: '0.5rem' }}>
                <span style={{ color: 'hsl(var(--text-secondary))', fontWeight: 600 }}>Amount to Pay</span>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, color: 'hsl(var(--warning))' }}>
                  ₹{grandTotal.toLocaleString('en-IN')}
                </span>
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button
                  type="button"
                  onClick={() => setPaymentStep('shipping')}
                  className="btn btn-secondary"
                  style={{ flex: 1 }}
                >
                  Back
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                  style={{ flex: 1.5 }}
                >
                  {loading ? 'Initiating UPI...' : 'Pay Now'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Payment Success Page */}
      {paymentStep === 'success' && (
        <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem', maxWidth: '500px', margin: '0 auto' }}>
          <div className="card glass-panel" style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '1.5rem',
            padding: '3rem 2rem'
          }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'hsl(var(--success) / 0.15)',
              color: 'hsl(var(--success))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '2px solid hsl(var(--success) / 0.3)',
              boxShadow: '0 0 20px 2px hsl(var(--success) / 0.15)'
            }}>
              <CheckCircle2 size={36} />
            </div>

            <div>
              <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem' }}>Payment Successful!</h2>
              <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '0.9rem' }}>
                Thank you for your order! Your payment has been successfully processed and verified.
              </p>
            </div>

            <div className="glass-panel" style={{
              width: '100%',
              borderRadius: 'var(--radius-md)',
              padding: '1rem',
              textAlign: 'left',
              fontSize: '0.85rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.5rem',
              backgroundColor: 'hsl(var(--bg-surface))'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'hsl(var(--text-muted))' }}>Order ID:</span>
                <span style={{ fontWeight: 700 }}>{orderId.substring(0, 18)}...</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'hsl(var(--text-muted))' }}>Transaction Total:</span>
                <span style={{ fontWeight: 700, color: 'hsl(var(--warning))' }}>₹{grandTotal.toLocaleString('en-IN')}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'hsl(var(--text-muted))' }}>Method:</span>
                <span style={{ fontWeight: 700 }}>UPI ({upiId || 'test@upi'})</span>
              </div>
            </div>

            <Link to="/" className="btn btn-primary" style={{ width: '100%', borderRadius: 'var(--radius-md)' }}>
              Continue Shopping
            </Link>
          </div>
        </div>
      )}

      {/* Payment Failed Page */}
      {paymentStep === 'failed' && (
        <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem', maxWidth: '500px', margin: '0 auto' }}>
          <div className="card glass-panel" style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '1.5rem',
            padding: '3rem 2rem'
          }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'hsl(var(--error) / 0.15)',
              color: 'hsl(var(--error))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '2px solid hsl(var(--error) / 0.3)'
            }}>
              <AlertTriangle size={36} />
            </div>

            <div>
              <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem' }}>Payment Failed</h2>
              <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '0.9rem' }}>
                Your payment transaction was declined or could not be verified by the UPI gateway bank.
              </p>
            </div>

            <button
              onClick={() => setPaymentStep('payment')}
              className="btn btn-primary"
              style={{ width: '100%', borderRadius: 'var(--radius-md)' }}
            >
              Retry Payment
            </button>
            <Link to="/" className="btn btn-secondary" style={{ width: '100%', borderRadius: 'var(--radius-md)' }}>
              Cancel & Return Home
            </Link>
          </div>
        </div>
      )}

      {/* Gateway Simulation Modal */}
      {simulatingGateway && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '420px', border: '1px solid hsl(var(--primary))' }}>
            <div style={{
              background: 'linear-gradient(135deg, hsl(var(--primary) / 0.3) 0%, transparent 100%)',
              padding: '1.5rem',
              borderBottom: '1px solid hsl(var(--border))',
              position: 'relative'
            }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.15rem' }}>
                <Sparkles size={18} style={{ color: 'hsl(var(--warning))' }} /> External UPI Gateway
              </h3>
              <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))', marginTop: '0.25rem' }}>
                Simulating third-party UPI validation provider
              </p>
            </div>

            <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="glass-panel" style={{
                borderRadius: 'var(--radius-md)',
                padding: '1rem',
                fontSize: '0.8rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.4rem',
                backgroundColor: 'hsl(var(--bg-surface))'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'hsl(var(--text-muted))' }}>Merchant:</span>
                  <span style={{ fontWeight: 700 }}>ShopEase Inc.</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'hsl(var(--text-muted))' }}>Gateway Ref:</span>
                  <span style={{ fontWeight: 700, fontFamily: 'monospace' }}>{gatewayOrderId.substring(0, 16)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'hsl(var(--text-muted))' }}>UPI ID:</span>
                  <span style={{ fontWeight: 700 }}>{upiId}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid hsl(var(--border))', paddingTop: '0.5rem', marginTop: '0.25rem' }}>
                  <span style={{ color: 'hsl(var(--text-muted))' }}>Grand Total:</span>
                  <span style={{ fontWeight: 800, color: 'hsl(var(--warning))' }}>₹{grandTotal.toLocaleString('en-IN')}</span>
                </div>
              </div>

              {loading ? (
                <div style={{ textAlign: 'center', padding: '1rem 0' }}>
                  <div style={{
                    width: '32px',
                    height: '32px',
                    border: '3px solid hsl(var(--primary) / 0.2)',
                    borderTopColor: 'hsl(var(--primary))',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite',
                    margin: '0 auto 0.5rem'
                  }}></div>
                  <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))' }}>
                    Verifying HMAC hash and transaction details...
                  </p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <button
                    onClick={handleCompletePaymentSimulated}
                    className="btn btn-primary"
                    style={{ width: '100%', backgroundColor: 'hsl(var(--success))', hoverColor: 'hsl(var(--success))' }}
                  >
                    Authorize & Approve Payment
                  </button>
                  <button
                    onClick={() => { setSimulatingGateway(false); setError("Payment cancelled by user in gateway"); }}
                    className="btn btn-secondary"
                    style={{ width: '100%' }}
                  >
                    Decline Transaction
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default Checkout;
