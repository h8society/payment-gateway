import React, { useEffect, useState } from 'react';
import { Table, Typography, message } from 'antd';

interface AdminShop {
    shopId: number;
    name: string;
    description: string;
    merchantUsername: string;
}

const AdminShopsPage: React.FC = () => {
    const [shops, setShops] = useState<AdminShop[]>([]);
    const [loading, setLoading] = useState(false);

    const fetchShops = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/admin/shops', {
                credentials: 'include',
            });
            const data = await res.json();
            setShops(data);
        } catch {
            message.error('Не удалось загрузить магазины');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchShops();
    }, []);

    return (
        <div style={{ padding: '2rem' }}>
            <Typography.Title level={3}>Список всех магазинов</Typography.Title>
            <Table
                dataSource={shops}
                columns={[
                    { title: 'ID', dataIndex: 'shopId' },
                    { title: 'Название', dataIndex: 'name' },
                    { title: 'Описание', dataIndex: 'description' },
                    { title: 'Мерчант', dataIndex: 'merchantUsername' },
                ]}
                rowKey="shopId"
                loading={loading}
                pagination={{ pageSize: 10 }}
            />
        </div>
    );
};

export default AdminShopsPage;
