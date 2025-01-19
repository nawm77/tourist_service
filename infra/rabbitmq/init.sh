#!/bin/bash
set -e

until rabbitmqctl status; do
  >&2 echo "RabbitMQ is unavailable - sleeping"
  sleep 5
done
rabbitmq-plugins enable rabbitmq_management

echo "RabbitMQ setup completed"