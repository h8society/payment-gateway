import React, { createContext, useContext, useEffect, useState } from 'react';

interface AuthContextProps {
    isAuthenticated: boolean;
    loading: boolean;
    roles: string[];
    checkAuth: () => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextProps>({
    isAuthenticated: false,
    loading: true,
    roles: [],
    checkAuth: () => {},
    logout: () => {},
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const [roles, setRoles] = useState<string[]>([]);

    const checkAuth = async () => {
        try {
            const res = await fetch('http://localhost:8080/api/merchant', {
                credentials: 'include',
            });
            const user = await res.json();
            setIsAuthenticated(res.ok);
            setRoles(user.roles || []);
        } catch {
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };

    const logout = async () => {
        try {
            await fetch('http://localhost:8080/auth/logout', {
                method: 'POST',
                credentials: 'include',
            });
        } catch (e) {
            console.error('Logout failed', e);
        } finally {
            setIsAuthenticated(false);
        }
    };

    useEffect(() => {
        checkAuth();
    }, []);

    return (
        <AuthContext.Provider value={{ isAuthenticated, loading, roles, checkAuth, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
