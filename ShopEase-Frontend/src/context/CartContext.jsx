/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
  const { user } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const [wishlistItems, setWishlistItems] = useState([]);

  // Load from localStorage on mount and when user changes
  useEffect(() => {
    const userId = user?.userId || 'guest';
    const savedCart = localStorage.getItem(`shopease_cart_${userId}`);
    const savedWishlist = localStorage.getItem(`shopease_wishlist_${userId}`);
    
    queueMicrotask(() => {
      setCartItems(savedCart ? JSON.parse(savedCart) : []);
      setWishlistItems(savedWishlist ? JSON.parse(savedWishlist) : []);
    });
  }, [user]);

  // Save to localStorage when state changes
  const saveCart = (items) => {
    const userId = user?.userId || 'guest';
    setCartItems(items);
    localStorage.setItem(`shopease_cart_${userId}`, JSON.stringify(items));
  };

  const saveWishlist = (items) => {
    const userId = user?.userId || 'guest';
    setWishlistItems(items);
    localStorage.setItem(`shopease_wishlist_${userId}`, JSON.stringify(items));
  };

  const addToCart = async (product, quantity = 1) => {
    try {
      if (user) {
        // Call backend product-service to reserve stock and add to cart
        await api.post('/product/api/v1/product/add-to-cart', {
          productId: product.productId,
          quantity: quantity
        });
      }
      
      const existing = cartItems.find((item) => item.productId === product.productId);
      let updated;
      if (existing) {
        updated = cartItems.map((item) =>
          item.productId === product.productId
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        updated = [...cartItems, { ...product, quantity }];
      }
      saveCart(updated);
    } catch (err) {
      console.error('Failed to add product to backend cart:', err);
      // fallback to local cart anyway for premium offline feel
      const existing = cartItems.find((item) => item.productId === product.productId);
      let updated;
      if (existing) {
        updated = cartItems.map((item) =>
          item.productId === product.productId
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        updated = [...cartItems, { ...product, quantity }];
      }
      saveCart(updated);
      throw err;
    }
  };

  const removeFromCart = (productId) => {
    const updated = cartItems.filter((item) => item.productId !== productId);
    saveCart(updated);
  };

  const updateQuantity = async (productId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }
    const item = cartItems.find(i => i.productId === productId);
    if (!item) return;

    const delta = quantity - item.quantity;
    try {
      if (user && delta > 0) {
        // Reserve additional stock
        await api.post('/product/api/v1/product/add-to-cart', {
          productId,
          quantity: delta
        });
      }
      // Note: Backend doesn't support easy decrement via add-to-cart directly, 
      // but in local simulation we update the quantity.
      const updated = cartItems.map((item) =>
        item.productId === productId ? { ...item, quantity } : item
      );
      saveCart(updated);
    } catch (err) {
      console.error('Failed to update quantity on backend:', err);
      // Apply local update anyway
      const updated = cartItems.map((item) =>
        item.productId === productId ? { ...item, quantity } : item
      );
      saveCart(updated);
      throw err;
    }
  };

  const addToWishlist = async (product) => {
    try {
      if (user) {
        await api.post('/product/api/v1/product/add-to-wishlist', {
          productId: product.productId
        });
      }
      const existing = wishlistItems.find((item) => item.productId === product.productId);
      if (!existing) {
        saveWishlist([...wishlistItems, product]);
      }
    } catch (err) {
      console.error('Failed to add to wishlist on backend:', err);
      // Local fallback
      const existing = wishlistItems.find((item) => item.productId === product.productId);
      if (!existing) {
        saveWishlist([...wishlistItems, product]);
      }
    }
  };

  const removeFromWishlist = (productId) => {
    const updated = wishlistItems.filter((item) => item.productId !== productId);
    saveWishlist(updated);
  };

  const moveToWishlist = async (product) => {
    try {
      if (user) {
        await api.delete(`/product/api/v1/cart/move-to-wishlist?productId=${product.productId}`);
      }
      removeFromCart(product.productId);
      await addToWishlist(product);
    } catch (err) {
      console.error('Failed to move to wishlist on backend:', err);
      // Local fallback
      removeFromCart(product.productId);
      const existing = wishlistItems.find((item) => item.productId === product.productId);
      if (!existing) {
        saveWishlist([...wishlistItems, product]);
      }
    }
  };

  const clearCart = () => {
    saveCart([]);
  };

  const getCartTotal = () => {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  };

  const getCartCount = () => {
    return cartItems.reduce((sum, item) => sum + item.quantity, 0);
  };

  return (
    <CartContext.Provider
      value={{
        cartItems,
        wishlistItems,
        addToCart,
        removeFromCart,
        updateQuantity,
        addToWishlist,
        removeFromWishlist,
        moveToWishlist,
        clearCart,
        getCartTotal,
        getCartCount,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
