import React, { useState } from 'react';
import { Layout, Menu, Button, Drawer, Grid } from 'antd';
import {
    TransactionOutlined,
    KeyOutlined,
    LogoutOutlined,
    MenuOutlined,
    ShopOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthProvider';

const { Header, Sider, Content } = Layout;
const { useBreakpoint } = Grid;

const MerchantLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const { logout } = useAuth();
    const screens = useBreakpoint();

    const [drawerVisible, setDrawerVisible] = useState(false);

    const menuItems = [
        {
            key: '/cabinet',
            icon: <TransactionOutlined />,
            label: 'Транзакции',
        },
        {
            key: '/cabinet/keys',
            icon: <KeyOutlined />,
            label: 'API ключи',
        },
        {
            key: '/cabinet/shops',
            icon: <ShopOutlined />,
            label: 'Магазины',
        },
        {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: 'Выйти',
        },
    ];

    const onMenuClick = ({ key }: { key: string }) => {
        setDrawerVisible(false);

        if (key === 'logout') {
            logout();
        } else {
            navigate(key);
        }
    };

    const menuComponent = (
        <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={onMenuClick}
        />
    );

    return (
        <Layout style={{ minHeight: '100vh' }}>
            {!screens.xs && (
                <Sider breakpoint="lg" collapsedWidth="0">
                    <div
                        style={{
                            color: '#fff',
                            fontSize: '1.2rem',
                            fontWeight: 'bold',
                            padding: '1rem',
                            textAlign: 'center',
                        }}
                    >
                        Merchant Cabinet
                    </div>
                    {menuComponent}
                </Sider>
            )}

            <Layout>
                { screens.xs &&
                    <Header
                        style={{
                            background: '#fff',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            padding: '0 1rem',
                        }}
                    >
                        <Button
                            type="text"
                            icon={<MenuOutlined />}
                            onClick={() => setDrawerVisible(true)}
                        />
                    </Header>
                }

                <Content style={{ margin: '1rem', padding: '1rem', background: '#fff' }}>
                    {children}
                </Content>
            </Layout>

            <Drawer
                title="Меню"
                placement="left"
                onClose={() => setDrawerVisible(false)}
                open={drawerVisible}
                bodyStyle={{ padding: 0 }}
            >
                {menuComponent}
            </Drawer>
        </Layout>
    );
};

export default MerchantLayout;
