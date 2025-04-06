import {useAuth} from "../context/AuthProvider.tsx";
import {Spin} from "antd";
import {Navigate} from "react-router-dom";

interface PrivateRouteProps {
    children: React.ReactNode;
    requiredRoles?: string[];
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children, requiredRoles }) => {
    const { isAuthenticated, loading, roles } = useAuth();

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '10vh' }}>
                <Spin size="large" />
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRoles && !requiredRoles.some(role => roles.includes(role))) {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};

export default PrivateRoute;