import React, { useEffect, useState } from 'react';
import { Table, Typography, message, Card, Row, Col } from 'antd';
import { Layout, Button } from 'antd';
import { useAuth } from '../context/AuthProvider';
import styles from './CabinetPage.module.scss'

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

const CabinetPage: React.FC = () => {
    const [merchant, setMerchant] = useState<Merchant | null>(null);
    const [stats, setStats] = useState<Stats | null>(null);
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [loading, setLoading] = useState(false);
    const {logout} = useAuth()

    useEffect(() => {
        const fetchAll = async () => {
            try {
                const [mRes, sRes, tRes] = await Promise.all([
                    fetch('http://localhost:8080/api/merchant', { credentials: 'include' }),
                    fetch('http://localhost:8080/api/merchant/stats', { credentials: 'include' }),
                    fetch('http://localhost:8080/api/merchant/transactions', { credentials: 'include' }),
                ]);

                if (!mRes.ok || !sRes.ok || !tRes.ok) throw new Error();

                const m = await mRes.json();
                const s = await sRes.json();
                const t = await tRes.json();

                setMerchant(m);
                setStats(s);
                setTransactions(t);
            } catch {
                message.error('Ошибка загрузки данных');
            } finally {
                setLoading(false);
            }
        };

        fetchAll();
    }, []);

    const columns = [
        { title: 'ID', dataIndex: 'transactionId', key: 'transactionId', ellipsis: true },
        {
            title: 'Сумма',
            dataIndex: 'amount',
            key: 'amount',
            render: (val: number) => `${val.toFixed(2)} ₽`,
        },
        { title: 'Статус', dataIndex: 'statusCode', key: 'statusCode' },
        { title: 'Код ответа', dataIndex: 'responseCode', key: 'responseCode' },
        {
            title: 'Дата',
            dataIndex: 'transactionDate',
            key: 'transactionDate',
            render: (date: string) => new Date(date).toLocaleString(),
        },
    ];

    const { Header, Content } = Layout;

    console.log('CabinetPage mounted');

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

                <Typography.Title level={4}>Транзакции</Typography.Title>
                <Table
                    columns={columns}
                    dataSource={transactions}
                    rowKey="transactionId"
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    scroll={{ x: 'max-content' }}
                />
            </Content>
        </Layout>
    );
};

export default CabinetPage;
