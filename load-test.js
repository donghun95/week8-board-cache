import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
        const res = http.get('http://localhost:18080/api/boards/offset?cursor=49000&size=20');

        check(res, {
            'status is 200': (r) => r.status === 200,
        });

        sleep(1);
}
