# Demo 5: Async Notification System

**Development Sprint**: Event-driven notifications  
**Estimated Duration**: 5-6 hours  
**Target**: Async publishing + notification workers + dead-letter queue  
**Agents**: nova-async-comm, nova-service-gen, nova-frontend-gen, nova-ops-monitor

---

## Business Context

When guarantees transition states, stakeholders must be notified. The system publishes events to a message broker, async workers consume them, and send emails/Slack notifications. Failed notifications move to DLQ for retry.

---

## Task Breakdown

### nova-async-comm (Messaging & Events)

**Objective**: Implement event publisher + broker infrastructure

**Must deliver**:

1. **Event Publisher in Backend**
   - [ ] Create event classes: `GuaranteeCreatedEvent`, `GuaranteeIssuedEvent`, `AmendmentAddedEvent`, `ClaimSubmittedEvent`
   - [ ] Each event has: eventType, guaranteeId, timestamp, applicantEmail, beneficiaryEmail, details
   - [ ] In `GuaranteeService.issue()`: `rabbitTemplate.convertAndSend("guarantees.events", event)`
   - [ ] In `GuaranteeService.addAmendment()`: publish GuaranteeAmendedEvent
   - [ ] In `ClaimService.submitClaim()`: publish ClaimSubmittedEvent

2. **RabbitMQ Configuration**
   - [ ] Create queue: `guarantee.notifications` (durable, auto-delete=false)
   - [ ] Create dead-letter queue: `guarantee.notifications.dlq`
   - [ ] Bind DLX: if message rejected 3 times, move to DLQ
   - [ ] Message TTL: 24 hours (messages expire if not consumed)
   - [ ] Configuration class with @Bean methods for Queue, Exchange, Binding

3. **Event Schema**
   - [ ] JSON format:
     ```json
     {
       "eventType": "GUARANTEE_ISSUED",
       "guaranteeId": 123,
       "guaranteeReference": "BG-2026-001",
       "applicantEmail": "applicant@example.com",
       "beneficiaryEmail": "beneficiary@example.com",
       "timestamp": "2026-06-29T10:30:00Z"
     }
     ```

4. **Message Routing**
   - [ ] Exchange: `guarantees.events` (topic or direct)
   - [ ] Routing key pattern: `guarantee.*` (to catch all guarantee events)
   - [ ] Alternative: separate queues per event type

5. **Graceful Error Handling**
   - [ ] If RabbitMQ is down: buffer events locally (or log+retry)
   - [ ] Retry logic: exponential backoff (1s, 2s, 4s)
   - [ ] DLQ inspection: log rejected messages with reason

**Success Criteria**:
- ✅ Events published when guarantees issued/amended
- ✅ Events appear in RabbitMQ queue
- ✅ DLQ configured and functional
- ✅ Message format valid JSON

---

### nova-service-gen (Backend Services)

**Objective**: Integrate event publishing into business logic

**Must deliver**:

1. **Notification Service**
   - [ ] Create `NotificationService` with methods:
     - `notifyGuaranteeIssued(Guarantee)`
     - `notifyAmendmentAdded(Amendment)`
     - `notifyClaimSubmitted(Claim)`
   - [ ] Each method publishes event to RabbitMQ

2. **Integration Points**
   - [ ] In `GuaranteeService.issue()`: call `notificationService.notifyGuaranteeIssued()`
   - [ ] In `GuaranteeService.addAmendment()`: call `notificationService.notifyAmendmentAdded()`
   - [ ] In `ClaimService.submitClaim()`: call `notificationService.notifyClaimSubmitted()`
   - [ ] Ensure publication is async (use @Async or RabbitTemplate)

3. **Event Data Enrichment**
   - [ ] Include applicant/beneficiary emails in event
   - [ ] Include human-readable details (guarantee reference, amount, etc.)

**Success Criteria**:
- ✅ Events published on every state change
- ✅ Events include all necessary data for notification
- ✅ No errors in logs when publishing

---

### nova-frontend-gen (Angular UI)

**Objective**: Display real-time notifications to users

**Must deliver**:

1. **WebSocket/Server-Sent Events (SSE) Integration**
   - [ ] Create service: `NotificationService` (different from backend service)
   - [ ] Connect to backend endpoint: `GET /api/v1/events/subscribe`
   - [ ] Subscribe to event stream in component init
   - [ ] Unsubscribe on component destroy

2. **Backend SSE Endpoint**
   - [ ] Add to backend: `@GetMapping("/api/v1/events/subscribe") SseEmitter subscribe()`
   - [ ] For each event published, send to all connected SSE clients
   - [ ] Payload: event details (guaranteeReference, status change, etc.)

3. **Toast Notifications**
   - [ ] On event received: display toast in top-right corner
   - [ ] Content: "Guarantee BG-2026-001 has been issued"
   - [ ] Auto-dismiss after 5 seconds
   - [ ] Color by event type (green=issued, blue=amended, orange=claimed)

4. **Notification Center (Optional)**
   - [ ] Store last 20 notifications in memory
   - [ ] UI component: bell icon with dropdown showing notification history
   - [ ] Each notification: timestamp, type, guarantee reference

**Success Criteria**:
- ✅ Toast appears when guarantee status changes
- ✅ No console errors
- ✅ Real-time updates visible (no page refresh needed)

---

### nova-ops-monitor (Infrastructure & Observability)

**Objective**: Monitor message broker and notification system

**Must deliver**:

1. **RabbitMQ Monitoring**
   - [ ] Access RabbitMQ Management UI: http://localhost:15672
   - [ ] Monitor: queue length, consumer count, message rate
   - [ ] Create Prometheus metrics exporter (or use native metrics)

2. **Metrics Dashboard**
   - [ ] Grafana dashboard: "Event Notification Pipeline"
   - [ ] Panels:
     - Messages published/sec: line graph
     - Queue depth: gauge (should stay <1000)
     - DLQ size: gauge (should stay ~0, alert if growing)
     - Consumer lag: time to process message
     - Error rate: failed notifications / total

3. **Alerts**
   - [ ] Alert: "Queue depth > 5000" (messages piling up)
   - [ ] Alert: "DLQ size > 100" (notifications failing)
   - [ ] Alert: "No consumers listening" (workers down)

4. **Health Check**
   - [ ] Include RabbitMQ health in `/actuator/health`
   - [ ] Endpoint should return DOWN if RabbitMQ unreachable

**Success Criteria**:
- ✅ RabbitMQ metrics visible in Grafana
- ✅ Queue depth trends monitored
- ✅ DLQ growth alerts functional

---

## Verification Checklist

**After all agents complete**:

1. **Event Publishing**
   - [ ] Issue a guarantee via UI
   - [ ] Check RabbitMQ Management UI: message appears in `guarantee.notifications` queue
   - [ ] Verify message content: includes guaranteeReference, timestamp, emails

2. **Toast Notifications**
   - [ ] After issuing guarantee, toast appears in UI (within 1 second)
   - [ ] Toast says "Guarantee BG-2026-001 has been issued"

3. **DLQ Handling**
   - [ ] Simulate consumer failure: stop the worker (or throw exception)
   - [ ] Publish event: should retry 3 times, then move to DLQ
   - [ ] Verify message in DLQ via Management UI
   - [ ] Fix consumer, messages move back to main queue

4. **Grafana Monitoring**
   - [ ] Dashboard shows messages published (counter increasing)
   - [ ] Queue depth returns to 0 after consumption
   - [ ] Consumer lag < 100ms

**Definition of Done for Demo 5**: 
All agents complete, events published, notifications displayed, DLQ functional, metrics monitored.

---

## Full System Flow

1. User issues guarantee via UI (POST /api/v1/guarantees/{id}/issue)
2. Backend: GuaranteeService.issue() → notificationService.notifyGuaranteeIssued()
3. RabbitTemplate publishes GuaranteeIssuedEvent to `guarantees.events`
4. Message arrives in `guarantee.notifications` queue
5. Notification worker (async @RabbitListener) consumes message
6. Worker sends email notification to applicant/beneficiary
7. If email service fails: message retries 3 times, then moves to DLQ
8. Frontend subscribed to SSE receives event
9. Toast notification displays to user (no page refresh)
10. Grafana shows message flow metrics

---

## Future Enhancements

- Kafka instead of RabbitMQ (higher throughput)
- Email templating (HTML emails, multiple languages)
- SMS notifications (Twilio)
- Slack webhooks (notify team channels)
- Email retry with exponential backoff (current: simple retry)
- Notification preferences (user configurable: email/SMS/none)
