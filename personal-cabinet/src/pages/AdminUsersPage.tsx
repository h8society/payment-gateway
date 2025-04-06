import React, { useEffect, useState } from 'react';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, message, Switch } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

interface AdminUser {
    id: number;
    username: string;
    email: string;
    active: boolean;
    roles: string[];
}

const AdminUsersPage: React.FC = () => {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [loading, setLoading] = useState(false);
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [form] = Form.useForm();

    const fetchUsers = async () => {
        setLoading(true);
        const res = await fetch('http://localhost:8080/api/admin/users', { credentials: 'include' });
        const data = await res.json();
        setUsers(data);
        setLoading(false);
    };

    const handleCreateUser = async (values: any) => {
        try {
            const res = await fetch('http://localhost:8080/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    username: values.username,
                    email: values.email,
                    password: values.password,
                    roleName: values.role,
                }),
            });

            if (res.ok) {
                message.success('Пользователь добавлен');
                form.resetFields();
                setDrawerVisible(false);
                fetchUsers();
            } else {
                message.error('Ошибка при добавлении пользователя');
            }
        } catch (e) {
            message.error('Ошибка сети');
        }
    };

    const handleToggleActive = async (user: AdminUser) => {
        const updated = !user.active;
        await fetch(`http://localhost:8080/api/admin/users/${user.id}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ active: updated }),
        });
        fetchUsers();
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 60,
        },
        {
            title: 'Имя пользователя',
            dataIndex: 'username',
        },
        {
            title: 'Email',
            dataIndex: 'email',
        },
        {
            title: 'Роли',
            dataIndex: 'roles',
            render: (roles: string[]) => roles.map(r => <Tag key={r}>{r}</Tag>),
        },
        {
            title: 'Активен',
            dataIndex: 'active',
            render: (_: any, record: AdminUser) => (
                <Switch checked={record.active} onChange={() => handleToggleActive(record)} />
            ),
        },
    ];

    return (
        <>
            <Space style={{ marginBottom: 16 }}>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => setDrawerVisible(true)}>
                    Добавить пользователя
                </Button>
            </Space>
            <Table
                dataSource={users}
                columns={columns}
                rowKey="id"
                loading={loading}
                scroll={{ x: 'max-content' }}
            />

            <Drawer
                title="Добавление пользователя"
                open={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                width={400}
            >
                <Form layout="vertical" form={form} onFinish={handleCreateUser}>
                    <Form.Item label="Имя пользователя" name="username" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item label="Email" name="email" rules={[{ required: true }]}>
                        <Input type="email" />
                    </Form.Item>
                    <Form.Item label="Пароль" name="password" rules={[{ required: true }]}>
                        <Input.Password />
                    </Form.Item>
                    <Form.Item label="Роль" name="role" rules={[{ required: true }]}>
                        <Select options={['MERCHANT', 'ADMIN'].map(r => ({ value: r, label: r }))} />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block>
                        Зарегистрировать
                    </Button>
                </Form>
            </Drawer>
        </>
    );
};

export default AdminUsersPage;
