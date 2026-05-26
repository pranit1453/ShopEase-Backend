// /* eslint-disable react-refresh/only-export-components */
// import { createContext, useContext, useState, useEffect } from 'react';
// import api from '../services/api';

// const AuthContext = createContext(null);

// export const AuthProvider = ({ children }) => {
//   const [user, setUser] = useState(() => {
//     const saved = localStorage.getItem('shopease_user');
//     return saved ? JSON.parse(saved) : null;
//   });
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState(null);

//   // Sync token-based logout event from api interceptor
//   useEffect(() => {
//     const handleLogout = () => {
//       setUser(null);
//       api.clearTokens();
//     };

//     window.addEventListener('auth-logout', handleLogout);
//     return () => window.removeEventListener('auth-logout', handleLogout);
//   }, []);

//   const login = async (username, password) => {
//     setLoading(true);
//     setError(null);
//     try {
//       // 1. Call login endpoint
//       const response = await api.post('/auth/api/v1/auth/login', { username, password });
      
//       // The backend returns { username, roles } and sets cookies
//       console.log('Login Response:', response);

//       // 2. Fetch detailed profile info using user search by username
//       const searchResponse = await api.get(`/auth/api/v1/user/all?keyword=${username}`);
//       const userProfile = searchResponse?.content?.find(
//         (u) => u.username.toLowerCase() === username.toLowerCase()
//       );

//       if (!userProfile) {
//         throw new Error('Could not retrieve user profile details.');
//       }

//       const completeUser = {
//         userId: userProfile.userId,
//         name: userProfile.name,
//         username: userProfile.username,
//         gender: userProfile.gender,
//         roles: response.roles || userProfile.roleWithPermission?.map(r => r.roleName) || ['USER'],
//         permissions: userProfile.roleWithPermission?.flatMap(r => r.permissionResponseList?.map(p => p.permissionName)) || [],
//       };

//       setUser(completeUser);
//       localStorage.setItem('shopease_user', JSON.stringify(completeUser));
//       return completeUser;
//     } catch (err) {
//       setError(err.message || 'Login failed. Please check your credentials.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const register = async (signUpData) => {
//     setLoading(true);
//     setError(null);
//     try {
//       // signUpData: { username, name, email, password, confirmPassword, gender }
//       const response = await api.post('/auth/api/v1/auth/register', signUpData);
//       console.log('Register Response:', response);
//       return response;
//     } catch (err) {
//       setError(err.message || 'Registration failed.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const verifyOtp = async (email, otp) => {
//     setLoading(true);
//     setError(null);
//     try {
//       const response = await api.post('/auth/api/v1/auth/verify', { email, otp });
//       console.log('Verification Response:', response);
//       return response;
//     } catch (err) {
//       setError(err.message || 'OTP verification failed.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const resendOtp = async (email) => {
//     setLoading(true);
//     setError(null);
//     try {
//       // Send email directly as request body text (string)
//       const response = await api.post('/auth/api/v1/auth/resend-otp', email);
//       console.log('Resend OTP Response:', response);
//       return response;
//     } catch (err) {
//       setError(err.message || 'Resending OTP failed.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const logout = async () => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/logout', {});
//     } catch (err) {
//       console.error('Logout API failed:', err);
//     } finally {
//       setUser(null);
//       api.clearTokens();
//       setLoading(false);
//     }
//   };

//   const updateProfile = async (name, gender) => {
//     if (!user?.userId) return;
//     setLoading(true);
//     try {
//       const response = await api.put(`/auth/api/v1/user/update/${user.userId}`, {
//         name,
//         username: user.username,
//         gender,
//       });
//       const updatedUser = {
//         ...user,
//         name: response.name || name,
//         gender: response.gender || gender,
//       };
//       setUser(updatedUser);
//       localStorage.setItem('shopease_user', JSON.stringify(updatedUser));
//       return updatedUser;
//     } catch (err) {
//       setError(err.message || 'Failed to update profile.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const changePassword = async (oldPassword, newPassword, confirmPassword) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/update/password', {
//         oldPassword,
//         newPassword,
//         confirmPassword,
//       });
//     } catch (err) {
//       setError(err.message || 'Failed to change password.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const requestPasswordReset = async (email) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/send-otp', { email });
//     } catch (err) {
//       setError(err.message || 'Failed to send OTP.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const verifyPasswordResetOtp = async (email, otp) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/otp/verify', { email, otp });
//     } catch (err) {
//       setError(err.message || 'Failed to verify OTP.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const confirmPasswordReset = async (email, otp, newPassword, confirmPassword) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/confirm/password', {
//         email,
//         otp,
//         newPassword,
//         confirmPassword,
//       });
//     } catch (err) {
//       setError(err.message || 'Failed to reset password.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   // Request email change — sends OTP to new email address
//   // POST /auth/api/v1/user/update/email  { email: newEmail }
//   const requestEmailChange = async (email) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/update/email', { email });
//     } catch (err) {
//       setError(err.message || 'Failed to send email OTP.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   // Verify email OTP and confirm new email
//   // POST /auth/api/v1/user/email/verify  { newEmail, otp }
//   const verifyEmailChange = async (newEmail, otp) => {
//     setLoading(true);
//     try {
//       await api.post('/auth/api/v1/user/email/verify', { newEmail, otp });
//     } catch (err) {
//       setError(err.message || 'Failed to verify email OTP.');
//       throw err;
//     } finally {
//       setLoading(false);
//     }
//   };

//   const isAdmin = () => {
//     return user?.roles?.includes('ADMIN') || false;
//   };

//   return (
//     <AuthContext.Provider
//       value={{
//         user,
//         loading,
//         error,
//         login,
//         register,
//         verifyOtp,
//         resendOtp,
//         logout,
//         updateProfile,
//         changePassword,
//         requestPasswordReset,
//         verifyPasswordResetOtp,
//         confirmPasswordReset,
//         requestEmailChange,
//         verifyEmailChange,
//         isAdmin,
//       }}
//     >
//       {children}
//     </AuthContext.Provider>
//   );
// };

// export const useAuth = () => useContext(AuthContext);
/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('shopease_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // logout sync from API layer
  useEffect(() => {
    const handleLogout = () => {
      setUser(null);
      api.clearTokens();
    };

    window.addEventListener('auth-logout', handleLogout);
    return () => window.removeEventListener('auth-logout', handleLogout);
  }, []);

  // LOGIN
  const login = async (username, password) => {
    setLoading(true);
    setError(null);

    try {
      const response = await api.post('/auth/api/v1/auth/login', {
        username,
        password,
      });

      // ✅ STORE TOKENS (IMPORTANT)
      api.setTokens(response.accessToken, response.refreshToken);

      // fetch profile
      const searchResponse = await api.get(
        `/auth/api/v1/user/user-information`
      );

      const userProfile = searchResponse?.content?.find(
        (u) => u.username.toLowerCase() === username.toLowerCase()
      );

      if (!userProfile) {
        throw new Error('User profile not found');
      }

      const completeUser = {
        userId: userProfile.userId,
        name: userProfile.name,
        username: userProfile.username,
        gender: userProfile.gender,
        roles:
          response.roles ||
          userProfile.roleWithPermission?.map((r) => r.roleName) ||
          ['USER'],
        permissions:
          userProfile.roleWithPermission?.flatMap((r) =>
            r.permissionResponseList?.map((p) => p.permissionName)
          ) || [],
      };

      setUser(completeUser);
      localStorage.setItem('shopease_user', JSON.stringify(completeUser));

      return completeUser;
    } catch (err) {
      setError(err.message || 'Login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // REGISTER
  const register = async (data) => {
    setLoading(true);
    try {
      return await api.post('/auth/api/v1/auth/register', data);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // OTP VERIFY
  const verifyOtp = (email, otp) =>
    api.post('/auth/api/v1/auth/verify', { email, otp });

  const resendOtp = (email) =>
    api.post('/auth/api/v1/auth/resend-otp', email);

  // LOGOUT
  const logout = async () => {
    setLoading(true);
    try {
      await api.post('/auth/api/v1/user/logout', {});
    } catch (err) {
      console.error(err);
    } finally {
      setUser(null);
      api.clearTokens();
      setLoading(false);
    }
  };

  // UPDATE PROFILE
  const updateProfile = async (name, gender) => {
    if (!user?.userId) return;

    setLoading(true);
    try {
      const response = await api.put(
        `/auth/api/v1/user/update/${user.userId}`,
        {
          name,
          username: user.username,
          gender,
        }
      );

      const updatedUser = {
        ...user,
        name: response.name || name,
        gender: response.gender || gender,
      };

      setUser(updatedUser);
      localStorage.setItem('shopease_user', JSON.stringify(updatedUser));

      return updatedUser;
    } finally {
      setLoading(false);
    }
  };

  // PASSWORD CHANGE
  const changePassword = (oldPassword, newPassword, confirmPassword) =>
    api.post('/auth/api/v1/user/update/password', {
      oldPassword,
      newPassword,
      confirmPassword,
    });

  // PASSWORD RESET FLOW
  const requestPasswordReset = (email) =>
    api.post('/auth/api/v1/user/send-otp', { email });

  const verifyPasswordResetOtp = (email, otp) =>
    api.post('/auth/api/v1/user/otp/verify', { email, otp });

  const confirmPasswordReset = (email, otp, newPassword, confirmPassword) =>
    api.post('/auth/api/v1/user/confirm/password', {
      email,
      otp,
      newPassword,
      confirmPassword,
    });

  // EMAIL CHANGE
  const requestEmailChange = (email) =>
    api.post('/auth/api/v1/user/update/email', { email });

  const verifyEmailChange = (newEmail, otp) =>
    api.post('/auth/api/v1/user/email/verify', { newEmail, otp });

  const isAdmin = () => user?.roles?.includes('ADMIN') || false;

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        login,
        register,
        verifyOtp,
        resendOtp,
        logout,
        updateProfile,
        changePassword,
        requestPasswordReset,
        verifyPasswordResetOtp,
        confirmPasswordReset,
        requestEmailChange,
        verifyEmailChange,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);