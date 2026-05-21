// Dynamic image generator based on product names
export const getProductImage = (name = '', category = '') => {
  const n = name.toLowerCase();
  const c = category.toLowerCase();
  
  if (n.includes('shoe') || n.includes('sneaker') || n.includes('run')) {
    return 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=600&auto=format&fit=crop';
  }
  if (n.includes('phone') || n.includes('mobile') || n.includes('iphone')) {
    return 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=600&auto=format&fit=crop';
  }
  if (n.includes('laptop') || n.includes('macbook') || n.includes('computer')) {
    return 'https://images.unsplash.com/photo-1496181130204-7552cc145cdb?q=80&w=600&auto=format&fit=crop';
  }
  if (n.includes('watch') || n.includes('clock') || n.includes('smartwatch')) {
    return 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=600&auto=format&fit=crop';
  }
  if (n.includes('headphone') || n.includes('audio') || n.includes('earbud')) {
    return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=600&auto=format&fit=crop';
  }
  if (c.includes('electronic') || n.includes('tv') || n.includes('camera')) {
    return 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?q=80&w=600&auto=format&fit=crop';
  }
  if (c.includes('apparel') || c.includes('cloth') || n.includes('shirt') || n.includes('jacket')) {
    return 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?q=80&w=600&auto=format&fit=crop';
  }
  if (c.includes('home') || c.includes('furnitur') || n.includes('chair') || n.includes('sofa')) {
    return 'https://images.unsplash.com/photo-1524758631624-e2822e304c36?q=80&w=600&auto=format&fit=crop';
  }
  // Default general e-commerce fallback
  return 'https://images.unsplash.com/photo-1484704849700-f032a568e944?q=80&w=600&auto=format&fit=crop';
};
