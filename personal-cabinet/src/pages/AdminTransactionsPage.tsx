import React, { useEffect, useState } from 'react';
import { Table, Typography, message, Tag } from 'antd';

interface Transaction {
    transactionId: string;
    amount: number;
    statusCode: string;
    responseCode: string;
    transactionDate: string;
    binBrand: string | null;
    binBankName: string | null;
    binCountry: string | null;
}

const AdminTransactionsPage: React.FC = () => {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [loading, setLoading] = useState(false);

    const fetchTransactions = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/admin/transactions', {
                credentials: 'include',
            });

            if (!res.ok) throw new Error();
            const data = await res.json();
            setTransactions(data);
        } catch {
            message.error('Не удалось загрузить транзакции');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTransactions();
    }, []);

    const columns = [
        {
            title: 'ID',
            dataIndex: 'transactionId',
            key: 'transactionId',
            ellipsis: true
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
            render: (val: string) => {
                const color = val === 'paid' ? 'green' : val === 'declined' ? 'red' : 'orange';
                return <Tag color={color}>{val.toUpperCase()}</Tag>;
            },
        },
        {
            title: 'Код ответа',
            dataIndex: 'responseCode',
            key: 'responseCode',
        },
        {
            title: 'Дата',
            dataIndex: 'transactionDate',
            key: 'transactionDate',
            render: (val: string) => new Date(val).toLocaleString(),
        },
        {
            title: 'Банк',
            dataIndex: 'binBankName',
            key: 'binBankName',
            render: (val: string | null) => val || '-',
        },
        {
            title: 'Бренд',
            dataIndex: 'binBrand',
            key: 'binBrand',
            render: (val: string | null) => val || '-',
        },
        {
            title: 'Страна',
            dataIndex: 'binCountry',
            key: 'binCountry',
            render: (val: string | null) => val || '-',
        },
    ];

    return (
        <div style={{ padding: '2rem' }}>
            <Typography.Title level={3}>Все транзакции</Typography.Title>
            <Table
                dataSource={transactions}
                columns={columns}
                rowKey="transactionId"
                loading={loading}
                pagination={{ pageSize: 10 }}
                scroll={{ x: 'max-content' }}
            />
        </div>
    );
};

export default AdminTransactionsPage;
