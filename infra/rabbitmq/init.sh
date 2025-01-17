#!/bin/bash
set -e

until rabbitmqctl status; do
  >&2 echo "RabbitMQ is unavailable - sleeping"
  sleep 5
done
rabbitmq-plugins enable rabbitmq_management

rabbitmqctl add_vhost ${RABBIT_AUDIT_VHOST}
rabbitmqctl set_permissions -p ${RABBIT_AUDIT_VHOST} ${RABBITMQ_DEFAULT_USER} ".*" ".*" ".*"

echo "RabbitMQ setup completed"