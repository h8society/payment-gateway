import React, { useEffect, useState } from 'react';
import { Button, Card, Input, Popconfirm, Space, Table, Typography, message } from 'antd';

const { Title } = Typography;

const MerchantApiKeysPage: React.FC = () => {
    const [keys, setKeys] = useState<string[]>([]);
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);

    const fetchKeys = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/merchant', { credentials: 'include' });
            const data = await res.json();
            setKeys(data.apiKeys || []);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateKey = async () => {
        if (!description) return;
        try {
            const res = await fetch('http://localhost:8080/api/merchant/keys', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ description }),
            });

            if (res.ok) {
                message.success('Ключ создан');
                setDescription('');
                fetchKeys();
            } else {
                message.error('Ошибка при создании ключа');
            }
        } catch {
            message.error('Ошибка сети');
        }
    };

    const handleDelete = async (key: string) => {
        try {
            const res = await fetch(`http://localhost:8080/api/merchant/keys/${key}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (res.ok) {
                message.success('Ключ удалён');
                fetchKeys();
            } else {
                message.error('Ошибка при удалении ключа');
            }
        } catch {
            message.error('Ошибка сети');
        }
    };

    useEffect(() => {
        fetchKeys();
    }, []);

    const columns = [
        {
            title: 'API ключ',
            dataIndex: 'key',
        },
        {
            title: 'Действия',
            render: (_: any, record: { key: string }) => (
                <Popconfirm
                    title="Удалить этот ключ?"
                    onConfirm={() => handleDelete(record.key)}
                >
                    <Button danger>Удалить</Button>
                </Popconfirm>
            ),
        },
    ];

    return (
        <Card>
            <Title level={4}>API ключи</Title>
            <Space direction="vertical" style={{ width: '100%' }} size="large">
                <Space>
                    <Input
                        placeholder="Описание нового ключа"
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                    />
                    <Button type="primary" onClick={handleCreateKey}>Создать ключ</Button>
                </Space>

                <Table
                    dataSource={keys.map(k => ({ key: k }))}
                    columns={columns}
                    rowKey="key"
                    loading={loading}
                />
            </Space>
        </Card>
    );
};

export default MerchantApiKeysPage;
