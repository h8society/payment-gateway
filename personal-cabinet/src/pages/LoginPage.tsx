import React, {useEffect, useState} from 'react';
import {Controller, useForm} from 'react-hook-form';
import { Button, Card, Input, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import {useAuth} from "../context/AuthProvider.tsx";

interface LoginFormData {
    username: string;
    password: string;
}

const LoginPage: React.FC = () => {
    const { control, handleSubmit } = useForm<LoginFormData>();
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const {checkAuth, isAuthenticated, roles} = useAuth()

    useEffect(() => {
        if (isAuthenticated) {
            if (roles.includes('ADMIN')) {
                navigate('/admin/transactions', { replace: true });
            } else {
                navigate('/cabinet', { replace: true });
            }
        }
    }, [isAuthenticated, roles, navigate]);

    const onSubmit = async (data: LoginFormData) => {
        setLoading(true);
        try {
            const formData = new URLSearchParams();
            formData.append('username', data.username);
            formData.append('password', data.password);

            const res = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                credentials: 'include',
                body: formData.toString(),
            });

            if (res.ok) {
                message.success('Успешный вход');
                await checkAuth()
                navigate('/');
            } else {
                message.error('Неверное имя пользователя или пароль');
            }
        } catch {
            message.error('Ошибка входа');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '10vh' }}>
            <Card title="Вход в кабинет" style={{width: 400}}>
                <form onSubmit={handleSubmit(onSubmit)} style={{display: 'flex', flexDirection: 'column', gap: 16}}>
                    <Controller
                        name="username"
                        control={control}
                        rules={{required: 'Введите имя пользователя'}}
                        render={({field}) => <Input {...field} placeholder="Имя пользователя"/>}
                    />

                    <Controller
                        name="password"
                        control={control}
                        rules={{required: 'Введите пароль'}}
                        render={({field}) => <Input.Password {...field} placeholder="Пароль"/>}
                    />

                    <Button htmlType="submit" type="primary" block loading={loading}>
                        Войти
                    </Button>
                </form>

            </Card>
        </div>
    );
};

export default LoginPage;
