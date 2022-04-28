import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    insecureSkipTLSVerify: true,
    thresholds: {
        checks: [{threshold: 'rate == 1.00'}],
    },
    vus: 1,
    iterations: 1
};

const BASE_URL = __ENV.BASE_URL;

export default () => {
    // Shorten Original URL without Custom Alias and no ttl
    for (let i = 0; i < 10; i++) {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        check(http.post(`${BASE_URL}/shorten`, payload, params), {
            '/shorten': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
    }


    // Shorten Original URL without Custom Alias and ttl
    for (let i = 0; i < 10; i++) {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        check(http.post(`${BASE_URL}/shorten?ttl=1000`, payload, params), {
            '/shorten ttl': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
    }

    // Shorten Original URL with Custom Alias and no ttl
    for (let i = 0; i < 10; i++) {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const alias = randomBase64(10)
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}`, payload, params), {
            '/shorten customAlias': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
    }

    // Shorten Original URL with Custom Alias and ttl
    for (let i = 0; i < 10; i++) {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const alias = randomBase64(10)
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}&ttl=1000`, payload, params), {
            '/shorten customAlias ttl': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
    }

    // Shorten Original URL if Custom Alias duplicated fail
    {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const alias = randomBase64(10)
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}`, payload, params), {
            '/shorten customAlias duplicate': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}`, payload, params), {
            '/shorten customAlias duplicate': (r) => {
                return r.status === 400
            },
        });
    }

    // Shorten Original URL if Custom Alias duplicated but expired no fail
    {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const alias = randomBase64(10)
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}&ttl=100`, payload, params), {
            '/shorten customAlias duplicate': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}&ttl=100`, payload, params), {
            '/shorten customAlias duplicate': (r) => {
                return r.status === 400
            },
        });
        sleep(1)
        check(http.post(`${BASE_URL}/shorten?customAlias=${alias}&ttl=100`, payload, params), {
            '/shorten customAlias duplicate': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
    }

    // Redirect if expired no redirect
    {
        const payload = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const res = http.post(`${BASE_URL}/shorten?ttl=100`, payload, params)
        const redirect = res.body
        check(res, {
            '/redirect expire': (r) => {
                const b = r.body
                return r.status === 200
                    && b.includes(BASE_URL)
            },
        });
        check(http.get(redirect), {
            '/redirect expire': (r) => {
                return r.status === 200
            },
        });
        sleep(10)
        check(http.get(redirect), {
            '/redirect expire': (r) => {
                return r.status === 404
            },
        });
    }
}

function randomBase64(length) {
    const randomChars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_';
    var result = '';
    for (var i = 0; i < length; i++ ) {
        result += randomChars.charAt(Math.floor(Math.random() * randomChars.length));
    }
    return result;
}
