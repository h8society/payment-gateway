import React, { useEffect, useState } from 'react';
import { Button, Drawer, Form, Input, Popconfirm, Space, Table, message } from 'antd';

interface Shop {
    shopId: number;
    name: string;
    description: string;
    merchantUsername?: string;
}

const MerchantShopsPage: React.FC = () => {
    const [shops, setShops] = useState<Shop[]>([]);
    const [loading, setLoading] = useState(false);
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [editingShop, setEditingShop] = useState<Shop | null>(null);
    const [form] = Form.useForm();

    // Получение списка магазинов для мерчанта
    const fetchShops = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/merchant/shops', {
                credentials: 'include'
            });
            if (!res.ok) {
                if (res.status === 401) {
                    message.error('Пользователь не авторизован');
                } else {
                    message.error('Ошибка при получении данных');
                }
                return;
            }
            const data = await res.json();
            setShops(data);
        } catch (error) {
            message.error('Ошибка сети');
        } finally {
            setLoading(false);
        }
    };

    // Сохранение магазина (создание или редактирование)
    const handleSave = async (values: any) => {
        try {
            const url = editingShop
                ? `http://localhost:8080/api/merchant/shops/${editingShop.shopId}`
                : 'http://localhost:8080/api/merchant/shops';
            const method = editingShop ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(values),
            });

            if (!res.ok) {
                if (res.status === 401) {
                    message.error('Пользователь не авторизован');
                } else {
                    message.error('Ошибка при сохранении');
                }
                return;
            }

            message.success('Магазин сохранён');
            setDrawerVisible(false);
            form.resetFields();
            fetchShops();
        } catch (error) {
            message.error('Ошибка сети');
        }
    };

    // Удаление магазина
    const handleDelete = async (shopId: number) => {
        try {
            const res = await fetch(`http://localhost:8080/api/merchant/shops/${shopId}`, {
                method: 'DELETE',
                credentials: 'include',
            });
            if (!res.ok) {
                if (res.status === 401) {
                    message.error('Пользователь не авторизован');
                } else {
                    message.error('Ошибка при удалении');
                }
                return;
            }
            message.success('Магазин удалён');
            fetchShops();
        } catch (error) {
            message.error('Ошибка сети');
        }
    };

    useEffect(() => {
        fetchShops();
    }, []);

    return (
        <>
            <Space style={{ marginBottom: 16 }}>
                <Button
                    type="primary"
                    onClick={() => {
                        setEditingShop(null);
                        form.resetFields();
                        setDrawerVisible(true);
                    }}
                >
                    Добавить магазин
                </Button>
            </Space>
            <Table
                dataSource={shops}
                columns={[
                    { title: 'ID', dataIndex: 'shopId' },
                    { title: 'Название', dataIndex: 'name' },
                    { title: 'Описание', dataIndex: 'description' },
                    {
                        title: 'Действия',
                        render: (_, record: Shop) => (
                            <Space>
                                <Button
                                    onClick={() => {
                                        setEditingShop(record);
                                        form.setFieldsValue(record);
                                        setDrawerVisible(true);
                                    }}
                                >
                                    Редактировать
                                </Button>
                                <Popconfirm
                                    title="Удалить магазин?"
                                    onConfirm={() => handleDelete(record.shopId)}
                                >
                                    <Button danger>Удалить</Button>
                                </Popconfirm>
                            </Space>
                        ),
                    },
                ]}
                rowKey="shopId"
                loading={loading}
            />

            <Drawer
                title={editingShop ? 'Редактировать магазин' : 'Новый магазин'}
                open={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                width={400}
            >
                <Form layout="vertical" form={form} onFinish={handleSave}>
                    <Form.Item
                        label="Название"
                        name="name"
                        rules={[
                            { required: true, message: 'Название магазина обязательно' },
                            { max: 255, message: 'Название магазина не должно превышать 255 символов' }
                        ]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="Описание"
                        name="description"
                        rules={[
                            { max: 1000, message: 'Описание не должно превышать 1000 символов' }
                        ]}
                    >
                        <Input.TextArea rows={3} />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block>
                        Сохранить
                    </Button>
                </Form>
            </Drawer>
        </>
    );
};

export default MerchantShopsPage;
