import {BrowserRouter, Route, Routes} from "react-router-dom";
import LoginPage from './pages/LoginPage';
import CabinetPage from './pages/CabinetPage';
import {AuthProvider} from "./context/AuthProvider.tsx";
import PrivateRoute from "./routes/PrivateRoute.tsx";
import AdminUsersPage from "./pages/AdminUsersPage.tsx";
import AdminTransactionsPage from "./pages/AdminTransactionsPage.tsx";
import AdminSettingsPage from "./pages/AdminSettingsPage.tsx";
import AdminLayout from "./layout/AdminLayout.tsx";
import HomeRedirect from "./routes/HomeRedirect.tsx";
import MerchantApiKeysPage from "./pages/MerchantApiKeysPage.tsx";
import MerchantLayout from "./layout/MerchantLayout.tsx";

function App() {

  return (
      <AuthProvider>
        <BrowserRouter>
              <Routes>
                  <Route path="/" element={<HomeRedirect />} />
                  <Route path="/login" element={<LoginPage />} />
                  <Route
                      path="/cabinet"
                      element={
                          <PrivateRoute  requiredRoles={['MERCHANT']}>
                              <MerchantLayout>
                                  <CabinetPage />
                              </MerchantLayout>
                          </PrivateRoute>
                      }
                  />
                  <Route
                      path="/cabinet/keys"
                      element={
                          <PrivateRoute requiredRoles={['MERCHANT']}>
                              <MerchantLayout>
                                  <MerchantApiKeysPage />
                              </MerchantLayout>
                          </PrivateRoute>
                      }
                  />
                  <Route
                      path="/admin/users"
                      element={
                          <PrivateRoute requiredRoles={['ADMIN']}>
                              <AdminLayout>
                                  <AdminUsersPage />
                              </AdminLayout>
                          </PrivateRoute>
                      }
                  />
                  <Route
                      path="/admin/transactions"
                      element={
                          <PrivateRoute requiredRoles={['ADMIN']}>
                              <AdminLayout>
                                  <AdminTransactionsPage />
                              </AdminLayout>
                          </PrivateRoute>
                      }
                  />
                  <Route
                      path="/admin/settings"
                      element={
                          <PrivateRoute requiredRoles={['ADMIN']}>
                              <AdminLayout>
                                  <AdminSettingsPage />
                              </AdminLayout>
                          </PrivateRoute>
                      }
                  />
              </Routes>
        </BrowserRouter>
      </AuthProvider>
  )
}

export default App
