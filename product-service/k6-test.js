import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 1000 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(99)<100'],
    },
};

export default function () {
    const page = Math.floor(Math.random() * 100);
    const url = `http://localhost:8080/api/products/search?keyword=Product&page=${page}&size=20`;

    const res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'has products': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.content && body.content.length > 0;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(1);
}
