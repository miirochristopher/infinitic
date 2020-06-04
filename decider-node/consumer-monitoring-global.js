const { pulsar } = require('./pulsar');
const { forMonitoringGlobalMessage } = require('./avro');

(async () => {
  // Create a consumer
  const consumer = await pulsar.subscribe({
    topic: 'persistent://public/default/monitoring-global',
    subscription: 'monitoring-global',
    subscriptionType: 'Shared',
    ackTimeoutMs: 10000,
  });

  // Receive messages
  for (let i = 0; i < 1000; i += 1) {
    const msg = await consumer.receive();
    console.log(forMonitoringGlobalMessage.fromBuffer(msg.getData()))
    consumer.acknowledge(msg);
  }

  await consumer.close();
  await pulsar.close();
})();