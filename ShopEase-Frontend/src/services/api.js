// const BASE_URL = 'http://localhost:8080';

// class ApiClient {
//   constructor() {
//     this.accessToken = localStorage.getItem('access_token') || null;
//     this.refreshToken = localStorage.getItem('refresh_token') || null;
//   }

//   setTokens(access, refresh) {
//     this.accessToken = access;
//     this.refreshToken = refresh;
//     if (access) localStorage.setItem('access_token', access);
//     else localStorage.removeItem('access_token');
//     if (refresh) localStorage.setItem('refresh_token', refresh);
//     else localStorage.removeItem('refresh_token');
//   }

//   clearTokens() {
//     this.accessToken = null;
//     this.refreshToken = null;
//     localStorage.removeItem('access_token');
//     localStorage.removeItem('refresh_token');
//     localStorage.removeItem('shopease_user');
//   }

//   async request(path, options = {}) {
//     const url = `${BASE_URL}${path}`;
//     const headers = {
//       'Content-Type': 'application/json',
//       ...options.headers,
//     };

//     if (this.accessToken) {
//       headers['Authorization'] = `Bearer ${this.accessToken}`;
//     }

//     const config = {
//       ...options,
//       headers,
//       credentials: 'include', // Important for HttpOnly cookies
//     };

//     if (options.body && typeof options.body === 'object') {
//       config.body = JSON.stringify(options.body);
//     }

//     try {
//       let response = await fetch(url, config);

//       // Handle token expiration (401)
//       if (response.status === 401 && this.refreshToken) {
//         console.warn('Access token expired, attempting refresh...');
//         const refreshSuccess = await this.refreshTokens();
//         if (refreshSuccess) {
//           // Retry the request with the new access token
//           headers['Authorization'] = `Bearer ${this.accessToken}`;
//           response = await fetch(url, config);
//         } else {
//           this.clearTokens();
//           // Trigger redirect or auth state refresh in the application
//           window.dispatchEvent(new Event('auth-logout'));
//         }
//       }

//       if (!response.ok) {
//         let errorData = {};
//         try {
//           errorData = await response.json();
//         } catch {
//           // Fallback if response is plain text
//           try {
//             const text = await response.text();
//             errorData = { message: text };
//           } catch {
//             errorData = { message: `HTTP error status ${response.status}` };
//           }
//         }
//         const error = new Error(errorData.message || `HTTP error ${response.status}`);
//         error.status = response.status;
//         error.data = errorData;
//         throw error;
//       }

//       // Handle empty response or no-content
//       if (response.status === 204) {
//         return null;
//       }

//       const contentType = response.headers.get('content-type');
//       if (contentType && contentType.includes('application/json')) {
//         return await response.json();
//       } else {
//         return await response.text();
//       }
//     } catch (error) {
//       console.error(`API Request Failed: ${path}`, error);
//       throw error;
//     }
//   }

//   async refreshTokens() {
//     try {
//       const url = `${BASE_URL}/auth/api/v1/auth/refresh`;
//       const response = await fetch(url, {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//           'X-Refresh-Token': this.refreshToken || '',
//         },
//         body: JSON.stringify({ refreshToken: this.refreshToken }),
//         credentials: 'include',
//       });

//       if (!response.ok) {
//         return false;
//       }

//       // Check if cookies are set, or if it returns tokens in headers/body
//       const authHeader = response.headers.get('Authorization');
//       let newAccess = null;
//       if (authHeader && authHeader.startsWith('Bearer ')) {
//         newAccess = authHeader.substring(7);
//       }

//       // In some spring configurations it might return details in body
//       try {
//         const body = await response.json();
//         if (body.accessToken) newAccess = body.accessToken;
//       } catch {
//         // No body tokens returned, fallback to browser session cookies
//         newAccess = null;
//       }

//       if (newAccess) {
//         this.setTokens(newAccess, this.refreshToken);
//         return true;
//       }

//       // If it returns 200/204 but cookies are used (HttpOnly)
//       return true;
//     } catch (err) {
//       console.error('Failed to refresh tokens', err);
//       return false;
//     }
//   }

//   get(path, options = {}) {
//     return this.request(path, { ...options, method: 'GET' });
//   }

//   post(path, body, options = {}) {
//     return this.request(path, { ...options, method: 'POST', body });
//   }

//   put(path, body, options = {}) {
//     return this.request(path, { ...options, method: 'PUT', body });
//   }

//   patch(path, body, options = {}) {
//     return this.request(path, { ...options, method: 'PATCH', body });
//   }

//   delete(path, options = {}) {
//     return this.request(path, { ...options, method: 'DELETE' });
//   }
// }

// export const api = new ApiClient();
// export default api;
const BASE_URL = 'http://localhost:8080';

class ApiClient {
  constructor() {
    this.accessToken = localStorage.getItem('access_token') || null;
    this.refreshToken = localStorage.getItem('refresh_token') || null;
  }

  setTokens(access, refresh) {
    this.accessToken = access;
    this.refreshToken = refresh;

    if (access) localStorage.setItem('access_token', access);
    else localStorage.removeItem('access_token');

    if (refresh) localStorage.setItem('refresh_token', refresh);
    else localStorage.removeItem('refresh_token');
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;

    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('shopease_user');
  }

  async request(path, options = {}) {
    const url = `${BASE_URL}${path}`;

    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    const config = {
      ...options,
      headers,
    };

    if (options.body && typeof options.body === 'object') {
      config.body = JSON.stringify(options.body);
    }

    try {
      let response = await fetch(url, config);

      // 🔥 AUTO REFRESH ON 401
      if (response.status === 401 && this.refreshToken) {
        const refreshed = await this.refreshTokens();

        if (refreshed) {
          const retryHeaders = {
            ...headers,
            Authorization: `Bearer ${this.accessToken}`,
          };

          response = await fetch(url, {
            ...config,
            headers: retryHeaders,
          });
        } else {
          this.clearTokens();
          window.dispatchEvent(new Event('auth-logout'));
        }
      }

      if (!response.ok) {
        let errorData = {};
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: `HTTP error ${response.status}` };
        }

        const error = new Error(errorData.message || 'Request failed');
        error.status = response.status;
        error.data = errorData;
        throw error;
      }

      if (response.status === 204) return null;

      const contentType = response.headers.get('content-type');

      if (contentType?.includes('application/json')) {
        return await response.json();
      }

      return await response.text();
    } catch (err) {
      console.error(`API Error: ${path}`, err);
      throw err;
    }
  }

  async refreshTokens() {
    try {
      const response = await fetch(`${BASE_URL}/auth/api/v1/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${this.refreshToken}`,
        },
      });

      if (!response.ok) return false;

      const data = await response.json();

      if (data.accessToken) {
        this.setTokens(data.accessToken, data.refreshToken || this.refreshToken);
        return true;
      }

      return false;
    } catch (err) {
      console.error('Refresh token failed', err);
      return false;
    }
  }

  get(path, options = {}) {
    return this.request(path, { ...options, method: 'GET' });
  }

  post(path, body, options = {}) {
    return this.request(path, { ...options, method: 'POST', body });
  }

  put(path, body, options = {}) {
    return this.request(path, { ...options, method: 'PUT', body });
  }

  patch(path, body, options = {}) {
    return this.request(path, { ...options, method: 'PATCH', body });
  }

  delete(path, options = {}) {
    return this.request(path, { ...options, method: 'DELETE' });
  }
}

export const api = new ApiClient();
export default api;