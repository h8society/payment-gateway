import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import styles from './PayPage.module.scss';
import classNames from 'classnames';

const USE_MOCK = false;

interface TransactionDetails {
    transactionId: string;
    amount: number;
    statusCode: string;
    responseCode: string;
    transactionDate: string;
}

interface PaymentForm {
    cardNumber: string;
    cardExpiry: string;
    cardCvc: string;
}

const PayPage: React.FC = () => {
    const { transactionId } = useParams<{ transactionId: string }>();
    const [details, setDetails] = useState<TransactionDetails | null>(null);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch,
        trigger,
        setValue,
    } = useForm<PaymentForm>({
        mode: 'onChange',
    });

    const rawCardNumber = watch('cardNumber') || '';
    const cardNumber = rawCardNumber.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
    const cardExpiry = watch('cardExpiry') || '';
    const cardCvc = watch('cardCvc') || '';

    useEffect(() => {
        const fetchDetails = async () => {
            if (USE_MOCK) {
                setDetails({
                    transactionId: transactionId || 'mock-id',
                    amount: 120.5,
                    statusCode: 'created',
                    responseCode: 'PENDING',
                    transactionDate: new Date().toISOString(),
                });
                return;
            }

            try {
                const res = await fetch(`http://localhost:8080/api/payments/${transactionId}`, {
                    credentials: 'include',
                });
                const data = await res.json();
                setDetails(data);
            } catch (err) {
                console.error(err);
                setError('Failed to load transaction details');
            }
        };

        fetchDetails();
    }, [transactionId]);

    const onSubmit = async (formData: PaymentForm) => {
        setLoading(true);
        setError(null);
        setResult(null);

        if (USE_MOCK) {
            setTimeout(() => {
                setResult('❌ Payment failed');
                setLoading(false);
            }, 1500);
            return;
        }

        try {
            const res = await fetch(`http://localhost:8080/api/payments/${transactionId}/pay`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    ...formData,
                    cardNumber: formData.cardNumber.replace(/\s/g, '')
                }),
            });
            const data = await res.json();
            setResult(data.statusCode === 'paid' ? '✅ Payment successful!' : '❌ Payment failed');
        } catch (err) {
            console.error(err);
            setError('Payment error occurred');
        } finally {
            setLoading(false);
        }
    };

    const validateLuhn = (num: string) => {
        const clean = num.replace(/\D/g, '');
        let sum = 0;
        let shouldDouble = false;
        for (let i = clean.length - 1; i >= 0; i--) {
            let digit = parseInt(clean[i]);
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }
        return sum % 10 === 0;
    };

    const validateExpiry = (exp: string) => {
        if (!/^\d{2}\/\d{2}$/.test(exp)) return false;
        const [monthStr, yearStr] = exp.split('/');
        const month = parseInt(monthStr);
        const year = parseInt('20' + yearStr);
        if (month < 1 || month > 12) return false;
        const now = new Date();
        const expiry = new Date(year, month);
        return expiry > now;
    };

    if (error) return <div className={styles.error}>{error}</div>;
    if (!details) return <div className={styles.container}>Loading...</div>;

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Secure Payment</h1>

            <div className={styles.transactionInfo}>
                <p><strong>Transaction ID:</strong> {details.transactionId}</p>
                <p><strong>Amount:</strong> ${details.amount.toFixed(2)}</p>
            </div>

            <div className={styles.card}>
                <div className={styles.cardNumber}>{cardNumber || '•••• •••• •••• ••••'}</div>
                <div className={styles.cardFooter}>
                    <div>{cardExpiry || 'MM/YY'}</div>
                    <div>{cardCvc ? '•••' : 'CVC'}</div>
                </div>
            </div>

            <form className={styles.inputGroup} onSubmit={handleSubmit(onSubmit)}>
                <input
                    placeholder="Card Number"
                    maxLength={19 + 3} // учёт пробелов
                    className={classNames(styles.input, {[styles.invalid]: errors.cardNumber})}
                    {...register('cardNumber', {
                        required: 'Card number is required',
                        maxLength: {value: 19 + 3, message: 'Too long'},
                        onChange: (e) => {
                            let val = e.target.value.replace(/\D/g, '').slice(0, 16);
                            let formatted = val.replace(/(.{4})/g, '$1 ').trim();
                            setValue('cardNumber', formatted, {shouldValidate: true});
                            trigger('cardNumber');
                        },
                        validate: (val) => validateLuhn(val.replace(/\s/g, '')) || 'Invalid card number',
                    })}
                />
                {errors.cardNumber && <p className={styles.errorText}>{errors.cardNumber.message}</p>}

                <input
                    placeholder="MM/YY"
                    maxLength={5}
                    className={classNames(styles.input, {[styles.invalid]: errors.cardExpiry})}
                    {...register('cardExpiry', {
                        required: 'Expiry is required',
                        onChange: (e) => {
                            let val = e.target.value.replace(/[^\d]/g, '').slice(0, 4);
                            if (val.length >= 3) {
                                val = `${val.slice(0, 2)}/${val.slice(2)}`;
                            }
                            setValue('cardExpiry', val, {shouldValidate: true});
                            trigger('cardExpiry');
                        },
                        validate: (val) => validateExpiry(val) || 'Invalid expiry date',
                    })}
                />
                {errors.cardExpiry && <p className={styles.errorText}>{errors.cardExpiry.message}</p>}

                <input
                    placeholder="CVC"
                    maxLength={4}
                    className={classNames(styles.input, {[styles.invalid]: errors.cardCvc})}
                    {...register('cardCvc', {
                        required: 'CVC is required',
                        pattern: {
                            value: /^[0-9]{3,4}$/,
                            message: 'Invalid CVC',
                        },
                        onChange: () => trigger('cardCvc'),
                    })}
                />
                {errors.cardCvc && <p className={styles.errorText}>{errors.cardCvc.message}</p>}

                <button type="submit" className={styles.button} disabled={loading}>
                    {loading ? <span className={styles.loader}></span> : 'Confirm Payment'}
                </button>
            </form>

            {result && <div className={styles.result}>{result}</div>}
        </div>
    );
};

export default PayPage;
