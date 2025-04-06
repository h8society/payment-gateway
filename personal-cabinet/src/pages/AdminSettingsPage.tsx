import React, { useEffect, useState } from 'react';
import { Button, Drawer, Form, Input, message, Space, Table } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

interface GatewaySetting {
    key: string;
    value: string;
}

const AdminSettingsPage: React.FC = () => {
    const [settings, setSettings] = useState<GatewaySetting[]>([]);
    const [loading, setLoading] = useState(false);
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [form] = Form.useForm();

    const fetchSettings = async () => {
        setLoading(true);
        const res = await fetch('http://localhost:8080/api/admin/settings', { credentials: 'include' });
        const data = await res.json();
        setSettings(data);
        setLoading(false);
    };

    const handleAddSetting = async (values: GatewaySetting) => {
        try {
            const res = await fetch('http://localhost:8080/api/admin/settings', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(values),
            });

            if (res.ok) {
                message.success('Настройка сохранена');
                form.resetFields();
                setDrawerVisible(false);
                fetchSettings();
            } else {
                message.error('Ошибка при сохранении настройки');
            }
        } catch {
            message.error('Ошибка сети');
        }
    };

    useEffect(() => {
        fetchSettings();
    }, []);

    const columns = [
        {
            title: 'Ключ',
            dataIndex: 'key',
        },
        {
            title: 'Значение',
            dataIndex: 'value',
        },
    ];

    return (
        <>
            <Space style={{ marginBottom: 16 }}>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => setDrawerVisible(true)}>
                    Добавить настройку
                </Button>
            </Space>
            <Table
                dataSource={settings}
                columns={columns}
                rowKey="key"
                loading={loading}
                scroll={{ x: 'max-content' }}
            />

            <Drawer
                title="Новая настройка шлюза"
                open={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                width={400}
            >
                <Form layout="vertical" form={form} onFinish={handleAddSetting}>
                    <Form.Item label="Ключ" name="key" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item label="Значение" name="value" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block>
                        Сохранить
                    </Button>
                </Form>
            </Drawer>
        </>
    );
};

export default AdminSettingsPage;