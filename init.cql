CREATE KEYSPACE IF NOT EXISTS payments WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS payments.payments (
    transaction_id text PRIMARY KEY,
    source_account text,
    target_account text,
    amount decimal,
    status text,
    timestamp timestamp,
    type text,
    user_id bigint
);
