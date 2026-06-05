import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    token: localStorage.getItem('token') || ''
  }),
  actions: {
    setUser(username, role, token) {
      this.username = username
      this.role = role
      this.token = token
      localStorage.setItem('username', username)
      localStorage.setItem('role', role)
      localStorage.setItem('token', token)
    },
    logout() {
      this.username = ''
      this.role = ''
      this.token = ''
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('token')
    }
  }
})