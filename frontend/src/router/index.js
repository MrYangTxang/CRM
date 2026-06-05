import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const LoginPage = () => import('../views/LoginPage.vue')
const RegisterPage = () => import('../views/RegisterPage.vue')
const DashboardPage = () => import('../views/DashboardPage.vue')
const CustomerPage = () => import('../views/CustomerPage.vue')
const BusinessPage = () => import('../views/BusinessPage.vue')
const WorkOrderPage = () => import('../views/WorkOrderPage.vue')
const StaffPage = () => import('../views/StaffPage.vue')

const routes = [
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: DashboardPage },
      { path: 'customer', component: CustomerPage },
      { path: 'business', component: BusinessPage },
      { path: 'workorder', component: WorkOrderPage },
      { path: 'staff', component: StaffPage },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    return '/login'
  }
})

export default router
