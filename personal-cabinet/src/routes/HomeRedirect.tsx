import React from 'react';
import { useAuth } from '../context/AuthProvider';
import {Navigate} from 'react-router-dom';
import { Spin } from 'antd';

const HomeRedirect: React.FC = () => {
    const { roles, loading } = useAuth();

    if(roles.includes('ADMIN')) {
        return <Navigate to={'/admin/transactions'} replace />
    }

    if(roles.includes('MERCHANT')) {
        return <Navigate to={'/cabinet'} replace />
    }

    if(loading) {
        return <div style={{display: 'flex', justifyContent: 'center', marginTop: '20vh'}}>
            <Spin size="large"/>
        </div>
    }

    return <Navigate to={'/login'} replace />
};

export default HomeRedirect;
