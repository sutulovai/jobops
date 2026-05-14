import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  userId: string | null
  email: string | null
  isAuthenticated: boolean
  login: (accessToken: string, refreshToken: string, userId: string, email: string) => void
  logout: () => void
  setAccessToken: (token: string) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      userId: null,
      email: null,
      isAuthenticated: false,

      login: (accessToken, refreshToken, userId, email) =>
        set({ accessToken, refreshToken, userId, email, isAuthenticated: true }),

      logout: () =>
        set({
          accessToken: null,
          refreshToken: null,
          userId: null,
          email: null,
          isAuthenticated: false,
        }),

      setAccessToken: (token) => set({ accessToken: token }),
    }),
    {
      name: 'jobops-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        userId: state.userId,
        email: state.email,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
)
