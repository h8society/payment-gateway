import { useEffect, useState } from 'react';
import {
    Layout, Typography, Button, Row, Col, Card, Table, Select, message, Tag, Input
} from 'antd';
import {
    LineChart, Line, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import styles from './CabinetPage.module.scss';

const { Header, Content } = Layout;

interface Transaction {
    transactionId: string;
    amount: number;
    statusCode: string;
    responseCode: string;
    transactionDate: string;
}

interface Merchant {
    username: string;
    email: string;
}

interface Stats {
    transactionCount: number;
    paidCount: number;
    nonPaidCount: number;
    totalAmount: number;
    paidAmount: number;
}

interface Shop {
    shopId: string;
    name: string;
}

interface Status {
    id: number;
    name: string;
    code: string;
}

export default function CabinetPage() {
    const [merchant, setMerchant] = useState<Merchant | null>(null);
    const [transactions, setTransactions] = useState<Transaction[] | null>([]);
    const [stats, setStats] = useState<Stats | null>(null);
    const [loading, setLoading] = useState(true);
    const [shops, setShops] = useState<Shop[] | null>([]);
    const [selectedShopId, setSelectedShopId] = useState(null);
    const [chartData, setChartData] = useState([]);
    const [statuses, setStatuses] = useState<Status[]>([]);
    const [selectedStatusId, setSelectedStatusId] = useState<number | null>(null);
    const [orderFilter, setOrderFilter] = useState<string>('');
    const [tableShopFilter, setTableShopFilter] = useState<string | null>(null);
    const [tableStatusFilter, setTableStatusFilter] = useState<number | null>(null);

    useEffect(() => {
        fetch('http://localhost:8080/api/merchant', { credentials: 'include' })
            .then(res => res.json())
            .then(setMerchant);

        fetch('http://localhost:8080/api/merchant/transactions', { credentials: 'include' })
            .then(res => res.json())
            .then(data => {
                setTransactions(data);
                setLoading(false);
            });

        fetch('http://localhost:8080/api/merchant/stats', { credentials: 'include' })
            .then(res => res.json())
            .then(setStats);

        fetch('http://localhost:8080/api/merchant/shops', { credentials: 'include' })
            .then(res => res.json())
            .then(setShops);
    }, []);

    useEffect(() => {
        fetch('http://localhost:8080/api/payments/statuses', { credentials: 'include' })
            .then(res => res.json())
            .then(setStatuses);
    }, []);

    useEffect(() => {
        const url = selectedShopId
            ? `http://localhost:8080/api/merchant/graphics?shopId=${selectedShopId}`
            : 'http://localhost:8080/api/merchant/graphics';

        fetch(url, { credentials: 'include' })
            .then(res => res.json())
            .then(setChartData);
    }, [selectedShopId]);

    useEffect(() => {
        const url = selectedStatusId
            ? `http://localhost:8080/api/merchant/graphics?statusId=${selectedStatusId}`
            : 'http://localhost:8080/api/merchant/graphics';

        fetch(url, { credentials: 'include' })
            .then(res => res.json())
            .then(setChartData);
    }, [selectedStatusId]);

    useEffect(() => {
        let url = 'http://localhost:8080/api/merchant/transactions';
        const params = new URLSearchParams();
        if (orderFilter.trim() !== '') {
            params.append('order', orderFilter);
        }
        if (tableShopFilter) {
            params.append('shopId', tableShopFilter);
        }
        if (tableStatusFilter) {
            params.append('statusId', tableStatusFilter.toString());
        }
        if ([...params].length > 0) {
            url += '?' + params.toString();
        }
        setLoading(true);
        fetch(url, { credentials: 'include' })
            .then(res => res.json())
            .then(data => {
                setTransactions(data);
                setLoading(false);
            });
    }, [orderFilter, tableShopFilter, tableStatusFilter]);

    const logout = () => {
        fetch('http://localhost:8080/auth/logout', {
            method: 'POST',
            credentials: 'include',
        }).then(() => {
            window.location.href = '/login';
        });
    };

    const handleRefund = async (id: string) => {
        try {
            const res = await fetch(`http://localhost:8080/api/payments/${id}/refund`, {
                method: 'POST',
                credentials: 'include',
            });

            if (!res.ok) throw new Error();
            message.success('Возврат выполнен');

            const updated = await fetch('http://localhost:8080/api/merchant/transactions', { credentials: 'include' });
            setTransactions(await updated.json());
        } catch {
            message.error('Ошибка при возврате');
        }
    };

    const columns = [
        {
            title: 'Номер заказа',
            dataIndex: 'orderNumber',
            key: 'orderNumber',
        },
        {
            title: 'Магазин',
            dataIndex: 'shopName',
            key: 'shopName',
        },
        {
            title: 'Сумма',
            dataIndex: 'amount',
            key: 'amount',
            render: (val: number) => `${val.toFixed(2)} ₽`,
        },
        {
            title: 'Статус',
            dataIndex: 'statusCode',
            key: 'statusCode',
            render: (status: string) => {
                const getColor = (st: string) => {
                    switch (st) {
                        case "paid":
                            return "green";
                        case "created":
                            return "geekblue";
                        case "declined":
                            return "red";
                        case "canceled":
                            return "orange";
                        case "refund":
                            return "magenta";
                        default:
                            return '';
                    }
                }
                // @ts-ignore
                return statuses ? <Tag color={getColor(status)}>{statuses.find(s => s.code === status).name.toUpperCase()}</Tag> : null;
            }
        },
        { title: 'Код ответа', dataIndex: 'responseCode', key: 'responseCode' },
        {
            title: 'Дата',
            dataIndex: 'transactionDate',
            key: 'transactionDate',
            render: (date: string) => new Date(date).toLocaleString(),
        },
        {
            title: 'Действия',
            key: 'actions',
            render: (_: any, record: Transaction) =>
                record.statusCode === 'paid' ? (
                    <Button type="link" danger onClick={() => handleRefund(record.transactionId)}>
                        Возврат
                    </Button>
                ) : null,
        },
    ];

    return (
        <Layout>
            <Header className={styles.header}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                    Личный кабинет
                </Typography.Title>
                <Button danger onClick={logout}>
                    Выйти
                </Button>
            </Header>
            <Content style={{ padding: '2rem' }}>
                {merchant && (
                    <Typography.Title level={3}>
                        Добро пожаловать, {merchant.username}
                    </Typography.Title>
                )}

                {stats && (
                    <Row gutter={16} style={{ marginBottom: '2rem' }}>
                        <Col span={6}>
                            <Card title="Всего транзакций">
                                <Typography.Text strong>{stats.transactionCount}</Typography.Text>
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card title="Оплачено">
                                <Typography.Text strong>{stats.paidCount}</Typography.Text>
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card title="Не оплачено">
                                <Typography.Text strong>{stats.nonPaidCount}</Typography.Text>
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card title="Сумма оплаченного">
                                <Typography.Text strong>{stats.paidAmount.toFixed(2)} ₽</Typography.Text>
                            </Card>
                        </Col>
                    </Row>
                )}

                {stats && (
                    <>
                        <Typography.Title level={4}>График транзакций по дням</Typography.Title>
                        <Select
                            style={{ width: 300, marginBottom: 16 }}
                            value={selectedShopId}
                            onChange={value => setSelectedShopId(value)}
                            allowClear
                            placeholder="Все магазины"
                        >
                            {shops && shops.map(shop => (
                                <Select.Option key={shop.shopId} value={shop.shopId}>
                                    {shop.name}
                                </Select.Option>
                            ))}
                        </Select>
                        <Select
                            style={{ width: 300, marginBottom: 16, marginLeft: 16 }}
                            value={selectedStatusId || undefined}
                            onChange={value => setSelectedStatusId(value)}
                            allowClear
                            placeholder="Все статусы"
                        >
                            {statuses && statuses.map((status: Status) => (
                                <Select.Option key={status.id} value={status.id}>
                                    {status.name} ({status.code})
                                </Select.Option>
                            ))}
                        </Select>

                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={chartData}>
                                <XAxis dataKey="date" />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Line type="monotone" dataKey="count" stroke="#8884d8" name="Кол-во" />
                                <Line type="monotone" dataKey="total" stroke="#82ca9d" name="Сумма" />
                            </LineChart>
                        </ResponsiveContainer>
                    </>
                )}

                <Row gutter={16} style={{ marginBottom: '1rem' }}>
                    <Col>
                        <Input
                            placeholder="Номер заказа"
                            value={orderFilter}
                            onChange={e => setOrderFilter(e.target.value)}
                            style={{ width: 200 }}
                        />
                    </Col>
                    <Col>
                        <Select
                            placeholder="Все магазины"
                            value={tableShopFilter || undefined}
                            onChange={value => setTableShopFilter(value)}
                            allowClear
                            style={{ width: 200 }}
                        >
                            {shops && shops.map(shop => (
                                <Select.Option key={shop.shopId} value={shop.shopId}>
                                    {shop.name}
                                </Select.Option>
                            ))}
                        </Select>
                    </Col>
                    <Col>
                        <Select
                            placeholder="Все статусы"
                            value={tableStatusFilter || undefined}
                            onChange={value => setTableStatusFilter(value)}
                            allowClear
                            style={{ width: 200 }}
                        >
                            {statuses && statuses.map((status: Status) => (
                                <Select.Option key={status.id} value={status.id}>
                                    {status.name} ({status.code})
                                </Select.Option>
                            ))}
                        </Select>
                    </Col>
                </Row>

                <Typography.Title level={4}>Транзакции</Typography.Title>
                <Table
                    columns={columns}
                    dataSource={transactions || []}
                    rowKey="transactionId"
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    scroll={{ x: 'max-content' }}
                />
            </Content>
        </Layout>
    );
}
